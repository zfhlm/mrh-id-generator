package org.lushen.mrh.id.generator.atomic.achieve;

import org.lushen.mrh.id.generator.atomic.AtomicIdGenerator;
import org.lushen.mrh.id.generator.atomic.AtomicIdGeneratorFactory;

/**
 * atomic ID 生成器工厂实现
 * 
 * @author hlm
 */
public class DefaultAtomicIdGeneratorFactory implements AtomicIdGeneratorFactory {

	@Override
	public AtomicIdGenerator create(Long seed) {
		if(seed != null) {
			return new DefaultAtomicIdGenerator(seed);
		} else {
			return new DefaultAtomicIdGenerator();
		}
	}

}
