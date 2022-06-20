package org.lushen.mrh.id.generator.revision.achieve;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.lushen.mrh.id.generator.revision.RevisionIdGenerator.ActualRevisionIdGenerator.maxWorkerId;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.revision.RevisionEntity;
import org.lushen.mrh.id.generator.revision.RevisionException;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.RevisionWorker;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 * revision 持久化接口 redis 实现，使用 redis setNX 全局锁进行并发控制
 * 
 * @author hlm
 */
public class RevisionRedisRepository implements RevisionRepository {

	private static final String LOCK = ".~lock";

	private final Log log = LogFactory.getLog(RevisionRedisRepository.class.getSimpleName());

	private final Jackson2JsonRedisSerializer<RevisionEntity> serializer = new Jackson2JsonRedisSerializer<>(RevisionEntity.class);

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

	@Override
	public RevisionWorker obtain(String namespace, long macthingAt, Duration timeToLive, int... workerIds) {

		// 获取连接
		RedisConnection connection = this.connectionFactory.getConnection();

		// 全局锁 key value
		byte[] lockKey = new StringBuilder(namespace).append(LOCK).toString().getBytes(StandardCharsets.UTF_8);
		byte[] lockValue = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);

		try {

			// 使用 setNX 加锁
			for(;;) {
				if(connection.set(lockKey, lockValue, Expiration.from(this.lockTimeout), SetOption.ifAbsent())) {
					break;
				}
			}

			// 查询所有节点
			byte[] name = namespace.getBytes(UTF_8);
			List<RevisionEntity> entities = connection.hGetAll(name).entrySet().stream()
					.map(entry -> this.serializer.deserialize(entry.getValue()))
					.collect(Collectors.toList());

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
				connection.hSet(name, String.valueOf(entity.getWorkerId()).getBytes(StandardCharsets.UTF_8), this.serializer.serialize(entity));
				
				if(log.isInfoEnabled()) {
					log.info("Update worker " + entity);
				}

				// 返回节点信息
				RevisionWorker worker = new RevisionWorker();
				worker.setNamespace(entity.getNamespace());
				worker.setWorkerId(entity.getWorkerId());
				worker.setBeginAt(macthingAt);
				worker.setExpiredAt(entity.getLastTimestamp());

				return worker;

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
				connection.hSet(name, String.valueOf(entity.getWorkerId()).getBytes(StandardCharsets.UTF_8), this.serializer.serialize(entity));

				if(log.isInfoEnabled()) {
					log.info("Add worker " + entity);
				}

				// 返回节点信息
				RevisionWorker worker = new RevisionWorker();
				worker.setNamespace(entity.getNamespace());
				worker.setWorkerId(entity.getWorkerId());
				worker.setBeginAt(macthingAt);
				worker.setExpiredAt(entity.getLastTimestamp());

				return worker;

			}

			throw new RevisionException("No worker available");

		} finally {

			try {

				// 移除锁
				byte[] remoteValue = connection.get(lockKey);
				if(remoteValue != null && Arrays.equals(lockValue, remoteValue)) {
					connection.del(lockKey);
				}

			} finally {

				// 释放连接
				connection.close();

			}

		}

	}

}
