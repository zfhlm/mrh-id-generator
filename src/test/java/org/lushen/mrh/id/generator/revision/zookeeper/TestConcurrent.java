package org.lushen.mrh.id.generator.revision.zookeeper;

import java.time.Duration;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.lushen.mrh.id.generator.revision.RevisionIdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.revision.achieve.AutoDelayRevisionIdGeneratorFactory;
import org.lushen.mrh.id.generator.revision.achieve.RevisionZookeeperRepository;

public class TestConcurrent {

	public static void main(String[] args) throws Exception {

		// 可用时长 10 秒钟
		RevisionProperties properties = RevisionProperties.buildDefault();
		properties.setTimeToLive(Duration.ofSeconds(10L));
		properties.setRemainingTimeToDelay(Duration.ofSeconds(3));

		String zkString = "localhost:2181";
		CuratorFramework client = CuratorFrameworkFactory.newClient(zkString, new RetryForever(1000));
		client.start();
		client.blockUntilConnected();
		RevisionRepository repository = new RevisionZookeeperRepository(client);

		// 并发创建
		for(int index=0; index<30; index++) {

			new Thread(() -> {
				RevisionIdGenerator idGenerator = new AutoDelayRevisionIdGeneratorFactory(repository).create(properties);
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
