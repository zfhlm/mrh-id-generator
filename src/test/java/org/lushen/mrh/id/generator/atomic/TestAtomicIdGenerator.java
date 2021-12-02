package org.lushen.mrh.id.generator.atomic;

import org.lushen.mrh.id.generator.IdGenerator;
import org.lushen.mrh.id.generator.atomic.achieve.DefaultAtomicIdGeneratorFactory;

public class TestAtomicIdGenerator {

	public static void main(String[] args) {

		IdGenerator idGenerator = new DefaultAtomicIdGeneratorFactory().create(1L);

		for(int i=0; i<100; i++) {
			System.out.println(idGenerator.generate());
		}

	}

}
