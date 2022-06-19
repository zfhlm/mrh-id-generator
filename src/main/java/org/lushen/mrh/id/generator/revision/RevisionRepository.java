package org.lushen.mrh.id.generator.revision;

import java.time.Duration;

import org.lushen.mrh.id.generator.revision.RevisionException.RevisionMatchFailureException;
import org.lushen.mrh.id.generator.revision.RevisionTarget.RevisionAvailable;

/**
 * revision 持久化接口，实现类必须解决并发冲突问题
 * 
 * @author hlm
 */
public interface RevisionRepository {

	/**
	 * 获取一个可用节点
	 * 
	 * @param namespace		业务命名空间
	 * @param timeToLive	获取时长
	 * @return
	 * @throws RevisionException 获取节点异常
	 */
	public RevisionAvailable attempt(String namespace, Duration timeToLive) throws RevisionException;

	/**
	 * 
	 * 获取指定节点（与数据源存储的 expiredAt 进行对比，相同则获取该节点）
	 * 
	 * @param namespace		业务命名空间
	 * @param timeToLive	获取时长
	 * @param target		目标节点
	 * @return
	 * @throws RevisionException 获取节点异常
	 * @throws RevisionMatchFailureException 获取节点 expired 匹配异常
	 */
	public RevisionAvailable attempt(String namespace, Duration timeToLive, RevisionTarget target) throws RevisionException, RevisionMatchFailureException;

}
