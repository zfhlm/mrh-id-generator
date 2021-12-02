package org.lushen.mrh.id.generator.supports;

import java.time.Duration;
import java.util.Optional;

public class TestDuration {
	
	public static void main(String[] args) {
		
		Duration timeToLive = Duration.ofDays(1);
		System.out.println(Optional.of(timeToLive.dividedBy(100L)).filter(e -> e.getSeconds()<=1).orElse(Duration.ofSeconds(1)).toMillis());;
		
	}

}
