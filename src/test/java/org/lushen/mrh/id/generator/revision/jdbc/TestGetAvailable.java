package org.lushen.mrh.id.generator.revision.jdbc;

import java.time.Duration;

import org.apache.commons.dbcp2.BasicDataSource;
import org.lushen.mrh.id.generator.revision.RevisionWorker;
import org.lushen.mrh.id.generator.revision.achieve.RevisionMysqlJdbcRepository;

public class TestGetAvailable {

	public static void main(String[] args) throws Exception {

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		dataSource.setUrl("jdbc:mysql://192.168.140.210:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8");

		RevisionMysqlJdbcRepository repository = new RevisionMysqlJdbcRepository(dataSource);
		RevisionWorker node = repository.obtain("test", System.currentTimeMillis(), Duration.ofMinutes(10));
		System.out.println(node);

		dataSource.close();

	}

}
