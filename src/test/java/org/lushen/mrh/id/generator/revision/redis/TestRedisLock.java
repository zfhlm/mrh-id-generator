package org.lushen.mrh.id.generator.revision.redis;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.locks.Lock;

import org.lushen.mrh.id.generator.supports.RedisLock;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class TestRedisLock {
	
	public static void main(String[] args) {
		
		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
		connectionFactory.afterPropertiesSet();

		String lockKey = "test.abc";
		Duration lockTimeout = Duration.ofSeconds(60);
		Lock lock = new RedisLock(() -> connectionFactory.getConnection(), conn -> conn.close(), lockKey, lockTimeout);
		
		lock.lock();
		
		System.out.println("-======================-");
		
		RedisConnection connection = connectionFactory.getConnection();
		System.out.println(new String(connection.get(lockKey.getBytes(StandardCharsets.UTF_8))));
		connection.close();
		
		try {
			Thread.sleep(20000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		lock.unlock();
		
	}

}
