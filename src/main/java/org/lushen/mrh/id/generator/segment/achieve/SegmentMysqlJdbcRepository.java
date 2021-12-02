package org.lushen.mrh.id.generator.segment.achieve;

import javax.sql.DataSource;

import org.lushen.mrh.id.generator.segment.Segment;
import org.lushen.mrh.id.generator.segment.SegmentRepository;
import org.lushen.mrh.id.generator.supports.JdbcExecutor;
import org.lushen.mrh.id.generator.supports.NamespaceSupport;

/**
 * 号段持久化接口 mysql jdbc 实现
 * 
 * @author hlm
 */
public class SegmentMysqlJdbcRepository extends NamespaceSupport implements SegmentRepository {

	private final DataSource dataSource;

	public SegmentMysqlJdbcRepository(DataSource dataSource) {
		this(null, dataSource);
	}

	public SegmentMysqlJdbcRepository(String namespace, DataSource dataSource) {
		super(namespace);
		if(dataSource == null) {
			throw new IllegalArgumentException("dataSource is null");
		}
		this.dataSource = dataSource;
	}

	@Override
	public Segment next(int range) {

		return JdbcExecutor.executeWithTransaction(() -> dataSource.getConnection(), connection -> connection.close(), connection -> {

			// 查询数据
			String selectForUpdate = "select * from `segment_alloc` where namespace = ? for update";
			Long maxValue = JdbcExecutor.executeQueryFirst(connection, selectForUpdate, rs -> rs.getLong("max_value"), this.namespace);

			// 插入数据
			if(maxValue == null) {
				String insert = "insert into `segment_alloc` set namespace=?, max_value=?, create_time=now(), modify_time=now(), version=0";
				JdbcExecutor.executeUpdate(connection, insert, this.namespace, range);
				return new Segment(1L, range);
			}
			// 更新数据
			else {
				String update = "update `segment_alloc` set max_value=?, version=version+1, modify_time=now() where namespace = ?";
				JdbcExecutor.executeUpdate(connection, update, maxValue+range, this.namespace);
				return new Segment(maxValue.longValue()+1, maxValue.longValue()+range);
			}

		});

	}

}
