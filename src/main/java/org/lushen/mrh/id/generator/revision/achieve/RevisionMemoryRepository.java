package org.lushen.mrh.id.generator.revision.achieve;

import static org.lushen.mrh.id.generator.revision.RevisionIdGenerator.ActualRevisionIdGenerator.maxWorkerId;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.revision.RevisionEntity;
import org.lushen.mrh.id.generator.revision.RevisionException;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.RevisionWorker;

/**
 * revision 持久化接口 memory 实现，使用 synchronized 方法锁进行并发控制，测试使用
 * 
 * @author hlm
 */
public class RevisionMemoryRepository implements RevisionRepository {

	private final ConcurrentHashMap<String, RevisionEntity[]> nodeGroups = new ConcurrentHashMap<String, RevisionEntity[]>();

	private final Log log = LogFactory.getLog(RevisionMemoryRepository.class.getSimpleName());

	public RevisionMemoryRepository() {
		super();
	}

	@Override
	public synchronized RevisionWorker obtain(String namespace, long macthingAt, Duration timeToLive, int... workerIds) {

		// 获取所有节点
		RevisionEntity[] entities = nodeGroups.computeIfAbsent(namespace, e -> new RevisionEntity[(int)maxWorkerId+1]);

		// 获取可用节点，排序将期望节点排在前面
		Set<Integer> workerIdSet = (workerIds == null ? Collections.emptySet() : Arrays.stream(workerIds).boxed().collect(Collectors.toSet()));
		RevisionEntity availableEntity = Arrays.stream(entities)
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

			// 更新可用节点
			RevisionEntity entity = new RevisionEntity();
			entity.setNamespace(availableEntity.getNamespace());
			entity.setWorkerId(availableEntity.getWorkerId());
			entity.setLastTimestamp(macthingAt + timeToLive.toMillis());
			entity.setCreateTime(availableEntity.getCreateTime());
			entity.setModifyTime(new Date());
			entities[entity.getWorkerId()] = entity;

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
		Integer workerId = IntStream.range(0, entities.length)
				.boxed()
				.filter(e -> entities[e] == null)
				.findFirst()
				.orElse(null);

		// 存在未被实例化的 workerId
		if(workerId != null) {

			// 添加节点信息
			RevisionEntity entity = new RevisionEntity();
			entity.setNamespace(namespace);
			entity.setWorkerId(workerId);
			entity.setLastTimestamp(macthingAt + timeToLive.toMillis());
			entity.setCreateTime(new Date());
			entity.setModifyTime(new Date());
			entities[workerId] = entity;

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

		// 无任何可用节点
		throw new RevisionException("No worker available");

	}

}
