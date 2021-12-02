package org.lushen.mrh.id.generator.revision.achieve;

import static org.lushen.mrh.id.generator.revision.achieve.DefaultRevisionIdGenerator.maxWorkerId;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorOp;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.lushen.mrh.id.generator.revision.RevisionNode;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.supports.NamespaceSupport;
import org.lushen.mrh.id.generator.supports.function.Supplier;

/**
 * revision 持久化接口 zookeeper 实现，使用全局可重入锁 InterProcessMutex 进行并发控制
 * 
 * @author hlm
 */
public class RevisionZookeeperRepository extends NamespaceSupport.Zookeeper implements RevisionRepository {

	private static final String LOCK = "~lock";

	private static final String COMMA = ".";

	private final Log log = LogFactory.getLog(RevisionZookeeperRepository.class.getSimpleName());

	private final CuratorFramework client;

	public RevisionZookeeperRepository(CuratorFramework client) {
		this(null, client);
	}

	public RevisionZookeeperRepository(String namespace, CuratorFramework client) {
		super(namespace);
		if(client == null) {
			throw new IllegalArgumentException("client is null");
		}
		this.client = client;
	}

	@Override
	public RevisionNode next(long begin, Duration timeToLive) {

		return executeWithLock(() -> {

			// 父节点不存在，创建父节点
			if(this.client.checkExists().forPath(this.namespace) == null) {
				this.client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(this.namespace);
			}

			// 查询所有节点
			List<String> nodePaths = this.client.getChildren().forPath(this.namespace);
			List<RevisionNode> nodes = nodePaths.stream().map(this::toNodeModel).filter(Objects::nonNull).collect(Collectors.toList());

			// 获取过期节点
			RevisionNode expiredNode = nodes.stream().filter(e -> e.getExpired() < begin).findFirst().orElse(null);

			if(expiredNode != null) {

				//更新过期节点
				RevisionNode node = new RevisionNode(expiredNode.getWorkerId(), begin+timeToLive.toMillis());
				CuratorOp deleteOp = this.client.transactionOp().delete().forPath(absolutePath(fromNodeModel(expiredNode)));
				CuratorOp createOp = this.client.transactionOp().create().withMode(CreateMode.PERSISTENT).forPath(absolutePath(fromNodeModel(node)));
				this.client.transaction().forOperations(deleteOp, createOp);

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
				this.client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(absolutePath(fromNodeModel(node)));

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

			// 父节点不存在
			if(this.client.checkExists().forPath(this.namespace) == null) {
				return Optional.<RevisionNode>empty();
			}

			// 查询当前节点
			List<String> nodePaths = this.client.getChildren().forPath(this.namespace);
			List<RevisionNode> nodes = nodePaths.stream().map(this::toNodeModel).filter(Objects::nonNull).collect(Collectors.toList());
			RevisionNode passNode = nodes.stream().filter(e -> e.getWorkerId() == workerId).findFirst().orElse(null);

			if(passNode == null || passNode.getExpired() != expired) {
				return Optional.<RevisionNode>empty();
			}

			// 更新节点信息
			RevisionNode newNode = new RevisionNode(workerId, expired+timeToLive.toMillis());
			CuratorOp deleteOp = this.client.transactionOp().delete().forPath(absolutePath(fromNodeModel(passNode)));
			CuratorOp createOp = this.client.transactionOp().create().withMode(CreateMode.PERSISTENT).forPath(absolutePath(fromNodeModel(newNode)));
			this.client.transaction().forOperations(deleteOp, createOp);

			if(log.isInfoEnabled()) {
				log.info("Delay node " + newNode);
			}

			return Optional.of(newNode);

		}).orElseThrow(() -> new RuntimeException(String.format("Failed to delay node [%s] with expired [%s] !", workerId, expired)));

	}

	private <T> T executeWithLock(Supplier<T> supplier) {
		String lockPath = new StringBuilder(this.namespace).append(SLASH).append(LOCK).toString();
		InterProcessMutex lock = new InterProcessMutex(this.client, lockPath);
		try {
			lock.acquire();
			return supplier.get();
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		} finally {
			try {
				lock.release();
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex);
			}
		}
	}

	private String absolutePath(String nodePath) {
		return new StringBuilder(this.namespace).append(SLASH).append(nodePath).toString();
	}

	private String fromNodeModel(RevisionNode node) {
		return new StringBuilder().append(node.getWorkerId()).append(COMMA).append(node.getExpired()).toString();
	}

	private RevisionNode toNodeModel(String nodePath) {
		if( ! LOCK.equals(nodePath) ) {
			int index = nodePath.indexOf(COMMA);
			return new RevisionNode(Integer.parseInt(nodePath.substring(0, index)), Long.parseLong(nodePath.substring(index+1)));
		} else {
			return null;
		}
	}

}
