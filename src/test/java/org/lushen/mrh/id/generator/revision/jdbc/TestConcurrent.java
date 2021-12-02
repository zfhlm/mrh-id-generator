package org.lushen.mrh.id.generator.revision.jdbc;

import java.time.Duration;

import org.apache.commons.dbcp2.BasicDataSource;
import org.lushen.mrh.id.generator.revision.RevisionIdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.achieve.AutoDelayRevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.achieve.RevisionMysqlJdbcRepository;

public class TestConcurrent {

	public static void main(String[] args) throws Exception {

		// 可用时长 10 秒钟
		RevisionProperties properties = RevisionProperties.buildDefault();
		properties.setTimeToLive(Duration.ofSeconds(10L));
		properties.setRemainingTimeToDelay(Duration.ofSeconds(3));

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		dataSource.setUrl("jdbc:mysql://192.168.140.210:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8");
		RevisionMysqlJdbcRepository repository = new RevisionMysqlJdbcRepository(dataSource);

		// 并发创建
		for(int index=0; index<150; index++) {

			new Thread(() -> {
				RevisionIdGenerator idGenerator = new AutoDelayRevisionIdGeneratorFactory(repository).create(properties);
				for(int i=0; i<30000; i++) {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(i % 5 == 0) {
						System.out.println(idGenerator.generate());
					}
				}
			}).start();

		}

	}

}
