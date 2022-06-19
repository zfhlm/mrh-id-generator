package org.lushen.mrh.id.generator.revision.achieve;

import static org.lushen.mrh.id.generator.revision.RevisionIdGenerator.InnerRevisionIdGenerator.maxWorkerId;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.revision.RevisionException;
import org.lushen.mrh.id.generator.revision.RevisionException.RevisionMatchFailureException;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.RevisionTarget;
import org.lushen.mrh.id.generator.revision.RevisionTarget.RevisionAvailable;

/**
 * revision 持久化接口 memory 实现，使用 synchronized 方法锁进行并发控制，测试使用
 * 
 * @author hlm
 */
public class RevisionMemoryRepository implements RevisionRepository {

	private final ConcurrentHashMap<String, RevisionTarget[]> nodeGroups = new ConcurrentHashMap<String, RevisionTarget[]>();

	private final Log log = LogFactory.getLog(RevisionMemoryRepository.class.getSimpleName());

	public RevisionMemoryRepository() {
		super();
	}

	@Override
	public synchronized RevisionAvailable attempt(String namespace, Duration timeToLive) throws RevisionException {

		RevisionTarget[] nodes = nodeGroups.computeIfAbsent(namespace, e -> new RevisionTarget[(int)maxWorkerId+1]);
		long beginAt = System.currentTimeMillis();

		// 尝试获取一个到期节点
		RevisionTarget expiredNode = Arrays.stream(nodes).filter(Objects::nonNull).filter(e -> e.getExpiredAt() < beginAt).findFirst().orElse(null);
		if(expiredNode != null) {

			RevisionTarget node = new RevisionTarget(expiredNode.getWorkerId(), beginAt+timeToLive.toMillis());
			nodes[node.getWorkerId()] = node;

			// 更新节点信息
			if(log.isInfoEnabled()) {
				log.info("Update node " + node);
			}

			return new RevisionAvailable(node.getWorkerId(), beginAt, node.getExpiredAt());
		}

		// 尝试获取一个从未使用的节点
		Integer workerId = IntStream.range(0, nodes.length).boxed().filter(e -> nodes[e] == null).findFirst().orElse(null);
		if(workerId != null) {

			// 添加节点信息
			RevisionTarget node = new RevisionTarget(workerId, beginAt+timeToLive.toMillis());
			nodes[workerId] = node;

			if(log.isInfoEnabled()) {
				log.info("Add node " + node);
			}

			return new RevisionAvailable(node.getWorkerId(), beginAt, node.getExpiredAt());
		}

		// 无任何可用节点
		throw new RevisionException("No workId available");
	}

	@Override
	public synchronized RevisionAvailable attempt(String namespace, Duration timeToLive, RevisionTarget target) throws RevisionException, RevisionMatchFailureException {

		RevisionTarget[] nodes = nodeGroups.computeIfAbsent(namespace, e -> new RevisionTarget[(int)maxWorkerId+1]);

		// 尝试获取目标节点
		int workerId = target.getWorkerId();
		long expiredAt = target.getExpiredAt();
		RevisionTarget node = nodes[workerId];

		// 更新节点信息
		if(node != null && node.getExpiredAt()==expiredAt) {

			RevisionTarget newNode = new RevisionTarget(workerId, expiredAt+timeToLive.toMillis());
			nodes[workerId] = newNode;

			if(log.isInfoEnabled()) {
				log.info("Update target node " + newNode);
			}

			return new RevisionAvailable(workerId, expiredAt+1, newNode.getExpiredAt());
		}

		throw new RevisionMatchFailureException(target.toString());

	}

}
