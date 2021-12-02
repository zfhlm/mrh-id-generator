package org.lushen.mrh.id.generator.snowflake.achieve;

import java.time.ZoneOffset;

import org.lushen.mrh.id.generator.snowflake.SnowflakeIdGenerator;
import org.lushen.mrh.id.generator.snowflake.SnowflakeIdGeneratorFactory;
import org.lushen.mrh.id.generator.snowflake.SnowflakeProperties;

/**
 * snowflake ID 生成器工厂实现
 * 
 * @author hlm
 */
public class DefaultSnowflakeIdGeneratorFactory implements SnowflakeIdGeneratorFactory {

	@Override
	public SnowflakeIdGenerator create(SnowflakeProperties config) {

		if(config.getEpochDate() == null) {
			throw new IllegalArgumentException("epochDate is null");
		}

		long epochAt = config.getEpochDate().atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();

		return new DefaultSnowflakeIdGenerator(epochAt, config.getDataCenterId(), config.getWorkerId());
	}

}
