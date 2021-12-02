package org.lushen.mrh.id.generator.revision.achieve;

import static org.lushen.mrh.id.generator.revision.achieve.DefaultRevisionIdGenerator.maxWorkerId;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.revision.RevisionNode;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.supports.JdbcExecutor;
import org.lushen.mrh.id.generator.supports.NamespaceSupport;

/**
 * revision 持久化接口 mysql jdbc 实现，使用 select for update 悲观锁进行并发控制（因为数据量很少速度更快，使用乐观锁冲突概率非常高）
 * 
 * @author hlm
 */
public class RevisionMysqlJdbcRepository extends NamespaceSupport implements RevisionRepository {

	private final Log log = LogFactory.getLog(RevisionMysqlJdbcRepository.class.getSimpleName());

	private final DataSource dataSource;

	public RevisionMysqlJdbcRepository(DataSource dataSource) {
		this(null, dataSource);
	}

	public RevisionMysqlJdbcRepository(String namespace, DataSource dataSource) {
		super(namespace);
		if(dataSource == null) {
			throw new IllegalArgumentException("dataSource is null");
		}
		this.dataSource = dataSource;
	}

	@Override
	public RevisionNode next(long begin, Duration timeToLive) {

		return JdbcExecutor.executeWithTransaction(() -> dataSource.getConnection(), conn -> conn.close(), conn -> {

			// 查询所有工作节点
			String queryForUpdate = "select * from revision_alloc where namespace=? order by worker_id asc for update";
			List<RevisionNode> nodes = JdbcExecutor.executeQuery(conn, queryForUpdate, (rs -> {
				return new RevisionNode(rs.getInt("worker_id"), rs.getLong("expired"));
			}), this.namespace);

			RevisionNode expiredNode = nodes.stream().filter(e -> e.getExpired() < begin).findFirst().orElse(null);

			if(expiredNode != null) {

				//更新过期节点
				RevisionNode node = new RevisionNode(expiredNode.getWorkerId(), begin+timeToLive.toMillis());
				String update = "update `revision_alloc` set expired=?, modify_time=now() where worker_id=? and namespace=?";
				JdbcExecutor.executeUpdate(conn, update, node.getExpired(), node.getWorkerId(), this.namespace);

				if(log.isInfoEnabled()) {
					log.info("Update node " + node);
				}

				return Optional.of(node);

			}

			// 获取未使用的节点
			Set<Integer> workerIds = nodes.stream().map(e -> e.getWorkerId()).collect(Collectors.toSet());
			int workerId = IntStream.range(0, (int)(maxWorkerId+1)).filter(e -> ! workerIds.contains(e) ).findFirst().orElse(-1);

			if(workerId != -1) {

				// 新增节点
				RevisionNode node = new RevisionNode(workerId, begin+timeToLive.toMillis());
				String insert = "insert into `revision_alloc`(worker_id, namespace, expired, create_time, modify_time) values (?, ?, ?, now(), now())";
				JdbcExecutor.executeUpdate(conn, insert, node.getWorkerId(), this.namespace, node.getExpired());

				if(log.isInfoEnabled()) {
					log.info("Add node " + node);
				}

				return Optional.of(node);

			}

			return Optional.<RevisionNode>empty();

		}).orElseThrow(() -> new RuntimeException("No workId available"));

	}

	@Override
	public RevisionNode delay(int workerId, long expired, Duration timeToLive) {

		return JdbcExecutor.executeWithTransaction(() -> dataSource.getConnection(), conn -> conn.close(), conn -> {

			// 查询节点数据
			String selectForUpdate = "select * from `revision_alloc` where worker_id=? and namespace=? for update";
			RevisionNode passNode = JdbcExecutor.executeQueryFirst(conn, selectForUpdate, (rs -> {
				return new RevisionNode(rs.getInt("worker_id"), rs.getLong("expired"));
			}), workerId, this.namespace);

			// 版本号不匹配
			if(passNode == null || passNode.getExpired() != expired) {
				return Optional.<RevisionNode>empty();
			}

			// 更新节点数据
			RevisionNode newNode = new RevisionNode(workerId, expired+timeToLive.toMillis());
			String update = "update `revision_alloc` set expired=?, modify_time=now() where worker_id=? and namespace=?";
			JdbcExecutor.executeUpdate(conn, update, newNode.getExpired(), newNode.getWorkerId(), this.namespace);

			if(log.isInfoEnabled()) {
				log.info("Delay node " + newNode);
			}

			return Optional.of(newNode);

		}).orElseThrow(() -> new RuntimeException(String.format("Failed to delay node [%s] with expired [%s] !", workerId, expired)));

	}

}
