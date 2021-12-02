package org.lushen.mrh.id.generator;

/**
 * 序列 ID 生成器
 * 
 * @author hlm
 */
public interface IdGenerator {

	/**
	 * 生成唯一序列 ID
	 * 
	 * @return
	 */
	public long generate();

}
