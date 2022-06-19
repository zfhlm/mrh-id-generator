package org.lushen.mrh.id.generator.atomic;

import java.util.concurrent.atomic.AtomicLong;

import org.lushen.mrh.id.generator.IdGenerator;

/**
 * atomic ID 生成器
 * 
 * @author hlm
 */
public class AtomicIdGenerator implements IdGenerator {

	private final AtomicLong atomic;

	AtomicIdGenerator(long seed) {
		super();
		this.atomic = new AtomicLong(seed);
	}

	@Override
	public long generate() {
		return atomic.getAndIncrement();
	}

}
