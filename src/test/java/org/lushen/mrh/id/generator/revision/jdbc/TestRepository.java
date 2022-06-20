package org.lushen.mrh.id.generator.revision.jdbc;

import java.time.Duration;

import org.apache.commons.dbcp2.BasicDataSource;
import org.lushen.mrh.id.generator.revision.achieve.RevisionMysqlJdbcRepository;

public class TestRepository {

	public static void main(String[] args) throws InterruptedException {

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		dataSource.setUrl("jdbc:mysql://192.168.140.210:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8");

		RevisionMysqlJdbcRepository repository = new RevisionMysqlJdbcRepository(dataSource);

		while(true) {
			Thread.sleep(1000L);
			repository.obtain("test", System.currentTimeMillis(), Duration.ofSeconds(10));
			repository.obtain("test", System.currentTimeMillis(), Duration.ofSeconds(10));
			repository.obtain("test", System.currentTimeMillis(), Duration.ofSeconds(10));
			repository.obtain("test", System.currentTimeMillis(), Duration.ofSeconds(10));
			repository.obtain("test", System.currentTimeMillis(), Duration.ofSeconds(10));
		}

	}

}
