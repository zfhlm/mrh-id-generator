package org.lushen.mrh.id.generator.segment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lushen.mrh.id.generator.IdGenerator;

/**
 * 号段 ID 生成器
 * 
 * @author hlm
 */
public class SegmentIdGenerator extends SegmentProperties implements IdGenerator {

	private final Log log = LogFactory.getLog("SegmentIdGenerator");

	private final SegmentRepository repository;											// 存储接口

	private long offset;																// 当前号段使用偏移量

	private Segment curr;																// 当前号段

	private Segment back;																// 备用号段

	SegmentIdGenerator(SegmentRepository repository) {
		super();
		if(repository == null) {
			throw new IllegalArgumentException("repository");
		}
		this.repository = repository;
	}

	@Override
	public synchronized long generate() {

		// 当前号段未加载，直接加载
		if(this.curr == null) {
			Segment segment = this.repository.next(this.namespace, this.range);
			log.info("Load segment " + segment);
			this.curr = segment;
			this.offset = 0;
		}

		// 当前号段刚好达到预加载阈值，异步加载备用号段
		// 加载任务非频繁任务，不用线程池，直接使用守护线程执行，用后即毁
		if(this.back == null && (this.range - this.offset == this.remaining + 1)) {
			Thread async = new Thread(() ->  {
				synchronized (this.repository) {
					Segment segment = this.repository.next(this.namespace, this.range);
					log.info("Preload segment " + segment);
					this.back = segment;
				}
			});
			async.setDaemon(true);
			async.start();
		}

		// 根据当前号段，生成当前ID
		long id = this.curr.getMin() + this.offset;
		this.offset++;

		// 当前号段未耗尽，返回当前ID
		if(id <= curr.getMax()) {
			return id;
		}
		// 当前号段已经耗尽
		else {
			synchronized (this.repository) {
				// 存在备用号段，切换号段
				if(this.back != null) {
					this.curr = this.back;
					this.offset = 0;
					this.back = null;
				}
				// 不存在备用号段，重新加载当前号段
				else {
					this.curr = null;
				}
				return generate();
			}
		}

	}

}
