package org.lushen.mrh.id.generator.revision;

import java.time.Duration;

/**
 * revision 持久化接口，实现类必须解决并发冲突问题
 * 
 * @author hlm
 */
public interface RevisionRepository {

	/**
	 * 获取可用节点
	 * 
	 * @param begin			当前时间戳
	 * @param timeToLive	获取时长
	 * @return not null
	 */
	public RevisionNode next(long begin, Duration timeToLive);

	/**
	 * 延时指定节点，expired 不一致必须抛出异常
	 * 
	 * @param workerId		当前节点ID
	 * @param expired		当前节点过期时间戳
	 * @param timeToLive	当前节点延时时长
	 * @return not null
	 */
	public RevisionNode delay(int workerId, long expired, Duration timeToLive);

}
