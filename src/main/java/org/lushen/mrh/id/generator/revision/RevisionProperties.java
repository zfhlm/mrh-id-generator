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
		properties.setRemainingTimeToDelay(Duration.ofMinutes(1));
		return properties;
	}

	protected LocalDate epochDate;				// 系统上线日期

	protected Duration timeToLive;				// 可用时长(初始时长、延时时长)

	protected Duration remainingTimeToDelay;	// 剩余多少时长触发延时

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

	public Duration getRemainingTimeToDelay() {
		return remainingTimeToDelay;
	}

	public void setRemainingTimeToDelay(Duration remainingTimeToDelay) {
		this.remainingTimeToDelay = remainingTimeToDelay;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[epochDate=");
		builder.append(epochDate);
		builder.append(", timeToLive=");
		builder.append(timeToLive);
		builder.append(", remainingTimeToDelay=");
		builder.append(remainingTimeToDelay);
		builder.append("]");
		return builder.toString();
	}

}
