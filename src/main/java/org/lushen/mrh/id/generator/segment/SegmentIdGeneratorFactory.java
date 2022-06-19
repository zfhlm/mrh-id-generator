package org.lushen.mrh.id.generator.segment;

import org.lushen.mrh.id.generator.IdGeneratorFactory;

/**
 * 号段 ID 生成器工厂
 * 
 * @author hlm
 */
public class SegmentIdGeneratorFactory implements IdGeneratorFactory<SegmentProperties> {

	private final SegmentRepository repository;

	public SegmentIdGeneratorFactory(SegmentRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public SegmentIdGenerator create(SegmentProperties config) {

		if(config.getNamespace() == null || config.getNamespace().trim().length() == 0) {
			throw new IllegalArgumentException("namespace");
		}
		if(config.getRange() <= 0) {
			throw new IllegalArgumentException("range");
		}
		if(config.getRemaining() <=0 || config.getRemaining() >= config.getRange()) {
			throw new IllegalArgumentException("remaining");
		}

		// 实例化 ID生成器
		SegmentIdGenerator idGenerator = new SegmentIdGenerator(this.repository);
		idGenerator.setRange(config.getRange());
		idGenerator.setRemaining(config.getRemaining());
		idGenerator.setNamespace(config.getNamespace());

		return idGenerator;
	}

}
