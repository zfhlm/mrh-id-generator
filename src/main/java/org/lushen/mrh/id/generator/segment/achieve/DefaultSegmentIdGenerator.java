package org.lushen.mrh.id.generator.segment.achieve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.segment.Segment;
import org.lushen.mrh.id.generator.segment.SegmentIdGenerator;
import org.lushen.mrh.id.generator.segment.SegmentProperties;
import org.lushen.mrh.id.generator.segment.SegmentRepository;
import org.lushen.mrh.id.generator.supports.SingleScheduler;
import org.lushen.mrh.id.generator.supports.function.Stater;

/**
 * 号段 ID 生成器默认实现
 * 
 * @author hlm
 */
class DefaultSegmentIdGenerator extends SegmentProperties implements SegmentIdGenerator, Stater {

	private final Log log = LogFactory.getLog("SegmentIdGenerator");

	private final Object loadLock = new Object();	// 加载号段锁对象

	private final SegmentRepository repository;		// 存储接口

	private SingleScheduler scheduler;				// 调度器

	private volatile long offset;					// 当前号段使用偏移量

	private Segment curr;							// 当前号段

	private Segment next;							// 预加载号段

	DefaultSegmentIdGenerator(SegmentRepository repository) {
		super();
		if(repository == null) {
			throw new IllegalArgumentException("repository");
		}
		this.repository = repository;
	}

	@Override
	public synchronized long generate() {

		// 生成当前 ID
		long id = this.curr.getMin() + this.offset;
		this.offset++;

		// ID号段已经耗尽
		if(id > curr.getMax()) {

			// 切换预加载号段
			if(this.next != null) {
				this.curr = this.next;
				this.offset = 0;
				this.next = null;
				return generate();
			}

			// 直接加载，与预加载共用一把锁，二次确认预加载号段，保证单点绝对趋势递增
			synchronized (this.loadLock) {
				if(next != null) {
					this.curr = this.next;
					this.offset = 0;
					this.next = null;
				} else {
					Segment segment = this.repository.next(this.range);
					log.info("Load segment " + segment);
					this.curr = segment;
					this.offset = 0;
				}
				return generate();
			}

		}

		return id;
	}

	@Override
	public void start() {

		if(this.curr == null) {

			// 初始化当前号段
			Segment segment = this.repository.next(this.range);
			log.info("Initial load segment " + segment);
			this.curr = segment;
			this.offset = 0;

		}

		if(this.scheduler == null) {

			// 预加载逻辑，与直接加载共用一把锁
			long point = this.range*this.threshold/100;
			Runnable preload = () -> {
				synchronized (this.loadLock) {
					if(this.next == null && this.offset >= point) {
						Segment segment = this.repository.next(this.range);
						log.info("Preload segment " + segment);
						this.next = segment;
					}
				}
			};

			// 启动预加载调度线程
			this.scheduler = new SingleScheduler(() -> this.interval.toMillis());
			this.scheduler.execute(preload);

		}

	}

}
