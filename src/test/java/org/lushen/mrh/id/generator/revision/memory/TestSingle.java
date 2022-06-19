package org.lushen.mrh.id.generator.revision.memory;

import java.time.Duration;

import org.lushen.mrh.id.generator.IdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.achieve.RevisionMemoryRepository;

public class TestSingle {

	public static void main(String[] args) throws Exception {

		RevisionRepository repository = new RevisionMemoryRepository();

		RevisionProperties properties = RevisionProperties.buildDefault();
		properties.setTimeToLive(Duration.ofSeconds(10L));
		properties.setRemainingTimeToDelay(Duration.ofSeconds(3));
		IdGenerator idGenerator = new RevisionIdGeneratorFactory(repository).create(properties);

		for(int i=0; i<30000; i++) {
			Thread.sleep(1000L);
			System.out.println(idGenerator.generate());
		}

	}

}
