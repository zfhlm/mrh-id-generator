package org.lushen.mrh.id.generator.segment.achieve;

import java.nio.charset.StandardCharsets;

import org.lushen.mrh.id.generator.segment.Segment;
import org.lushen.mrh.id.generator.segment.SegmentRepository;
import org.lushen.mrh.id.generator.supports.NamespaceSupport;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 号段持久化接口 redis 实现
 * 
 * @author hlm
 */
public class SegmentRedisRepository extends NamespaceSupport implements SegmentRepository {

	private final RedisConnectionFactory connectionFactory;

	public SegmentRedisRepository(RedisConnectionFactory connectionFactory) {
		this(null, connectionFactory);
	}

	public SegmentRedisRepository(String namespace, RedisConnectionFactory connectionFactory) {
		super(namespace);
		if(connectionFactory == null) {
			throw new IllegalArgumentException("connectionFactory is null");
		}
		this.connectionFactory = connectionFactory;
	}

	@Override
	public Segment next(int range) {
		RedisConnection connection = null;
		try {
			connection = this.connectionFactory.getConnection();
			long maxValue = connection.incrBy(this.namespace.getBytes(StandardCharsets.UTF_8), (long)range);
			return new Segment(maxValue-range+1, maxValue);
		} finally {
			if(connection != null) {
				connection.close();
			}
		}
	}

}
