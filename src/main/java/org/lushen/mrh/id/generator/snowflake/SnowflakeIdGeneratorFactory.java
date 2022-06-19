package org.lushen.mrh.id.generator.snowflake;

import java.time.ZoneOffset;

import org.lushen.mrh.id.generator.IdGeneratorFactory;

/**
 * snowflake ID 生成器工厂
 * 
 * @author hlm
 */
public class SnowflakeIdGeneratorFactory implements IdGeneratorFactory<SnowflakeProperties> {

	@Override
	public SnowflakeIdGenerator create(SnowflakeProperties config) {

		if(config.getEpochDate() == null) {
			throw new IllegalArgumentException("epochDate is null");
		}

		long epochAt = config.getEpochDate().atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();

		return new SnowflakeIdGenerator(epochAt, config.getDataCenterId(), config.getWorkerId());
	}

}
