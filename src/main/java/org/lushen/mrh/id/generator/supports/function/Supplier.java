package org.lushen.mrh.id.generator.supports.function;

/**
 * 自定义 supplier 允许抛出异常
 * 
 * @author hlm
 * @param <T>
 */
@FunctionalInterface
public interface Supplier<T> {

	public T get() throws Exception;

}
