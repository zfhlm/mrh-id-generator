package org.lushen.mrh.id.generator.revision.redis;

import java.time.Duration;

import org.lushen.mrh.id.generator.IdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.achieve.RevisionRedisRepository;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

public class TestConcurrent {

	public static void main(String[] args) throws Exception {

		LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory();
		connectionFactory.afterPropertiesSet();
		RevisionRepository repository = new RevisionRedisRepository(connectionFactory);

		// 测试，可用时长 10 秒钟
		RevisionProperties properties = RevisionProperties.buildDefault();
		properties.setTimeToLive(Duration.ofSeconds(10L));
		properties.setRemainingTimeToDelay(Duration.ofSeconds(3));

		// 并发创建
		for(int index=0; index<30; index++) {

			new Thread(() -> {
				IdGenerator idGenerator = new RevisionIdGeneratorFactory(repository).create(properties);
				for(int i=0; i<30000; i++) {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(i % 5 == 0) {
						System.out.println(idGenerator.generate());
					}
				}
			}).start();

		}

	}

}
