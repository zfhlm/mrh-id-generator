package org.lushen.mrh.id.generator.snowflake;

import org.lushen.mrh.id.generator.IdGenerator;

/**
 * snowflake ID 生成器
 * 
 * +------------------+------------------+------------------+------------------+------------------+
 * +      1 bit       +      41 bit      +      5 bit       +      5 bit       +      12 bit      +
 * +------------------+------------------+------------------+------------------+------------------+
 * +     固定取整          +    毫秒时间戳        +   数据中心节点       +     工作节点          +     计数序列号      +
 * +------------------+------------------+------------------+------------------+------------------+
 * 
 * @author hlm
 */
public class SnowflakeIdGenerator implements IdGenerator {

	// 计数序列号 bit 位数
	private static final long sequenceBits = 12L;

	// 工作节点 bit 位数
	private static final long workerIdBits = 5L;

	// 数据中心节点 bit 位数
	private static final long centerIdBits = 5L;

	// 工作节点 左移 bit 位数
	private static final long workerIdLeftShift = sequenceBits;

	// 数据中心节点 左移 bit 位数
	private static final long datacenterIdLeftShift = sequenceBits + workerIdBits;

	// 毫秒时间戳 左移 bit 位数
	private static final long timestampLeftShift = sequenceBits + workerIdBits + centerIdBits;

	// 最大计数序列号
	public static final long maxSequence = -1L ^ (-1L << sequenceBits);

	// 最大工作节点
	public static final long maxWorkerId = -1L ^ (-1L << workerIdBits);

	// 最大数据中心节点
	public static final long maxCenterId = -1L ^ (-1L << centerIdBits);

	private long epochAt;

	private int dataCenterId;

	private int workerId;

	private long sequence = 0L;

	private long lastTimestamp;

	/**
	 * snowflake ID 生成器
	 * 
	 * @param epochAt			系统上线日期时间戳，一旦指定使用后不可改变
	 * @param dataCenterId		数据中心节点ID
	 * @param workerId			工作节点ID
	 */
	SnowflakeIdGenerator(long epochAt, int dataCenterId, int workerId) {
		super();
		if(epochAt <= 0) {
			throw new IllegalArgumentException("epochAt can't be less than or equal to 0");
		}
		if (dataCenterId > maxCenterId || dataCenterId < 0) {
			throw new IllegalArgumentException(String.format("dataCenterId can't be greater than %d or less than 0", maxCenterId));
		}
		if (workerId > maxWorkerId || workerId < 0) {
			throw new IllegalArgumentException(String.format("workerId can't be greater than %d or less than 0", maxWorkerId));
		}
		this.epochAt = epochAt;
		this.dataCenterId = dataCenterId;
		this.workerId = workerId;
	}

	@Override
	public synchronized long generate() {

		long timestamp = timeGen();

		// 发生时钟回拨，直接抛出异常
		if (timestamp < lastTimestamp) {
			throw new RuntimeException("clock move back !");
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
		return ((timestamp - epochAt) << timestampLeftShift) | (dataCenterId << datacenterIdLeftShift) | (workerId << workerIdLeftShift) | sequence;
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
	
}
