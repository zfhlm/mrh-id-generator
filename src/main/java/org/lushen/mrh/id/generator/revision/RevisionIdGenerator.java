package org.lushen.mrh.id.generator.revision;

import static org.lushen.mrh.id.generator.revision.RevisionIdGenerator.ActualRevisionIdGenerator.expiredFlagSequence;

import java.time.ZoneOffset;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.IdGenerator;

/**
 * revision ID 生成器，基于 snowflake 的变种生成器，指定可用时段，并实现以下结构：
 * 
 * +------------------+------------------+------------------+------------------+------------------+
 * +      1 bit       +      41 bit      +     10 bit       +      2 bit       +      10 bit      +
 * +------------------+------------------+------------------+------------------+------------------+
 * +     固定取整          +    毫秒时间戳        +     工作节点          + 时钟回拨滚动次数    +     计数序列号      +
 * +------------------+------------------+------------------+------------------+------------------+
 * 
 * @author hlm
 */
public class RevisionIdGenerator extends RevisionProperties implements IdGenerator {

	private final Log log = LogFactory.getLog(RevisionIdGenerator.class.getSimpleName());

	private final RevisionRepository repository;		// 持久化接口

	private ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 1, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});													// daemon线程池

	private ActualRevisionIdGenerator curr;				// 当前 ID 生成器

	private ActualRevisionIdGenerator back;				// 备用 ID 生成器

	RevisionIdGenerator(RevisionRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public synchronized long generate() {

		// ID 正常，直接返回
		long id = this.curr.generate();
		if(id >= 0L) {
			return id;
		}

		// 生成器不可用
		synchronized (this.executor) {

			// 生成器已过期，且存在备用生成器，直接切换
			if(id == expiredFlagSequence && this.back != null) {
				this.curr = this.back;
				this.back = null;
			}
			// 其他异常情况，同步创建生成器
			else {
				long epochAt = this.epochDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
				RevisionWorker worker = this.repository.obtain(this.namespace, System.currentTimeMillis(), this.timeToLive);
				this.curr = new ActualRevisionIdGenerator(epochAt, worker.getWorkerId(), worker.getBeginAt(), worker.getExpiredAt());
				this.back = null;
			}

			// 重新生成ID
			id = this.curr.generate();
			if(id >= 0L) {
				return id;
			}

			// 再次生成的 ID 不正常，直接抛出异常
			throw new RevisionException("Generate ID error");

		}

	}

	public void initialize() {

		// 初始化 ID 生成器
		if(this.curr == null) {
			long epochAt = this.epochDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
			RevisionWorker worker = this.repository.obtain(this.namespace, System.currentTimeMillis(), this.timeToLive);
			this.curr = new ActualRevisionIdGenerator(epochAt, worker.getWorkerId(), worker.getBeginAt(), worker.getExpiredAt());
		}

		// 启动调度任务，当前生成器剩余可用时长到达阈值，创建备用生成器
		schedule(() -> {
			synchronized (this.executor) {
				if(this.back == null && this.curr.getExpiredAt() - System.currentTimeMillis() <= this.remainingTimeToDelay.toMillis()) {
					try {
						long epochAt = this.epochDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
						RevisionWorker worker = this.repository.obtain(this.namespace, this.curr.getExpiredAt()+1L, this.timeToLive, this.curr.getWorkerId());
						this.back = new ActualRevisionIdGenerator(epochAt, worker.getWorkerId(), worker.getBeginAt(), worker.getExpiredAt());
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		});

	}

	private void schedule(Runnable command) {
		this.executor.execute(() -> {
			try {
				while(true) {
					Thread.sleep(100L);
					command.run();
				}
			} catch (Exception e) {
				log.warn("Restart scheduler, because of " + e.getMessage());
				schedule(command);
			}
		});
	}

	/**
	 * revision ID 生成器实现
	 * 
	 * @author hlm
	 */
	public static class ActualRevisionIdGenerator implements IdGenerator {

		// 生成器不可用标识序列
		public static final long unavailableFlagSequence = -1L;

		// 生成器已过期标识序列
		public static final long expiredFlagSequence = -2L;

		// 计数序列号 bit 位数
		private static final long sequenceBits = 10L;

		// 时钟回拨滚动次数 bit 位数
		private static final long moveBackBits = 2L;

		// 工作节点 bit 位数
		private static final long workerIdBits = 10L;

		// 时钟回拨滚动次数 左移 bit 位数
		private static final long moveBackShift = sequenceBits;

		// 工作节点 左移 bit 位数
		private static final long workerIdShift = sequenceBits + moveBackBits;

		// 毫秒时间戳 左移 bit 位数
		private static final long timestampLeftShift = sequenceBits + moveBackBits + workerIdBits;

		// 最大计数序列号
		private static final long maxSequence = -1L ^ (-1L << sequenceBits);

		// 最大时钟回拨滚动次数
		private static final long maxMoveBack = -1L ^ (-1L << moveBackBits);

		// 最大工作节点
		public static final long maxWorkerId = -1L ^ (-1L << workerIdBits);

		private long epochAt;				// 系统上线日期时间戳

		private int workerId;				// 工作节点

		private int moveBack = 0;			// 回拨滚动次数

		private long sequence = 0L;			// 计数序列号

		private long lastTimestamp;			// 最后一次使用时间戳

		private long beginAt;				// 可用开始时间戳

		private long expiredAt;				// 可用过期时间戳

		/**
		 * revision ID 生成器
		 * 
		 * @param epochAt		系统上线日期时间戳，一旦指定使用后不可改变
		 * @param workerId		工作节点ID，范围 [0, 1024)
		 * @param beginAt		可用开始时间戳
		 * @param expiredAt		可用过期时间戳
		 */
		public ActualRevisionIdGenerator(long epochAt, int workerId, long beginAt, long expiredAt) {
			super();
			if(epochAt <= 0) {
				throw new IllegalArgumentException("epochAt can't be less than or equal to 0");
			}
			if (workerId > maxWorkerId || workerId < 0) {
				throw new IllegalArgumentException(String.format("workerId can't be greater than %d or less than 0", maxWorkerId));
			}
			if(beginAt >= expiredAt) {
				throw new IllegalArgumentException(String.format("beginAt %s can't be greater than or equal to expiredAt %d", beginAt, expiredAt));
			}
			this.epochAt = epochAt;
			this.workerId = workerId;
			this.beginAt = beginAt;
			this.expiredAt = expiredAt;
			this.lastTimestamp = beginAt;
		}

		/**
		 * 生成唯一序列 ID
		 * 
		 * @return 生成返回 [0, ∞)，失败返回 {{@link beforeBeginAtFlagSequence}, {@link afterExpiredFlagSequence}, {@link maxMoveBackFlagSequence}}
		 */
		@Override
		public synchronized long generate() {

			long timestamp = timeGen();

			// 当前时间在可用时间之前，返回不可用标识序列
			if(timestamp < beginAt) {
				return unavailableFlagSequence;
			}

			// 当前时间在可用时间之后，返回已过期标识序列
			if(timestamp > expiredAt) {
				return expiredFlagSequence;
			}

			// 发生时钟回拨，滚动次数加 1，如果达到最大次数，返回不可用标识序列
			if (timestamp < lastTimestamp) {
				if(moveBack == maxMoveBack) {
					return unavailableFlagSequence;
				} else {
					moveBack += 1;
				}
			}

			// 当前毫秒产生的ID不足，阻塞到下一毫秒
			if (lastTimestamp == timestamp) {
				sequence = (sequence + 1) & maxSequence;
				if (sequence == 0) {
					timestamp = tilNextMillis(lastTimestamp);
				}
			} else {
				sequence = 0L;
			}

			// 更新最后一次时间
			lastTimestamp = timestamp;

			// 生成并返回唯一序列
			return ((timestamp - epochAt) << timestampLeftShift) | (workerId << workerIdShift) | (moveBack << moveBackShift) | sequence;
		}

		private long tilNextMillis(long lastTimestamp) {
			long timestamp = timeGen();
			while (timestamp <= lastTimestamp) {
				timestamp = timeGen();
			}
			return timestamp;
		}

		private long timeGen() {
			return System.currentTimeMillis();
		}

		/**
		 * 获取当前工作节点ID
		 * 
		 * @return
		 */
		public int getWorkerId() {
			return workerId;
		}

		/**
		 * 获取开始时间戳
		 * 
		 * @return
		 */
		public long getBeginAt() {
			return this.beginAt;
		}

		/**
		 * 获取过期时间戳
		 * 
		 * @return
		 */
		public long getExpiredAt() {
			return this.expiredAt;
		}

	}

}
