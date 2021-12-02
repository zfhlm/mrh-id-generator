package org.lushen.mrh.id.generator.revision;

/**
 * 工作节点
 * 
 * @author hlm
 */
public class RevisionNode {

	private final int workerId;		// 工作节点ID

	private final long expired;		// 过期时间

	public RevisionNode(int workerId, long expired) {
		super();
		this.workerId = workerId;
		this.expired = expired;
	}

	public int getWorkerId() {
		return workerId;
	}

	public long getExpired() {
		return expired;
	}

	@Override
	public int hashCode() {
		return this.workerId;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RevisionNode) {
			return ((RevisionNode) obj).getWorkerId() == this.workerId;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[workerId=");
		builder.append(workerId);
		builder.append(", expired=");
		builder.append(expired);
		builder.append("]");
		return builder.toString();
	}

}
