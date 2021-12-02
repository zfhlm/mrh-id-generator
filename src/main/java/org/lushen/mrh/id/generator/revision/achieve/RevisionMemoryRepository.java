package org.lushen.mrh.id.generator.revision.achieve;

import static org.lushen.mrh.id.generator.revision.achieve.DefaultRevisionIdGenerator.maxWorkerId;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.revision.RevisionNode;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.supports.NamespaceSupport;

/**
 * revision 持久化接口 memory 实现，使用 synchronized 方法锁进行并发控制，测试使用
 * 
 * @author hlm
 */
public class RevisionMemoryRepository extends NamespaceSupport implements RevisionRepository {

	private static final ConcurrentHashMap<String, RevisionNode[]> nodeGroups = new ConcurrentHashMap<String, RevisionNode[]>();

	private final Log log = LogFactory.getLog(RevisionMemoryRepository.class.getSimpleName());

	public RevisionMemoryRepository() {
		this(null);
	}

	public RevisionMemoryRepository(String namespace) {
		super(namespace);
	}

	private RevisionNode[] nodes() {
		return nodeGroups.computeIfAbsent(this.namespace, e -> new RevisionNode[(int)maxWorkerId+1]);
	}

	@Override
	public synchronized RevisionNode next(long begin, Duration timeToLive) {

		RevisionNode[] nodes = nodes();

		return Arrays.stream(nodes).filter(Objects::nonNull).filter(e -> e.getExpired() < begin).findFirst().map(expiredNode -> {

			// 更新节点
			RevisionNode node = new RevisionNode(expiredNode.getWorkerId(), begin+timeToLive.toMillis());
			nodes[node.getWorkerId()] = node;

			if(log.isInfoEnabled()) {
				log.info("Update node " + node);
			}

			return node;

		}).orElse(IntStream.range(0, nodes.length).boxed().filter(e -> nodes[e] == null).findFirst().map(workerId -> {

			// 添加节点
			RevisionNode node = new RevisionNode(workerId, begin+timeToLive.toMillis());
			nodes[workerId] = node;

			if(log.isInfoEnabled()) {
				log.info("Add node " + node);
			}

			return node;

		}).orElseThrow(() -> new RuntimeException("No workId available")));

	}

	@Override
	public synchronized RevisionNode delay(int workerId, long expired, Duration timeToLive) {

		RevisionNode[] nodes = nodes();

		return Optional.ofNullable(nodes[workerId]).filter(node -> node.getExpired()==expired).map(passNode -> {

			// 更新节点
			RevisionNode newNode = new RevisionNode(workerId, expired+timeToLive.toMillis());
			nodes[workerId] = newNode;

			if(log.isInfoEnabled()) {
				log.info("Delay node " + newNode);
			}

			return newNode;

		}).orElseThrow(() -> new RuntimeException(String.format("Failed to delay node [%s] with expired [%s] !", workerId, expired)));

	}

}
