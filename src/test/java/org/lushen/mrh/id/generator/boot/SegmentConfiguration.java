package org.lushen.mrh.id.generator.boot;

import javax.sql.DataSource;

import org.lushen.mrh.id.generator.IdGenerator;
import org.lushen.mrh.id.generator.segment.SegmentIdGeneratorFactory;
import org.lushen.mrh.id.generator.segment.SegmentProperties;
import org.lushen.mrh.id.generator.segment.SegmentRepository;
import org.lushen.mrh.id.generator.segment.achieve.SegmentMysqlJdbcRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SegmentConfiguration {

	@Bean
	public SegmentProperties segmentProperties() {
		SegmentProperties properties = SegmentProperties.buildDefault();
		properties.setRange(10);
		return properties;
	}

	@Bean
	public SegmentRepository segmentRepository(DataSource dataSource) {
		return new SegmentMysqlJdbcRepository(dataSource);
	}

	@Bean
	public IdGenerator segmentIdGenerator(SegmentRepository repository, SegmentProperties properties) {
		return new SegmentIdGeneratorFactory(repository).create(properties);
	}

}
