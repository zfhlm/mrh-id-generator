package org.lushen.mrh.id.generator.segment.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.lushen.mrh.id.generator.segment.achieve.SegmentZookeeperRepository;

public class TestSegmentZookeeperRepository {

	public static void main(String[] args) throws Exception {

		String zkString = "localhost:2181";
		CuratorFramework client = CuratorFrameworkFactory.newClient(zkString, new RetryForever(1000));
		client.start();
		client.blockUntilConnected();

		SegmentZookeeperRepository repository = new SegmentZookeeperRepository("/test", client);

		System.out.println("第1个号段：" + repository.next(10000));
		System.out.println("第2个号段：" + repository.next(10000));
		System.out.println("第3个号段：" + repository.next(1000));

		client.close();

	}

}
