package org.lushen.mrh.id.generator.revision;

import java.io.Serializable;
import java.util.Date;

/**
 * revision 持久化信息定义
 * 
 * @author hlm
 */
public class RevisionEntity implements Serializable {

	private static final long serialVersionUID = 446399776533159057L;

	private String namespace;				// 业务命名空间

	private int workerId;					// 工作节点ID

	private long lastTimestamp;				// 已被使用的最后时间戳

	private Date createTime;				// 创建
	
	private Date modifyTime;				// 更新时间

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

	public long getLastTimestamp() {
		return lastTimestamp;
	}

	public void setLastTimestamp(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[namespace=");
		builder.append(namespace);
		builder.append(", workerId=");
		builder.append(workerId);
		builder.append(", lastTimestamp=");
		builder.append(lastTimestamp);
		builder.append(", createTime=");
		builder.append(createTime);
		builder.append(", modifyTime=");
		builder.append(modifyTime);
		builder.append("]");
		return builder.toString();
	}

}
