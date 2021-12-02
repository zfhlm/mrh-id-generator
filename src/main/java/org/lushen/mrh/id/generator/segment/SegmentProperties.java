package org.lushen.mrh.id.generator.segment;

import java.time.Duration;

/**
 * 号段 ID 生成器配置
 * 
 * @author hlm
 */
public class SegmentProperties {

	/**
	 * 创建一份默认配置
	 * 
	 * @return
	 */
	public static final SegmentProperties buildDefault() {
		SegmentProperties properties = new SegmentProperties();
		properties.setRange(10000);
		properties.setThreshold(50);
		properties.setInterval(Duration.ofSeconds(1L));
		return properties;
	}

	protected int range;				// 每次拉取号段长度

	protected int threshold;			// 预加载阈值百分比，范围(0,100)，到达指定阈值后，预加载新的号段到缓存

	protected Duration interval;		// 预加载检测时间间隔

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
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
		builder.append("[range=");
		builder.append(range);
		builder.append(", threshold=");
		builder.append(threshold);
		builder.append(", interval=");
		builder.append(interval);
		builder.append("]");
		return builder.toString();
	}

}
