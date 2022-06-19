package org.lushen.mrh.id.generator.segment;

/**
 * 号段 ID 生成器配置
 * 
 * @author hlm
 */
public class SegmentProperties {

	private static final String DEFAULT_NAMESPACE = "default";

	private static final int DEFAULT_RANGE = 10000;

	private static final int DEFAULT_REMAINING = 5000;

	/**
	 * 创建一份默认配置
	 * 
	 * @return
	 */
	public static final SegmentProperties buildDefault() {
		SegmentProperties properties = new SegmentProperties();
		properties.setNamespace(DEFAULT_NAMESPACE);
		properties.setRange(DEFAULT_RANGE);
		properties.setRemaining(DEFAULT_REMAINING);
		return properties;
	}

	protected String namespace;				// 业务命名空间

	protected int range;					// 每次拉取号段长度

	protected int remaining;				// 剩余多少号段进行预加载

	protected SegmentProperties() {
		super();
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

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
		builder.append("[namespace=");
		builder.append(namespace);
		builder.append(", range=");
		builder.append(range);
		builder.append(", remaining=");
		builder.append(remaining);
		builder.append("]");
		return builder.toString();
	}

}
