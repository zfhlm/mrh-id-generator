package org.lushen.mrh.id.generator.supports;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestStreamSorted {

	public static void main(String[] args) {

		List<Integer> numbers = new ArrayList<Integer>();
		numbers.add(1);
		numbers.add(2);
		numbers.add(4);
		numbers.add(5);
		numbers.add(6);
		numbers.add(3);
		numbers.add(7);
		numbers.add(6);
		numbers.add(8);
		
		System.out.println(numbers.stream().sorted((prev, next) -> {
			if(prev == 3 || prev == 6) {
				return -1;
			}
			if(next == 3 || next == 6) {
				return 1;
			}
			return 0;
		}).collect(Collectors.toList()));
		
		
	}
	
}
