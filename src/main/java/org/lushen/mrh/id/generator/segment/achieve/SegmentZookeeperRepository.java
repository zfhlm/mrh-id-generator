package org.lushen.mrh.id.generator.segment.achieve;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicLong;
import org.apache.curator.retry.RetryNTimes;
import org.lushen.mrh.id.generator.segment.Segment;
import org.lushen.mrh.id.generator.segment.SegmentRepository;
import org.lushen.mrh.id.generator.supports.NamespaceSupport;

/**
 * 号段持久化接口 zookeeper 实现
 * 
 * @author hlm
 */
public class SegmentZookeeperRepository extends NamespaceSupport.Zookeeper implements SegmentRepository {

	private final CuratorFramework client;

	public SegmentZookeeperRepository(CuratorFramework client) {
		this(null, client);
	}

	public SegmentZookeeperRepository(String namespace, CuratorFramework client) {
		super(namespace);
		if(client == null) {
			throw new IllegalArgumentException("client is null");
		}
		this.client = client;
	}

	@Override
	public Segment next(int range) {
		while(true) {
			try {
				DistributedAtomicLong atomic = new DistributedAtomicLong(this.client, this.namespace, new RetryNTimes(0, 0));
				AtomicValue<Long> atomicValue = atomic.add(Long.valueOf(range));
				if(atomicValue.succeeded()) {
					long max = atomicValue.postValue();
					return new Segment(max-range+1, max);
				}
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
	}

}
