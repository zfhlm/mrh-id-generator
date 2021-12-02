package org.lushen.mrh.id.generator.boot;

import java.time.Duration;

import javax.sql.DataSource;

import org.lushen.mrh.id.generator.revision.RevisionIdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.achieve.AutoDelayRevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.achieve.RevisionMysqlJdbcRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RevisionConfiguration {

	@Bean
	public RevisionProperties revisionProperties() {
		RevisionProperties properties = RevisionProperties.buildDefault();
		properties.setInterval(Duration.ofSeconds(30L));
		properties.setTimeToLive(Duration.ofMinutes(10));
		properties.setThreshold(80);
		return properties;
	}

	@Bean
	public RevisionRepository revisionRepository(DataSource dataSource) {
		return new RevisionMysqlJdbcRepository("mytest", dataSource);
	}

	@Bean
	public RevisionIdGenerator revisionIdGenerator(RevisionRepository repository, RevisionProperties properties) {
		return new AutoDelayRevisionIdGeneratorFactory(repository).create(properties);
	}

}
