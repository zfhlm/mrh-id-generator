package org.lushen.mrh.id.generator.segment.memory;

import org.lushen.mrh.id.generator.segment.SegmentIdGenerator;
import org.lushen.mrh.id.generator.segment.SegmentProperties;
import org.lushen.mrh.id.generator.segment.achieve.DefaultSegmentIdGeneratorFactory;
import org.lushen.mrh.id.generator.segment.achieve.SegmentMemoryRepository;

public class TestSegmentGenerator {

	public static void main(String[] args) throws Exception {
		
		SegmentProperties properties = SegmentProperties.buildDefault();
		properties.setThreshold(20);
		properties.setRange(10);

		SegmentMemoryRepository repository = new SegmentMemoryRepository();
		SegmentIdGenerator generator = new DefaultSegmentIdGeneratorFactory(repository).create(properties);

		for(int i=0; i<60; i++) {
			Thread.sleep(1000L);
			System.out.println(generator.generate());
		}
	}

}
