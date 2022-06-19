package org.lushen.mrh.id.generator.revision.achieve;

import static org.lushen.mrh.id.generator.revision.RevisionIdGenerator.InnerRevisionIdGenerator.maxWorkerId;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.revision.RevisionException;
import org.lushen.mrh.id.generator.revision.RevisionException.RevisionMatchFailureException;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.RevisionTarget;
import org.lushen.mrh.id.generator.revision.RevisionTarget.RevisionAvailable;
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
	public RevisionAvailable attempt(String namespace, Duration timeToLive) throws RevisionException {

		return JdbcExecutor.executeWithTransaction(() -> dataSource.getConnection(), conn -> conn.close(), conn -> {

			// 查询所有工作节点
			String queryForUpdate = "select * from revision_alloc where namespace=? order by worker_id asc for update";
			List<RevisionTarget> nodes = JdbcExecutor.executeQuery(conn, queryForUpdate, (rs -> {
				return new RevisionTarget(rs.getInt("worker_id"), rs.getLong("expired"));
			}), namespace);

			long beginAt = System.currentTimeMillis();
			
			// 获取到期节点
			RevisionTarget expiredNode = nodes.stream().filter(e -> e.getExpiredAt() < beginAt).findFirst().orElse(null);

			if(expiredNode != null) {

				//更新过期节点
				RevisionTarget node = new RevisionTarget(expiredNode.getWorkerId(), beginAt+timeToLive.toMillis());
				String update = "update `revision_alloc` set expired=?, modify_time=now() where worker_id=? and namespace=?";
				JdbcExecutor.executeUpdate(conn, update, node.getExpiredAt(), node.getWorkerId(), namespace);

				if(log.isInfoEnabled()) {
					log.info("Update node " + node);
				}

				return Optional.of(new RevisionAvailable(node.getWorkerId(), beginAt, node.getExpiredAt()));

			}

			// 获取未使用的节点
			Set<Integer> workerIds = nodes.stream().map(e -> e.getWorkerId()).collect(Collectors.toSet());
			int workerId = IntStream.range(0, (int)(maxWorkerId+1)).filter(e -> ! workerIds.contains(e) ).findFirst().orElse(-1);

			if(workerId != -1) {

				// 新增节点
				RevisionTarget node = new RevisionTarget(workerId, beginAt+timeToLive.toMillis());
				String insert = "insert into `revision_alloc`(worker_id, namespace, expired, create_time, modify_time) values (?, ?, ?, now(), now())";
				JdbcExecutor.executeUpdate(conn, insert, node.getWorkerId(), namespace, node.getExpiredAt());

				if(log.isInfoEnabled()) {
					log.info("Add node " + node);
				}

				return Optional.of(new RevisionAvailable(node.getWorkerId(), beginAt, node.getExpiredAt()));

			}

			return Optional.<RevisionAvailable>empty();

		}).orElseThrow(() -> new RevisionException("No workId available"));

	}

	@Override
	public RevisionAvailable attempt(String namespace, Duration timeToLive, RevisionTarget target) throws RevisionException, RevisionMatchFailureException {

		return JdbcExecutor.executeWithTransaction(() -> dataSource.getConnection(), conn -> conn.close(), conn -> {

			int workerId = target.getWorkerId();
			long expiredAt = target.getExpiredAt();

			// 查询节点数据
			String selectForUpdate = "select * from `revision_alloc` where worker_id=? and namespace=? for update";
			RevisionTarget passNode = JdbcExecutor.executeQueryFirst(conn, selectForUpdate, (rs -> {
				return new RevisionTarget(rs.getInt("worker_id"), rs.getLong("expired"));
			}), workerId, namespace);

			// 过期时间不匹配(已被占用)
			if(passNode == null || passNode.getExpiredAt() != expiredAt) {
				return Optional.<RevisionAvailable>empty();
			}

			// 更新节点数据
			RevisionTarget newNode = new RevisionTarget(workerId, expiredAt+timeToLive.toMillis());
			String update = "update `revision_alloc` set expired=?, modify_time=now() where worker_id=? and namespace=?";
			JdbcExecutor.executeUpdate(conn, update, newNode.getExpiredAt(), newNode.getWorkerId(), namespace);

			if(log.isInfoEnabled()) {
				log.info("Update target node " + newNode);
			}

			return Optional.of(new RevisionAvailable(workerId, expiredAt+1, newNode.getExpiredAt()));

		}).orElseThrow(() -> new RevisionMatchFailureException(target.toString()));

	}

}
