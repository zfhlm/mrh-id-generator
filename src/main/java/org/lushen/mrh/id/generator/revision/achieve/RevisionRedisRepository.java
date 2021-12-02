package org.lushen.mrh.id.generator.revision.achieve;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.lushen.mrh.id.generator.revision.achieve.DefaultRevisionIdGenerator.maxWorkerId;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.revision.RevisionNode;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.supports.NamespaceSupport;
import org.lushen.mrh.id.generator.supports.RedisLock;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * revision 持久化接口 redis 实现，使用 redis setNX 全局锁进行并发控制
 * 
 * @author hlm
 */
public class RevisionRedisRepository extends NamespaceSupport implements RevisionRepository {

	private static final String LOCK = ".~lock";

	private final Log log = LogFactory.getLog(RevisionRedisRepository.class.getSimpleName());

	private final RedisConnectionFactory connectionFactory;

	private final Duration lockTimeout;

	public RevisionRedisRepository(RedisConnectionFactory connectionFactory) {
		this(null, connectionFactory);
	}

	public RevisionRedisRepository(String namespace, RedisConnectionFactory connectionFactory) {
		this(namespace, connectionFactory, Duration.ofSeconds(60));
	}

	public RevisionRedisRepository(RedisConnectionFactory connectionFactory, Duration lockTimeout) {
		this(null, connectionFactory, lockTimeout);
	}

	public RevisionRedisRepository(String namespace, RedisConnectionFactory connectionFactory, Duration lockTimeout) {
		super(namespace);
		if(connectionFactory == null) {
			throw new IllegalArgumentException("connectionFactory is null");
		}
		if(lockTimeout == null) {
			throw new IllegalArgumentException("lockTimeout is null");
		}
		this.connectionFactory = connectionFactory;
		this.lockTimeout = lockTimeout;
	}

	@Override
	public RevisionNode next(long begin, Duration timeToLive) {

		return executeWithLock(() -> {

			// 查询所有节点
			byte[] name = this.namespace.getBytes(UTF_8);
			List<RevisionNode> nodes = execute(conn -> conn.hGetAll(name)).entrySet().stream()
					.map(entry -> new RevisionNode(Ints.fromByteArray(entry.getKey()), Longs.fromByteArray(entry.getValue())))
					.collect(Collectors.toList());

			// 获取过期节点
			RevisionNode expiredNode = nodes.stream().filter(e -> e.getExpired() < begin).findFirst().orElse(null);

			if(expiredNode != null) {

				//更新过期节点
				RevisionNode node = new RevisionNode(expiredNode.getWorkerId(), begin+timeToLive.toMillis());
				execute(conn -> conn.hSet(name, Ints.toByteArray(node.getWorkerId()), Longs.toByteArray(node.getExpired())));

				if(log.isInfoEnabled()) {
					log.info("Update node " + node);
				}

				return Optional.of(node);

			}

			// 获取一个未使用的节点
			Set<Integer> workerIds = nodes.stream().map(e -> e.getWorkerId()).collect(Collectors.toSet());
			int workerId = IntStream.range(0, (int)(maxWorkerId+1)).filter(e -> ! workerIds.contains(e) ).findFirst().orElse(-1);

			if(workerId != -1) {

				// 新增节点
				RevisionNode node = new RevisionNode(workerId, begin+timeToLive.toMillis());
				execute(conn -> conn.hSet(name, Ints.toByteArray(node.getWorkerId()), Longs.toByteArray(node.getExpired())));

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

		return executeWithLock(() -> {

			// 查询当前节点
			byte[] name = this.namespace.getBytes(UTF_8);
			byte[] key = Ints.toByteArray(workerId);
			byte[] value = execute(conn -> conn.hGet(name, key));

			if(value == null || Longs.fromByteArray(value) != expired) {
				return Optional.<RevisionNode>empty();
			}

			// 更新节点信息
			RevisionNode node = new RevisionNode(workerId, expired+timeToLive.toMillis());
			execute(conn -> conn.hSet(name, key, Longs.toByteArray(node.getExpired())));

			if(log.isInfoEnabled()) {
				log.info("Delay node " + node);
			}

			return Optional.of(node);

		}).orElseThrow(() -> new RuntimeException(String.format("Failed to delay node [%s] with expired [%s] !", workerId, expired)));

	}

	/**
	 * 全局锁执行 supplier
	 * 
	 * @param supplier
	 * @return
	 */
	private <T> T executeWithLock(Supplier<T> supplier) {
		String lockKey = new StringBuilder(this.namespace).append(LOCK).toString();
		Lock lock = new RedisLock(() -> this.connectionFactory.getConnection(), conn -> conn.close(), lockKey, this.lockTimeout);
		lock.lock();
		try {
			return supplier.get();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 执行 function
	 * 
	 * @param function
	 * @return
	 */
	private <T> T execute(Function<RedisConnection, T> function) {
		RedisConnection connection = this.connectionFactory.getConnection();
		try {
			return function.apply(connection);
		} finally {
			connection.close();
		}
	}

}
