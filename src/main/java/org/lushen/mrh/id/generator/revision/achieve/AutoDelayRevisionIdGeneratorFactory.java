package org.lushen.mrh.id.generator.revision.achieve;

import org.lushen.mrh.id.generator.revision.RevisionIdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.RevisionRepository;

/**
 * 自动延时 revision ID 生成器工厂实现
 * 
 * @author hlm
 */
public class AutoDelayRevisionIdGeneratorFactory implements RevisionIdGeneratorFactory {

	private final RevisionRepository repository;

	public AutoDelayRevisionIdGeneratorFactory(RevisionRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public RevisionIdGenerator create(RevisionProperties config) {

		if(config.getEpochDate() == null) {
			throw new IllegalArgumentException("epochDate");
		}
		if(config.getTimeToLive() == null) {
			throw new IllegalArgumentException("timeToLive");
		}
		if(config.getThreshold() <= 0 || config.getThreshold() >= 100) {
			throw new IllegalArgumentException("threshold");
		}
		if(config.getInterval() == null) {
			throw new IllegalArgumentException("interval");
		}

		AutoDelayRevisionIdGenerator idGenerator = new AutoDelayRevisionIdGenerator(this.repository);
		idGenerator.setEpochDate(config.getEpochDate());
		idGenerator.setInterval(config.getInterval());
		idGenerator.setThreshold(config.getThreshold());
		idGenerator.setTimeToLive(config.getTimeToLive());
		idGenerator.start();

		return idGenerator;
	}

}
