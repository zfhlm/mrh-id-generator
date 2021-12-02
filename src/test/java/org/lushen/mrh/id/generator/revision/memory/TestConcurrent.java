package org.lushen.mrh.id.generator.revision.memory;

import java.time.Duration;

import org.lushen.mrh.id.generator.revision.RevisionIdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.achieve.AutoDelayRevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.achieve.RevisionMemoryRepository;

public class TestConcurrent {

	public static void main(String[] args) throws Exception {

		RevisionRepository repository = new RevisionMemoryRepository();

		RevisionProperties properties = RevisionProperties.buildDefault();
		properties.setTimeToLive(Duration.ofSeconds(10L));
		
		// 并发创建
		for(int index=0; index<100; index++) {

			new Thread(() -> {
				RevisionIdGenerator generator = new AutoDelayRevisionIdGeneratorFactory(repository).create(properties);
				for(int i=0; i<30000; i++) {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if(i % 5 == 0) {
						System.out.println(generator.generate());
					}
				}
			}).start();

		}

	}

}
