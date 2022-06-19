package org.lushen.mrh.id.generator.revision.achieve;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.lushen.mrh.id.generator.revision.RevisionIdGenerator.InnerRevisionIdGenerator.maxWorkerId;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.revision.RevisionException;
import org.lushen.mrh.id.generator.revision.RevisionException.RevisionMatchFailureException;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.RevisionTarget;
import org.lushen.mrh.id.generator.revision.RevisionTarget.RevisionAvailable;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.types.Expiration;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * revision 持久化接口 redis 实现，使用 redis setNX 全局锁进行并发控制
 * 
 * @author hlm
 */
public class RevisionRedisRepository implements RevisionRepository {

	private static final String LOCK = ".~lock";

	private final Log log = LogFactory.getLog(RevisionRedisRepository.class.getSimpleName());

	private final RedisConnectionFactory connectionFactory;

	private final Duration lockTimeout;

	public RevisionRedisRepository(RedisConnectionFactory connectionFactory) {
		this(connectionFactory, Duration.ofSeconds(60));
	}

	public RevisionRedisRepository(RedisConnectionFactory connectionFactory, Duration lockTimeout) {
		super();
		if(connectionFactory == null) {
			throw new IllegalArgumentException("connectionFactory is null");
		}
		if(lockTimeout == null) {
			throw new IllegalArgumentException("lockTimeout is null");
		}
		this.connectionFactory = connectionFactory;
		this.lockTimeout = lockTimeout;
	}

	// 重写锁以及后续执行方法，只使用一个连接，用完一起释放

	@Override
	public RevisionAvailable attempt(String namespace, Duration timeToLive) throws RevisionException {

		RedisConnection connection = this.connectionFactory.getConnection();
		try {

			String lockKey = new StringBuilder(namespace).append(LOCK).toString();
			Lock lock = new RedisLock(connection, lockKey, this.lockTimeout);
			lock.lock();

			try {

				long beginAt = System.currentTimeMillis();

				// 查询所有节点
				byte[] name = namespace.getBytes(UTF_8);
				List<RevisionTarget> nodes = connection.hGetAll(name).entrySet().stream()
						.map(entry -> new RevisionTarget(Ints.fromByteArray(entry.getKey()), Longs.fromByteArray(entry.getValue())))
						.collect(Collectors.toList());

				// 获取过期节点
				RevisionTarget expiredNode = nodes.stream().filter(e -> e.getExpiredAt() < beginAt).findFirst().orElse(null);

				if(expiredNode != null) {

					//更新过期节点
					RevisionTarget node = new RevisionTarget(expiredNode.getWorkerId(), beginAt+timeToLive.toMillis());
					connection.hSet(name, Ints.toByteArray(node.getWorkerId()), Longs.toByteArray(node.getExpiredAt()));

					if(log.isInfoEnabled()) {
						log.info("Update node " + node);
					}

					return new RevisionAvailable(node.getWorkerId(), beginAt, node.getExpiredAt());

				}

				// 获取一个未使用的节点
				Set<Integer> workerIds = nodes.stream().map(e -> e.getWorkerId()).collect(Collectors.toSet());
				int workerId = IntStream.range(0, (int)(maxWorkerId+1)).filter(e -> ! workerIds.contains(e) ).findFirst().orElse(-1);

				if(workerId != -1) {

					// 新增节点
					RevisionTarget node = new RevisionTarget(workerId, beginAt+timeToLive.toMillis());
					connection.hSet(name, Ints.toByteArray(node.getWorkerId()), Longs.toByteArray(node.getExpiredAt()));

					if(log.isInfoEnabled()) {
						log.info("Add node " + node);
					}

					return new RevisionAvailable(node.getWorkerId(), beginAt, node.getExpiredAt());

				}

				throw new RevisionException("No workId available");

			} finally {

				lock.unlock();

			}

		} finally {

			connection.close();

		}

	}

	@Override
	public RevisionAvailable attempt(String namespace, Duration timeToLive, RevisionTarget target) throws RevisionException, RevisionMatchFailureException {

		RedisConnection connection = this.connectionFactory.getConnection();
		try {

			String lockKey = new StringBuilder(namespace).append(LOCK).toString();
			Lock lock = new RedisLock(connection, lockKey, this.lockTimeout);
			lock.lock();

			try {

				// 查询当前节点
				int workerId = target.getWorkerId();
				long expiredAt = target.getExpiredAt();
				byte[] name = namespace.getBytes(UTF_8);
				byte[] key = Ints.toByteArray(workerId);
				byte[] value = connection.hGet(name, key);

				if(value == null || Longs.fromByteArray(value) != expiredAt) {
					throw new RevisionMatchFailureException(target.toString());
				}

				// 更新节点信息
				RevisionTarget node = new RevisionTarget(workerId, expiredAt+timeToLive.toMillis());
				connection.hSet(name, key, Longs.toByteArray(node.getExpiredAt()));

				if(log.isInfoEnabled()) {
					log.info("Update target node " + node);
				}

				return new RevisionAvailable(workerId, expiredAt+1, node.getExpiredAt());

			} finally {

				lock.unlock();

			}

		} finally {

			connection.close();

		}

	}

	/**
	 * redis 分布式锁
	 * 
	 * @author hlm
	 */
	private static final class RedisLock implements Lock {

		private final byte[] lockKey;						// 锁key

		private final byte[] lockValue;						// 锁value

		private final Duration lockTimeout;					// 锁超时时间(防死锁)

		private final RedisConnection conn;					// 连接

		public RedisLock(RedisConnection conn, String lockKey, Duration lockTimeout) {
			super();
			this.conn = conn;
			this.lockKey = lockKey.getBytes(StandardCharsets.UTF_8);
			this.lockValue = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
			this.lockTimeout = lockTimeout;
		}

		@Override
		public void lock() {
			for(;;) {
				if(tryLock()) {
					break;
				}
			}
		}

		@Override
		public void lockInterruptibly() throws InterruptedException {
			for(;;) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				if(tryLock()) {
					break;
				}
			}
		}

		@Override
		public boolean tryLock() {
			return conn.set(this.lockKey, this.lockValue, Expiration.from(this.lockTimeout), SetOption.ifAbsent());
		}

		@Override
		public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
			long start = System.currentTimeMillis();
			for(;;) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}
				if(tryLock()) {
					return true;
				}
				if(System.currentTimeMillis() - start > unit.toMillis(time)) {
					return false;
				}
			}
		}

		@Override
		public void unlock() {
			byte[] remoteValue = conn.get(this.lockKey);
			if(remoteValue != null && Arrays.equals(this.lockValue, remoteValue)) {
				conn.del(this.lockKey);
			}
		}

		@Override
		public Condition newCondition() {
			throw new RuntimeException("Not supported method !");
		}

	}

}
