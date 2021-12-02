package org.lushen.mrh.id.generator.supports.function;


/**
 * 自定义 consumer 允许抛出异常
 * 
 * @author hlm
 * @param <F>
 */
@FunctionalInterface
public interface Consumer<F> {

	public void consume(F value) throws Exception;

}
