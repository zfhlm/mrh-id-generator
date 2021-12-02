package org.lushen.mrh.id.generator.segment.mysql;

import org.apache.commons.dbcp2.BasicDataSource;
import org.lushen.mrh.id.generator.segment.achieve.SegmentMysqlJdbcRepository;

public class TestSegmentMysqlRepository {

	public static void main(String[] args) {

		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		dataSource.setUsername("root");
		dataSource.setPassword("123456");
		dataSource.setUrl("jdbc:mysql://192.168.140.210:3306/test?useUnicode=true&characterEncoding=UTF-8&serverTimezone=GMT%2B8");
		SegmentMysqlJdbcRepository repository = new SegmentMysqlJdbcRepository("test", dataSource);

		System.out.println("第1个号段：" + repository.next(10000));
		System.out.println("第2个号段：" + repository.next(10000));
		System.out.println("第3个号段：" + repository.next(1000));

	}

}
