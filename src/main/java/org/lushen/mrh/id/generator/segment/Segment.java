package org.lushen.mrh.id.generator.segment;

/**
 * 号段
 * 
 * @author hlm
 */
public final class Segment {

	private final long min;		// 号段最小ID，包含此值

	private final long max;		// 号段最大ID，包含此值

	public Segment(long min, long max) {
		super();
		this.min = min;
		this.max = max;
	}

	public long getMin() {
		return min;
	}

	public long getMax() {
		return max;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[min=");
		builder.append(min);
		builder.append(", max=");
		builder.append(max);
		builder.append("]");
		return builder.toString();
	}

}
