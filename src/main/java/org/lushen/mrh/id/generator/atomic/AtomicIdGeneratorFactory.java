package org.lushen.mrh.id.generator.atomic;

import org.lushen.mrh.id.generator.IdGenerator;
import org.lushen.mrh.id.generator.IdGeneratorFactory;

/**
 * atomic ID 生成器工厂
 * 
 * @author hlm
 */
public class AtomicIdGeneratorFactory implements IdGeneratorFactory<Long> {

	/**
	 * 创建 ID 生成器，初始值为 1
	 * 
	 * @return
	 */
	public IdGenerator create() {
		return create(1L);
	}

	@Override
	public IdGenerator create(Long seed) {
		if(seed != null) {
			return new AtomicIdGenerator(seed);
		}
		throw new IllegalArgumentException("seed can't be null !");
	}

}
