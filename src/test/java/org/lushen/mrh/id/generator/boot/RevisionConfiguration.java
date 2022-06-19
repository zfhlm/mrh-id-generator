package org.lushen.mrh.id.generator.boot;

import java.time.Duration;

import javax.sql.DataSource;

import org.lushen.mrh.id.generator.IdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.achieve.RevisionMysqlJdbcRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RevisionConfiguration {

	@Bean
	public RevisionProperties revisionProperties() {
		RevisionProperties properties = RevisionProperties.buildDefault();
		properties.setTimeToLive(Duration.ofMinutes(10));
		properties.setRemainingTimeToDelay(Duration.ofSeconds(30));
		return properties;
	}

	@Bean
	public RevisionRepository revisionRepository(DataSource dataSource) {
		return new RevisionMysqlJdbcRepository(dataSource);
	}

	@Bean
	public IdGenerator revisionIdGenerator(RevisionRepository repository, RevisionProperties properties) {
		return new RevisionIdGeneratorFactory(repository).create(properties);
	}

}
