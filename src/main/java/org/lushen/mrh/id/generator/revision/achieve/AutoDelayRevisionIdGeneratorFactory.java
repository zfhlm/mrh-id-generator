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
		if(config.getRemainingTimeToDelay() == null || config.getRemainingTimeToDelay().toMillis() >= config.getTimeToLive().toMillis()) {
			throw new IllegalArgumentException("remainingTimeToDelay");
		}
	
		AutoDelayRevisionIdGenerator idGenerator = new AutoDelayRevisionIdGenerator(this.repository);
		idGenerator.setEpochDate(config.getEpochDate());
		idGenerator.setTimeToLive(config.getTimeToLive());
		idGenerator.setRemainingTimeToDelay(config.getRemainingTimeToDelay());
		idGenerator.start();

		return idGenerator;
	}

}
