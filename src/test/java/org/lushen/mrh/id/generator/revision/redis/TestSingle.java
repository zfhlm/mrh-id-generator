package org.lushen.mrh.id.generator.revision.redis;

import java.time.Duration;

import org.lushen.mrh.id.generator.revision.RevisionIdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.achieve.AutoDelayRevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.achieve.RevisionRedisRepository;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class TestSingle {

	public static void main(String[] args) throws Exception {
		
		
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		
		LettuceClientConfiguration clientConfiguration = LettuceClientConfiguration
				.builder()
				.commandTimeout(Duration.ofSeconds(1))
				.build();

		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration, clientConfiguration);
		connectionFactory.afterPropertiesSet();

		// 测试，可用时长 10 秒钟
		RevisionProperties properties = RevisionProperties.buildDefault();
		properties.setTimeToLive(Duration.ofSeconds(10L));
		properties.setRemainingTimeToDelay(Duration.ofSeconds(3));

		RevisionRepository repository = new RevisionRedisRepository(connectionFactory);

		RevisionIdGenerator idGenerator = new AutoDelayRevisionIdGeneratorFactory(repository).create(properties);

		for(int i=0; i<30000; i++) {
			Thread.sleep(1000L);
			System.out.println(idGenerator.generate());
		}

	}

}
