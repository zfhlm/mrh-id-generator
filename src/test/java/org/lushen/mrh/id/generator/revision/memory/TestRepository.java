package org.lushen.mrh.id.generator.revision.memory;

import java.time.Duration;

import org.lushen.mrh.id.generator.revision.achieve.RevisionMemoryRepository;

public class TestRepository {
	
	public static void main(String[] args) throws InterruptedException {
		
		RevisionMemoryRepository repository = new RevisionMemoryRepository();
		
		while(true) {
			Thread.sleep(1000L);
			repository.next(System.currentTimeMillis(), Duration.ofSeconds(10));
			repository.next(System.currentTimeMillis(), Duration.ofSeconds(10));
			repository.next(System.currentTimeMillis(), Duration.ofSeconds(10));
			repository.next(System.currentTimeMillis(), Duration.ofSeconds(10));
			repository.next(System.currentTimeMillis(), Duration.ofSeconds(10));
		}
		
	}

}
