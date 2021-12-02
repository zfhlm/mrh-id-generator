package org.lushen.mrh.id.generator.revision;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * revision 配置
 * 
 * @author hlm
 */
public class RevisionProperties {

	/**
	 * snowflake epoch 日期常量
	 */
	private static final String SNOWFLAKE_EPOCH_DEFAULT = "2021-11-01";

	/**
	 * 创建一份默认配置
	 * 
	 * @return
	 */
	public static final RevisionProperties buildDefault() {
		RevisionProperties properties = new RevisionProperties();
		properties.setEpochDate(LocalDate.parse(SNOWFLAKE_EPOCH_DEFAULT, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		properties.setTimeToLive(Duration.ofMinutes(10));
		properties.setThreshold(80);
		properties.setInterval(Duration.ofSeconds(1));
		return properties;
	}

	protected LocalDate epochDate;	// 系统上线日期

	protected Duration timeToLive;	// 设定可用时长(初始时长、延时时长)

	protected int threshold;		// 设定延时阈值百分比，范围(0,100)，到达指定阈值后，延长工作时长

	protected Duration interval;	// 延时检测时间间隔

	public LocalDate getEpochDate() {
		return epochDate;
	}

	public void setEpochDate(LocalDate epochDate) {
		this.epochDate = epochDate;
	}

	public Duration getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(Duration timeToLive) {
		this.timeToLive = timeToLive;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public Duration getInterval() {
		return interval;
	}

	public void setInterval(Duration interval) {
		this.interval = interval;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[epochDate=");
		builder.append(epochDate);
		builder.append(", timeToLive=");
		builder.append(timeToLive);
		builder.append(", threshold=");
		builder.append(threshold);
		builder.append(", interval=");
		builder.append(interval);
		builder.append("]");
		return builder.toString();
	}

}
