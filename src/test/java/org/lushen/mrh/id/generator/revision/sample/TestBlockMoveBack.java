package org.lushen.mrh.id.generator.revision.sample;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.lushen.mrh.id.generator.revision.RevisionIdGenerator;
import org.lushen.mrh.id.generator.revision.achieve.DefaultRevisionIdGenerator;

public class TestBlockMoveBack {

	public static void main(String[] args) throws InterruptedException {

		long epochTimestamp = LocalDate.parse("2021-11-01", DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
		long minTimestamp = System.currentTimeMillis();
		long maxTimestamp = minTimestamp + 10*60*1000L;

		RevisionIdGenerator idGenerator = new DefaultRevisionIdGenerator(epochTimestamp, 0, minTimestamp, maxTimestamp);

		// 调整本地时间，模拟时钟回拨
		for(int i=0; i<10000; i++) {
			Thread.sleep(1000L);
			long id = idGenerator.generate();
			System.out.println(id);
			if(id == -1) {
				throw new RuntimeException("move back or expired !");
			}
		}

	}

}
