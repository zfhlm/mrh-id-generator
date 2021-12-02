package org.lushen.mrh.id.generator.segment;

/**
 * 号段持久化仓库接口
 * 
 * @author hlm
 */
public interface SegmentRepository {

	/**
	 * 获取下一个号段
	 * 
	 * @param range 号段长度
	 */
	public Segment next(int range);

}
