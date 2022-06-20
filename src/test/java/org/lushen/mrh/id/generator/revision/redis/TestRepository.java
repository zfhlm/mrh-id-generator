package org.lushen.mrh.id.generator.revision.redis;

import java.time.Duration;

import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.achieve.RevisionRedisRepository;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class TestRepository {

	public static void main(String[] args) throws InterruptedException {

		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
		connectionFactory.afterPropertiesSet();

		RevisionRepository repository = new RevisionRedisRepository(connectionFactory);

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
