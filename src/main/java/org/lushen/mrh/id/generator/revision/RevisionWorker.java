package org.lushen.mrh.id.generator.revision;

/**
 * revision 工作节点信息
 * 
 * @author hlm
 */
public class RevisionWorker {

	private String namespace;		// 业务命名空间

	private int workerId;			// 工作节点ID

	private long beginAt;			// 有效开始时间

	private long expiredAt;			// 有效结束时间

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public int getWorkerId() {
		return workerId;
	}

	public void setWorkerId(int workerId) {
		this.workerId = workerId;
	}

	public long getBeginAt() {
		return beginAt;
	}

	public void setBeginAt(long beginAt) {
		this.beginAt = beginAt;
	}

	public long getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(long expiredAt) {
		this.expiredAt = expiredAt;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[namespace=");
		builder.append(namespace);
		builder.append(", workerId=");
		builder.append(workerId);
		builder.append(", beginAt=");
		builder.append(beginAt);
		builder.append(", expiredAt=");
		builder.append(expiredAt);
		builder.append("]");
		return builder.toString();
	}

}
