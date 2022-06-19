package org.lushen.mrh.id.generator.segment.memory;

import org.lushen.mrh.id.generator.segment.achieve.SegmentMemoryRepository;

public class TestSegmentMemoryRepository {

	public static void main(String[] args) {

		SegmentMemoryRepository repository = new SegmentMemoryRepository();

		System.out.println("第1个号段：" + repository.next("service-test", 10000));
		System.out.println("第2个号段：" + repository.next("service-test", 10000));
		System.out.println("第3个号段：" + repository.next("service-test", 1000));

	}

}
