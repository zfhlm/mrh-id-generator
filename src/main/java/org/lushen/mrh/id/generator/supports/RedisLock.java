package org.lushen.mrh.id.generator.supports;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.types.Expiration;

/**
 * redis 分布式锁
 * 
 * @author hlm
 */
public class RedisLock implements Lock {

	private final byte[] lockKey;						// 锁key

	private final byte[] lockValue;						// 锁value

	private final Duration lockTimeout;					// 锁超时时间(防死锁)

	private final Supplier<RedisConnection> doGet;		// 获取连接方法

	private final Consumer<RedisConnection> doRelease;	// 释放连接方法

	public RedisLock(Supplier<RedisConnection> doGet, Consumer<RedisConnection> doRelease, String lockKey, Duration lockTimeout) {
		super();
		this.lockKey = lockKey.getBytes(StandardCharsets.UTF_8);
		this.lockValue = UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8);
		this.lockTimeout = lockTimeout;
		this.doGet = doGet;
		this.doRelease = doRelease;
	}

	@Override
	public void lock() {
		for(;;) {
			if(tryLock()) {
				break;
			}
		}
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		for(;;) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if(tryLock()) {
				break;
			}
		}
	}

	@Override
	public boolean tryLock() {
		RedisConnection connection = this.doGet.get();
		try {
			return connection.set(this.lockKey, this.lockValue, Expiration.from(this.lockTimeout), SetOption.ifAbsent());
		} finally {
			this.doRelease.accept(connection);
		}
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		long start = System.currentTimeMillis();
		for(;;) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if(tryLock()) {
				return true;
			}
			if(System.currentTimeMillis() - start > unit.toMillis(time)) {
				return false;
			}
		}
	}

	@Override
	public void unlock() {
		RedisConnection connection = this.doGet.get();
		try {
			byte[] remoteValue = connection.get(this.lockKey);
			if(remoteValue != null && Arrays.equals(this.lockValue, remoteValue)) {
				connection.del(this.lockKey);
			}
		} finally {
			this.doRelease.accept(connection);
		}
	}

	@Override
	public Condition newCondition() {
		throw new RuntimeException("Not supported method !");
	}

}
