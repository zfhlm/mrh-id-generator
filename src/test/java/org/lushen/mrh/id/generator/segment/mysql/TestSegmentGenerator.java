package org.lushen.mrh.id.generator.segment.mysql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.lushen.mrh.id.generator.segment.SegmentIdGenerator;
import org.lushen.mrh.id.generator.segment.SegmentProperties;
import org.lushen.mrh.id.generator.segment.achieve.DefaultSegmentIdGeneratorFactory;
import org.lushen.mrh.id.generator.segment.achieve.SegmentMysqlJdbcRepository;

public class TestSegmentGenerator {

	public static void main(String[] args) throws Exception {

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		dataSource.setUrl("jdbc:mysql://192.168.140.210:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8");
		SegmentMysqlJdbcRepository repository = new SegmentMysqlJdbcRepository("test", dataSource);

		SegmentProperties properties = SegmentProperties.buildDefault();
		properties.setThreshold(20);
		properties.setRange(10);
		SegmentIdGenerator generator = new DefaultSegmentIdGeneratorFactory(repository).create(properties);

		for(int i=0; i<60; i++) {
			Thread.sleep(1000L);
			System.out.println(generator.generate());
		}
	}

}
