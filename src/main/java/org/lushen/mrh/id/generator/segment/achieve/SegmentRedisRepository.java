package org.lushen.mrh.id.generator.segment.achieve;

import java.nio.charset.StandardCharsets;

import org.lushen.mrh.id.generator.segment.Segment;
import org.lushen.mrh.id.generator.segment.SegmentRepository;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 号段持久化接口 redis 实现
 * 
 * @author hlm
 */
public class SegmentRedisRepository implements SegmentRepository {

	private final RedisConnectionFactory connectionFactory;

	public SegmentRedisRepository(RedisConnectionFactory connectionFactory) {
		super();
		if(connectionFactory == null) {
			throw new IllegalArgumentException("connectionFactory is null");
		}
		this.connectionFactory = connectionFactory;
	}

	@Override
	public Segment next(String namespace, int range) {
		RedisConnection connection = null;
		try {
			connection = this.connectionFactory.getConnection();
			long maxValue = connection.incrBy(namespace.getBytes(StandardCharsets.UTF_8), (long)range);
			return new Segment(maxValue-range+1, maxValue);
		} finally {
			if(connection != null) {
				connection.close();
			}
		}
	}

}
