package org.lushen.mrh.id.generator.segment;

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
		properties.setRemaining(5000);
		return properties;
	}

	protected int range;				// 每次拉取号段长度

	protected int remaining;			// 剩余多少号段进行预加载

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public int getRemaining() {
		return remaining;
	}

	public void setRemaining(int remaining) {
		this.remaining = remaining;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[range=");
		builder.append(range);
		builder.append(", remaining=");
		builder.append(remaining);
		builder.append("]");
		return builder.toString();
	}

}
