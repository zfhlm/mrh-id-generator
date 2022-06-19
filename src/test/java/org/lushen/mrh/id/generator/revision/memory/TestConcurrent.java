package org.lushen.mrh.id.generator.revision.memory;

import java.time.Duration;

import org.lushen.mrh.id.generator.IdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.achieve.RevisionMemoryRepository;

public class TestConcurrent {

	public static void main(String[] args) throws Exception {

		RevisionRepository repository = new RevisionMemoryRepository();

		RevisionProperties properties = RevisionProperties.buildDefault();
		properties.setTimeToLive(Duration.ofSeconds(10L));
		properties.setRemainingTimeToDelay(Duration.ofSeconds(3));
		
		// 并发创建
		for(int index=0; index<100; index++) {

			new Thread(() -> {
				IdGenerator generator = new RevisionIdGeneratorFactory(repository).create(properties);
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
