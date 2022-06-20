package org.lushen.mrh.id.generator.revision.achieve;

import static org.lushen.mrh.id.generator.revision.RevisionIdGenerator.ActualRevisionIdGenerator.maxWorkerId;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.revision.RevisionEntity;
import org.lushen.mrh.id.generator.revision.RevisionException;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.RevisionWorker;
import org.lushen.mrh.id.generator.supports.JdbcExecutor;

/**
 * revision 持久化接口 mysql jdbc 实现，使用 select for update 悲观锁进行并发控制（因为数据量很少速度更快，使用乐观锁冲突概率非常高）
 * 
 * @author hlm
 */
public class RevisionMysqlJdbcRepository implements RevisionRepository {

	private final Log log = LogFactory.getLog(RevisionMysqlJdbcRepository.class.getSimpleName());

	private final DataSource dataSource;

	public RevisionMysqlJdbcRepository(DataSource dataSource) {
		super();
		if(dataSource == null) {
			throw new IllegalArgumentException("dataSource is null");
		}
		this.dataSource = dataSource;
	}

	@Override
	public RevisionWorker obtain(String namespace, long macthingAt, Duration timeToLive, int... workerIds) {

		return JdbcExecutor.executeWithTransaction(() -> dataSource.getConnection(), conn -> conn.close(), conn -> {

			// 查询所有工作节点
			String queryForUpdate = "select * from revision_alloc where namespace=? order by worker_id asc for update";
			List<RevisionEntity> entities = JdbcExecutor.executeQuery(conn, queryForUpdate, (rs -> {
				RevisionEntity entity = new RevisionEntity();
				entity.setNamespace(rs.getString("namespace"));
				entity.setWorkerId(rs.getInt("worker_id"));
				entity.setLastTimestamp(rs.getLong("last_timestamp"));
				entity.setCreateTime(rs.getDate("create_time"));
				entity.setModifyTime(rs.getDate("modify_time"));
				return entity;
			}), namespace);

			// 获取可用节点，排序将期望节点排在前面
			Set<Integer> workerIdSet = (workerIds == null ? Collections.emptySet() : Arrays.stream(workerIds).boxed().collect(Collectors.toSet()));
			RevisionEntity availableEntity = entities.stream()
					.filter(Objects::nonNull)
					.filter(e -> e.getLastTimestamp() < macthingAt)
					.sorted((prev, next) -> {
						if(workerIdSet.contains(prev.getWorkerId())) {
							return -1;
						}
						if(workerIdSet.contains(next.getWorkerId())) {
							return 1;
						}
						return 0;
					})
					.findFirst()
					.orElse(null);

			// 存在可用节点
			if(availableEntity != null) {

				//更新可用节点
				RevisionEntity entity = new RevisionEntity();
				entity.setNamespace(availableEntity.getNamespace());
				entity.setWorkerId(availableEntity.getWorkerId());
				entity.setLastTimestamp(macthingAt + timeToLive.toMillis());
				entity.setCreateTime(availableEntity.getCreateTime());
				entity.setModifyTime(new Date());
				String update = "update `revision_alloc` set last_timestamp=?, modify_time=now() where worker_id=? and namespace=?";
				JdbcExecutor.executeUpdate(conn, update, entity.getLastTimestamp(), entity.getWorkerId(), namespace);

				if(log.isInfoEnabled()) {
					log.info("Update worker " + entity);
				}

				// 返回节点信息
				RevisionWorker worker = new RevisionWorker();
				worker.setNamespace(entity.getNamespace());
				worker.setWorkerId(entity.getWorkerId());
				worker.setBeginAt(macthingAt);
				worker.setExpiredAt(entity.getLastTimestamp());

				return Optional.of(worker);

			}

			// 查询未被实例化的 workerId
			Set<Integer> allWorkerIds = entities.stream().map(e -> e.getWorkerId()).collect(Collectors.toSet());
			int workerId = IntStream.range(0, (int)(maxWorkerId+1)).filter(e -> ! allWorkerIds.contains(e) ).findFirst().orElse(-1);

			// 存在未被实例化的 workerId
			if(workerId != -1) {

				// 新增节点
				RevisionEntity entity = new RevisionEntity();
				entity.setNamespace(namespace);
				entity.setWorkerId(workerId);
				entity.setLastTimestamp(macthingAt + timeToLive.toMillis());
				entity.setCreateTime(new Date());
				entity.setModifyTime(new Date());
				String insert = "insert into `revision_alloc`(worker_id, namespace, last_timestamp, create_time, modify_time) values (?, ?, ?, now(), now())";
				JdbcExecutor.executeUpdate(conn, insert, entity.getWorkerId(), namespace, entity.getLastTimestamp());

				if(log.isInfoEnabled()) {
					log.info("Add worker " + entity);
				}

				// 返回节点信息
				RevisionWorker worker = new RevisionWorker();
				worker.setNamespace(entity.getNamespace());
				worker.setWorkerId(entity.getWorkerId());
				worker.setBeginAt(macthingAt);
				worker.setExpiredAt(entity.getLastTimestamp());

				return Optional.of(worker);

			}

			return Optional.<RevisionWorker>empty();

		}).orElseThrow(() -> new RevisionException("No worker available"));

	}

}
