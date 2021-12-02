package org.lushen.mrh.id.generator.segment.achieve;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.lushen.mrh.id.generator.segment.Segment;
import org.lushen.mrh.id.generator.segment.SegmentRepository;
import org.lushen.mrh.id.generator.supports.NamespaceSupport;

/**
 * 号段持久化接口 memory 实现，仅限测试使用
 * 
 * @author hlm
 */
public class SegmentMemoryRepository extends NamespaceSupport implements SegmentRepository {

	private static final ConcurrentHashMap<String, AtomicLong> atomics = new ConcurrentHashMap<String, AtomicLong>();

	public SegmentMemoryRepository() {
		this(null);
	}

	public SegmentMemoryRepository(String namespace) {
		super(namespace);
	}

	@Override
	public Segment next(int range) {
		long min = atomics.computeIfAbsent(this.namespace, e -> new AtomicLong(0)).getAndAdd(range);
		return new Segment(min+1, min+range);
	}

}
