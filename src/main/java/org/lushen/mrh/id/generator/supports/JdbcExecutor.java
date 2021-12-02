package org.lushen.mrh.id.generator.supports;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.lushen.mrh.id.generator.supports.function.Consumer;
import org.lushen.mrh.id.generator.supports.function.Function;
import org.lushen.mrh.id.generator.supports.function.Supplier;

/**
 * jdbc 执行器
 * 
 * @author hlm
 */
public class JdbcExecutor {

	/**
	 * 事务执行自定义接口逻辑
	 * 
	 * @param doGetConnection
	 * @param releaseConnection
	 * @param executing
	 * @return
	 */
	public static final <T> T executeWithTransaction(Supplier<Connection> doGetConnection, Consumer<Connection> releaseConnection, Function<Connection, T> executing) {
		Connection connection = null;
		try {
			// 关闭自动提交
			connection = doGetConnection.get();
			connection.setAutoCommit(false);
			// 执行指定逻辑
			T result = executing.apply(connection);
			// 提交事务
			connection.commit();
			// 返回结果对象
			return result;
		} catch (Exception e) {
			// 回滚事务
			if(connection != null) {
				try {
					connection.rollback();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			// 重置自动提交
			if(connection != null) {
				try {
					connection.setAutoCommit(true);
					releaseConnection.consume(connection);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	/**
	 * 查询第一条数据
	 * 
	 * @param conn
	 * @param sql
	 * @param converter
	 * @param parameters
	 * @return
	 * @throws JdbcException
	 */
	public static final <T> T executeQueryFirst(Connection conn, String sql, Function<ResultSet,T> converter, Object... parameters) throws JdbcException {
		return executeQuery(conn, sql, converter, parameters).stream().findFirst().orElse(null);
	}

	/**
	 * 查询数据
	 * 
	 * @param conn
	 * @param sql
	 * @param converter
	 * @param parameters
	 * @throws JdbcException
	 */
	public static final <T> List<T> executeQuery(Connection conn, String sql, Function<ResultSet,T> converter, Object... parameters) throws JdbcException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(sql);
			for(int i=1; i<= parameters.length; i++) {
				ps.setObject(i, parameters[i-1]);
			}
			rs = ps.executeQuery();
			List<T> records = new ArrayList<T>(rs.getFetchSize());
			while(rs.next()) {
				records.add(converter.apply(rs));
			}
			return records;
		} catch (Exception e) {
			throw new JdbcException(e.getMessage(), e);
		} finally {
			try {
				if(rs != null) {
					rs.close();
				}
				if(ps != null) {
					ps.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 更新数据
	 * 
	 * @param conn
	 * @param sql
	 * @param parameters
	 * @return
	 * @throws JdbcException
	 */
	public static final int executeUpdate(Connection conn, String sql, Object... parameters) throws JdbcException {
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			for(int i=1; i<= parameters.length; i++) {
				ps.setObject(i, parameters[i-1]);
			}
			return ps.executeUpdate();
		} catch (Exception e) {
			throw new JdbcException(e.getMessage(), e);
		}
	}

	@SuppressWarnings("serial")
	public static class JdbcException extends RuntimeException {

		public JdbcException(String message, Throwable cause) {
			super(message, cause);
		}

	}

}
