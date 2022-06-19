package org.lushen.mrh.id.generator.segment.achieve;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.lushen.mrh.id.generator.segment.Segment;
import org.lushen.mrh.id.generator.segment.SegmentRepository;

/**
 * 号段持久化接口 memory 实现，仅限测试使用
 * 
 * @author hlm
 */
public class SegmentMemoryRepository implements SegmentRepository {

	private final ConcurrentHashMap<String, AtomicLong> atomics = new ConcurrentHashMap<String, AtomicLong>();

	@Override
	public Segment next(String namespace, int range) {
		long min = atomics.computeIfAbsent(namespace, e -> new AtomicLong(0)).getAndAdd(range);
		return new Segment(min+1, min+range);
	}

}
