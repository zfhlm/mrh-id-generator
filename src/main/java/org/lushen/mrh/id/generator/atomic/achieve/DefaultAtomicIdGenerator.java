package org.lushen.mrh.id.generator.atomic.achieve;

import java.util.concurrent.atomic.AtomicLong;

import org.lushen.mrh.id.generator.atomic.AtomicIdGenerator;

/**
 * atomic ID 生成器实现
 * 
 * @author hlm
 */
class DefaultAtomicIdGenerator implements AtomicIdGenerator {

	private final AtomicLong atomic;

	public DefaultAtomicIdGenerator() {
		this(1L);
	}

	public DefaultAtomicIdGenerator(long seed) {
		super();
		this.atomic = new AtomicLong(seed);
	}

	@Override
	public long generate() {
		return atomic.getAndIncrement();
	}

}
