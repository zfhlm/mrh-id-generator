package org.lushen.mrh.id.generator.revision.jdbc;

import java.time.Duration;

import org.apache.commons.dbcp2.BasicDataSource;
import org.lushen.mrh.id.generator.IdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.achieve.RevisionMysqlJdbcRepository;

public class TestSingle {

	public static void main(String[] args) throws Exception {

		// 测试，可用时长 10 秒钟
		RevisionProperties properties = RevisionProperties.buildDefault();
		properties.setTimeToLive(Duration.ofSeconds(10L));
		properties.setRemainingTimeToDelay(Duration.ofSeconds(3));

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		dataSource.setUrl("jdbc:mysql://192.168.140.210:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8");
		RevisionMysqlJdbcRepository repository = new RevisionMysqlJdbcRepository(dataSource);

		IdGenerator idGenerator = new RevisionIdGeneratorFactory(repository).create(properties);

		for(int i=0; i<30000; i++) {
			Thread.sleep(1000L);
			System.out.println(idGenerator.generate());
		}

	}

}
