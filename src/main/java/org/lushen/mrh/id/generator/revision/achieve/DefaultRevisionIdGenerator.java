package org.lushen.mrh.id.generator.revision.achieve;

import org.lushen.mrh.id.generator.revision.RevisionIdGenerator;

/**
 * revision ID 生成器实现类
 * 
 * @author hlm
 */
public class DefaultRevisionIdGenerator implements RevisionIdGenerator {

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
	public DefaultRevisionIdGenerator(long epochAt, int workerId, long beginAt, long expiredAt) {
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

	@Override
	public synchronized long generate() {

		long timestamp = timeGen();

		// 不在可用时段内，返回 -1 由外部自行处理
		if(timestamp < beginAt || timestamp > expiredAt) {
			return -1L;
		}

		// 发生时钟回拨，滚动次数加 1，如果达到最大次数，返回 -1 由外部自行处理
		if (timestamp < lastTimestamp) {
			if(moveBack == maxMoveBack) {
				return -1L;
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

	/**
	 * 推迟过期时间
	 * 
	 * @param expiredAt
	 */
	public void delay(long expiredAt) {
		if(this.expiredAt < expiredAt) {
			this.expiredAt = expiredAt;
		}
	}

}
