package org.lushen.mrh.id.generator.revision;

import org.lushen.mrh.id.generator.IdGenerator;
import org.lushen.mrh.id.generator.IdGeneratorFactory;

/**
 * revision ID 生成器工厂
 * 
 * @author hlm
 */
public class RevisionIdGeneratorFactory implements IdGeneratorFactory<RevisionProperties> {

	private final RevisionRepository repository;

	public RevisionIdGeneratorFactory(RevisionRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public IdGenerator create(RevisionProperties config) {

		if(config.getEpochDate() == null) {
			throw new IllegalArgumentException("epochDate");
		}
		if(config.getTimeToLive() == null) {
			throw new IllegalArgumentException("timeToLive");
		}
		if(config.getRemainingTimeToDelay() == null || config.getRemainingTimeToDelay().toMillis() >= config.getTimeToLive().toMillis()) {
			throw new IllegalArgumentException("remainingTimeToDelay");
		}

		RevisionIdGenerator idGenerator = new RevisionIdGenerator(this.repository);
		idGenerator.setNamespace(config.getNamespace());
		idGenerator.setEpochDate(config.getEpochDate());
		idGenerator.setTimeToLive(config.getTimeToLive());
		idGenerator.setRemainingTimeToDelay(config.getRemainingTimeToDelay());
		idGenerator.initialize();

		return idGenerator;
	}

}
