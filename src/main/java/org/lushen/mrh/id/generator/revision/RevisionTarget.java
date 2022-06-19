package org.lushen.mrh.id.generator.revision;

/**
 * 目标节点信息对象
 * 
 * @author hlm
 */
public class RevisionTarget {

	protected final int workerId;			// 工作节点ID

	protected final long expiredAt;			// 可用到期时间

	public RevisionTarget(int workerId, long expiredAt) {
		super();
		this.workerId = workerId;
		this.expiredAt = expiredAt;
	}

	public int getWorkerId() {
		return workerId;
	}

	public long getExpiredAt() {
		return expiredAt;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[workerId=");
		builder.append(workerId);
		builder.append(", expiredAt=");
		builder.append(expiredAt);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * 可用节点信息对象
	 * 
	 * @author hlm
	 */
	public static class RevisionAvailable extends RevisionTarget {

		private final long beginAt;			// 可用开始时间

		public RevisionAvailable(int workerId, long beginAt, long expiredAt) {
			super(workerId, expiredAt);
			this.beginAt = beginAt;
		}

		public long getBeginAt() {
			return beginAt;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("[workerId=");
			builder.append(workerId);
			builder.append(", beginAt=");
			builder.append(beginAt);
			builder.append(", expiredAt=");
			builder.append(expiredAt);
			builder.append("]");
			return builder.toString();
		}

	}

}
