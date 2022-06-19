package org.lushen.mrh.id.generator.segment.redis;

import org.lushen.mrh.id.generator.segment.achieve.SegmentRedisRepository;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class TestSegmentRedisRepository {

	public static void main(String[] args) {

		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
		connectionFactory.afterPropertiesSet();

		SegmentRedisRepository repository = new SegmentRedisRepository(connectionFactory);

		System.out.println("第1个号段：" + repository.next("test2", 10000));
		System.out.println("第2个号段：" + repository.next("test2", 10000));
		System.out.println("第3个号段：" + repository.next("test2", 1000));

	}

}
