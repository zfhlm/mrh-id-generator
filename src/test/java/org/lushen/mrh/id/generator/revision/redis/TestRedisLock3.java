package org.lushen.mrh.id.generator.revision.redis;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.locks.Lock;

import org.lushen.mrh.id.generator.supports.RedisLock;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class TestRedisLock3 {

	public static void main(String[] args) {

		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
		connectionFactory.afterPropertiesSet();

		String lockKey = "test.abc";
		Duration lockTimeout = Duration.ofSeconds(10);

		for(int i=0; i<2; i++) {

			final int index = i;
			new Thread(() -> {

				Lock lock = new RedisLock(() -> connectionFactory.getConnection(), conn -> conn.close(), lockKey, lockTimeout);

				lock.lock();

				System.out.println("index : " + index);

				RedisConnection connection = connectionFactory.getConnection();
				System.out.println(new String(connection.get(lockKey.getBytes(StandardCharsets.UTF_8))));
				connection.close();

				try {
					Thread.sleep(20000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				lock.unlock();

			}).start();

		}

	}

}
