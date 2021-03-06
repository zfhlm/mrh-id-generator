package org.lushen.mrh.id.generator.segment.redis;

import org.lushen.mrh.id.generator.segment.SegmentIdGenerator;
import org.lushen.mrh.id.generator.segment.SegmentIdGeneratorFactory;
import org.lushen.mrh.id.generator.segment.SegmentProperties;
import org.lushen.mrh.id.generator.segment.achieve.SegmentRedisRepository;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class TestSegmentGenerator {

	public static void main(String[] args) throws Exception {

		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
		connectionFactory.afterPropertiesSet();
		SegmentRedisRepository repository = new SegmentRedisRepository(connectionFactory);

		SegmentProperties properties = SegmentProperties.buildDefault();
		properties.setRemaining(2);
		properties.setRange(10);
		SegmentIdGenerator generator = new SegmentIdGeneratorFactory(repository).create(properties);

		for(int i=0; i<60; i++) {
			Thread.sleep(1000L);
			System.out.println(generator.generate());
		}
	}

}
