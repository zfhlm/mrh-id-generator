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

	private static final String DEFAULT_NAMESPACE = "default";

	private static final LocalDate DEFAULT_EPOCH_DATE = LocalDate.parse("2021-11-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));

	private static final Duration DEFAULT_TIME_TO_LIVE = Duration.ofMinutes(10);

	private static final Duration DEFAULT_REMAINING_TIME_TO_DELAY = Duration.ofMinutes(1);

	/**
	 * 创建一份默认配置
	 * 
	 * @return
	 */
	public static final RevisionProperties buildDefault() {
		RevisionProperties properties = new RevisionProperties();
		properties.setNamespace(DEFAULT_NAMESPACE);
		properties.setEpochDate(DEFAULT_EPOCH_DATE);
		properties.setTimeToLive(DEFAULT_TIME_TO_LIVE);
		properties.setRemainingTimeToDelay(DEFAULT_REMAINING_TIME_TO_DELAY);
		return properties;
	}

	protected String namespace;							// 业务命名空间

	protected LocalDate epochDate;						// 系统上线日期

	protected Duration timeToLive;						// 可用时长(初始时长、延时时长)

	protected Duration remainingTimeToDelay;			// 剩余多少时长触发延时

	protected RevisionProperties() {
		super();
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

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
		builder.append("[namespace=");
		builder.append(namespace);
		builder.append(", epochDate=");
		builder.append(epochDate);
		builder.append(", timeToLive=");
		builder.append(timeToLive);
		builder.append(", remainingTimeToDelay=");
		builder.append(remainingTimeToDelay);
		builder.append("]");
		return builder.toString();
	}

}
