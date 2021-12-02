package org.lushen.mrh.id.generator.revision.zookeeper;

import java.time.Duration;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.achieve.RevisionZookeeperRepository;

public class TestRepository {

	public static void main(String[] args) throws InterruptedException {

		String zkString = "localhost:2181";
		CuratorFramework client = CuratorFrameworkFactory.newClient(zkString, new RetryForever(1000));
		client.start();
		client.blockUntilConnected();
		RevisionRepository repository = new RevisionZookeeperRepository(client);

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
