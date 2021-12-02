package org.lushen.mrh.id.generator.segment.achieve;

import org.lushen.mrh.id.generator.segment.SegmentIdGenerator;
import org.lushen.mrh.id.generator.segment.SegmentIdGeneratorFactory;
import org.lushen.mrh.id.generator.segment.SegmentProperties;
import org.lushen.mrh.id.generator.segment.SegmentRepository;

/**
 * 号段 ID 生成器工厂实现
 * 
 * @author hlm
 */
public class DefaultSegmentIdGeneratorFactory implements SegmentIdGeneratorFactory {

	private final SegmentRepository repository;

	public DefaultSegmentIdGeneratorFactory(SegmentRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public SegmentIdGenerator create(SegmentProperties config) {

		if(config.getRange() <= 0) {
			throw new IllegalArgumentException("range");
		}
		if(config.getRemaining() <=0 || config.getRemaining() >= config.getRange()) {
			throw new IllegalArgumentException("remaining");
		}

		// 实例化并启动 ID生成器
		DefaultSegmentIdGenerator idGenerator = new DefaultSegmentIdGenerator(this.repository);
		idGenerator.setRange(config.getRange());
		idGenerator.setRemaining(config.getRemaining());
		idGenerator.start();

		return idGenerator;
	}

}
