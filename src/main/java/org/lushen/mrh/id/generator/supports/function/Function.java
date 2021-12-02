package org.lushen.mrh.id.generator.supports.function;

/**
 * 自定义 function 允许抛出异常
 * 
 * @author hlm
 * @param <F>
 * @param <T>
 */
@FunctionalInterface
public interface Function<F, T> {

	public T apply(F value) throws Exception;

}
