package org.lushen.mrh.id.generator.revision;

import java.time.Duration;

/**
 * revision 持久化接口，实现类必须解决并发冲突问题
 * 
 * @author hlm
 */
public interface RevisionRepository {

	/**
	 * 根据匹配时间戳，获取一个可用节点
	 * 
	 * @param namespace				业务命名空间
	 * @param macthingAt			匹配时间戳
	 * @param timeToLive			使用时长
	 * @param workerIds				期望获取的节点(如果期望节点可用，优先选择期望节点)
	 * @return
	 */
	public RevisionWorker obtain(String namespace, long macthingAt, Duration timeToLive, int... workerIds);

}
