package org.lushen.mrh.id.generator;

/**
 * ID 生成器创建工厂
 * 
 * @author hlm
 * @param <T>
 */
public interface IdGeneratorFactory<C> {

	/**
	 * 创建 ID 生成器
	 * 
	 * @param config
	 * @return
	 */
	public IdGenerator create(C config);

}
