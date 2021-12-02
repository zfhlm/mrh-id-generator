package org.lushen.mrh.id.generator.revision.zookeeper;

import java.util.StringTokenizer;

public class TestStringTokenizer {
	
	public static void main(String[] args) {
		

		StringTokenizer tokenizer = new StringTokenizer("0-expired-create-modidy-version", "-");
		if(tokenizer.hasMoreElements()) {
			System.out.println(tokenizer.nextElement());
		}
		if(tokenizer.hasMoreTokens()) {
			System.out.println(tokenizer.nextToken());
		}
		if(tokenizer.hasMoreTokens()) {
			System.out.println(tokenizer.nextToken());
		}
		if(tokenizer.hasMoreTokens()) {
			System.out.println(tokenizer.nextToken());
		}
		if(tokenizer.hasMoreTokens()) {
			System.out.println(tokenizer.nextToken());
		}
		
	}

}
