package org.lushen.mrh.id.generator.revision.redis;

import java.time.Duration;

import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

public class TestRedisTemplate {

	public static void main(String[] args) {

		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
		connectionFactory.afterPropertiesSet();

		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(connectionFactory);
		redisTemplate.afterPropertiesSet();

		new Thread(() -> {

			System.out.println(redisTemplate.opsForValue().setIfAbsent("test", "ttt1", Duration.ofSeconds(10)));
			System.out.println("1111111111111111111");

		}).start();

		new Thread(() -> {

			System.out.println(redisTemplate.opsForValue().setIfAbsent("test", "ttt2", Duration.ofSeconds(10)));
			System.out.println("22222222222222222222");

		}).start();

	}

}
