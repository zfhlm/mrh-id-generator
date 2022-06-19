package org.lushen.mrh.id.generator.snowflake;

import org.lushen.mrh.id.generator.IdGenerator;

public class TestSnowflakeIdGenerator {

	public static void main(String[] args) throws InterruptedException {

		// 从启动参数加载配置
		SnowflakeProperties properties = SnowflakeProperties.buildFromSystem();
		IdGenerator idGenerator = new SnowflakeIdGeneratorFactory().create(properties);

		// 调整本地时间，模拟时钟回拨
		for(int i=0; i<100; i++) {
			Thread.sleep(1000L);
			long id = idGenerator.generate();
			System.out.println(id);
			if(id == -1) {
				throw new RuntimeException("move back !");
			}
		}

	}

}
