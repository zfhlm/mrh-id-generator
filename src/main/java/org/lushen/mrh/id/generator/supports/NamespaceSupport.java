package org.lushen.mrh.id.generator.supports;

import java.util.regex.Pattern;

/**
 * 业务命名空间定义：
 * 
 * 		当构造参数 ${namespace} == null 时，namespace = 实现类包名
 * 
 * 		当构造参数 ${namespace} != null 时，namespace = 实现类包名.${namespace}
 * 
 * @author hlm
 */
public class NamespaceSupport {

	protected String namespace;

	protected NamespaceSupport(String namespace) {
		super();
		this.namespace = namespace(namespace);
	}

	protected String namespace(String namespace) {
		StringBuilder builder = new StringBuilder();
		builder.append("org.lushen.mrh.id.generator");
		if(namespace != null) {
			builder.append(".");
			builder.append(namespace);
		}
		return builder.toString();
	}

	/**
	 * zookeeper 路径格式命名空间
	 * 
	 * @author hlm
	 */
	public static abstract class Zookeeper extends NamespaceSupport {

		protected static final Pattern ZOOKEEPER_PATH_PATTERN = Pattern.compile("^(/[a-zA-Z0-9\\.\\-~]+)+$");

		protected static final String SLASH = "/";

		protected Zookeeper(String namespace) {
			super(namespace);
		}

		@Override
		protected String namespace(String namespace) {
			if(namespace != null && ! ZOOKEEPER_PATH_PATTERN.matcher(namespace).matches() ) {
				throw new IllegalArgumentException(String.format("Namespace [%s] does not matches regex :: %s", namespace, ZOOKEEPER_PATH_PATTERN));
			}
			StringBuilder sb = new StringBuilder();
			if(namespace != null) {
				sb.append(namespace);
			}
			sb.append(SLASH);
			sb.append(getClass().getPackage().getName());
			return sb.toString();
		}

	}

}
