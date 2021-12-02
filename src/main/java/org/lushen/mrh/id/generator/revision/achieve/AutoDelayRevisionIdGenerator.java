package org.lushen.mrh.id.generator.revision.achieve;

import java.time.ZoneOffset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.revision.RevisionIdGenerator;
import org.lushen.mrh.id.generator.revision.RevisionNode;
import org.lushen.mrh.id.generator.revision.RevisionProperties;
import org.lushen.mrh.id.generator.revision.RevisionRepository;
import org.lushen.mrh.id.generator.supports.SingleScheduler;
import org.lushen.mrh.id.generator.supports.function.Stater;

/**
 * 自动延时 revision ID 生成器
 * 
 * @author hlm
 */
class AutoDelayRevisionIdGenerator extends RevisionProperties implements RevisionIdGenerator, Stater {

	private final Log log = LogFactory.getLog(AutoDelayRevisionIdGenerator.class.getSimpleName());

	private final RevisionRepository repository;		// 持久化接口

	private SingleScheduler scheduler;					// 调度器

	private DefaultRevisionIdGenerator idGenerator;		// ID 生成器

	AutoDelayRevisionIdGenerator(RevisionRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public long generate() {

		long id = this.idGenerator.generate();

		// ID 正常，直接返回
		if(id != -1L) {
			return id;
		}

		// 可能因为时钟回退次数超过三次、异步延时失败，导致 ID 生成器不可用，与延时任务共用一把锁
		synchronized (this) {
			// 二次确认是否可用
			id = this.idGenerator.generate();
			if(id != -1L) {
				return id;
			}
			// 直接创建 ID 生成器
			else {
				createIdGenerator();
				return generate();
			}
		}

	}

	@Override
	public void start() {

		// 初始化 ID 生成器
		if(this.idGenerator == null) {
			createIdGenerator();
		}

		// 启动延时调度器
		if(this.scheduler == null) {
			this.scheduler = new SingleScheduler(() -> 1000L);
			this.scheduler.execute(() -> {
				synchronized (this) {
					delayIdGenerator();
				}
			});
		}

	}

	// 创建 ID 生成器
	private void createIdGenerator() {
		long epochAt = this.epochDate.atStartOfDay(ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
		long begin = System.currentTimeMillis();
		RevisionNode node = this.repository.next(begin, this.timeToLive);
		this.idGenerator = new DefaultRevisionIdGenerator(epochAt, node.getWorkerId(), begin, node.getExpired());
	}

	// 延时 ID 生成器
	private void delayIdGenerator() {
		if(this.idGenerator.getExpiredAt() - System.currentTimeMillis() <= this.remainingTimeToDelay.toMillis()) {
			try {
				int workerId = this.idGenerator.getWorkerId();
				long expired = this.idGenerator.getExpiredAt();
				RevisionNode node = this.repository.delay(workerId, expired, this.timeToLive);
				this.idGenerator.delay(node.getExpired());
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}
	}

}
