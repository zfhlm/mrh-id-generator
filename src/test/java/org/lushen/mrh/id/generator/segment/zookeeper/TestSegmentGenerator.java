package org.lushen.mrh.id.generator.segment.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.lushen.mrh.id.generator.segment.SegmentIdGenerator;
import org.lushen.mrh.id.generator.segment.SegmentProperties;
import org.lushen.mrh.id.generator.segment.achieve.DefaultSegmentIdGeneratorFactory;
import org.lushen.mrh.id.generator.segment.achieve.SegmentZookeeperRepository;

public class TestSegmentGenerator {
	
	public static void main(String[] args) throws Exception {

		String zkString = "localhost:2181";
		CuratorFramework client = CuratorFrameworkFactory.newClient(zkString, new RetryForever(1000));
		client.start();
		client.blockUntilConnected();

		SegmentZookeeperRepository repository = new SegmentZookeeperRepository("/test/segment", client);

		SegmentProperties properties = SegmentProperties.buildDefault();
		properties.setThreshold(20);
		properties.setRange(10);
		SegmentIdGenerator generator = new DefaultSegmentIdGeneratorFactory(repository).create(properties);

		for(int i=0; i<60; i++) {
			Thread.sleep(1000L);
			System.out.println(generator.generate());
		}
	}

}
