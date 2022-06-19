package org.lushen.mrh.id.generator.atomic;

import org.lushen.mrh.id.generator.IdGenerator;

public class TestAtomicIdGenerator {

	public static void main(String[] args) {

		// 默认 seed
		IdGenerator idGenerator = new AtomicIdGeneratorFactory().create();
		for(int i=0; i<100; i++) {
			System.out.println(idGenerator.generate());
		}

		// 指定 seed
		idGenerator = new AtomicIdGeneratorFactory().create(1000L);
		for(int i=0; i<100; i++) {
			System.out.println(idGenerator.generate());
		}

	}

}
