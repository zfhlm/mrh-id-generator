package org.lushen.mrh.id.generator.supports;

import java.util.Date;

import org.lushen.mrh.id.generator.revision.RevisionEntity;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

public class TestRedisSerializer {

	public static void main(String[] args) {

		RevisionEntity entity = new RevisionEntity();
		entity.setCreateTime(new Date());
		entity.setModifyTime(new Date());
		entity.setLastTimestamp(System.currentTimeMillis());
		entity.setNamespace("test");
		entity.setWorkerId(2);

		Jackson2JsonRedisSerializer<RevisionEntity> serializer = new Jackson2JsonRedisSerializer<>(RevisionEntity.class);
		System.out.println(new String(serializer.serialize(entity)));

		System.out.println(serializer.deserialize(serializer.serialize(entity)));


	}

}
