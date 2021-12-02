package org.lushen.mrh.id.generator.supports;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 单线程调度线程池，使用守护线程进行调度，随应用停止自动停止
 * 
 * @author hlm
 */
public class SingleScheduler implements Executor {

	private final Log log = LogFactory.getLog(SingleScheduler.class.getName());

	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 1, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		return thread;
	});

	private LongSupplier doGetInterval;

	public SingleScheduler(LongSupplier doGetInterval) {
		super();
		this.doGetInterval = doGetInterval;
	}

	@Override
	public void execute(Runnable command) {
		this.executor.execute(() -> {
			try {
				while(true) {
					Thread.sleep(this.doGetInterval.getAsLong());
					command.run();
				}
			} catch (Exception e) {
				log.warn("Restart the scheduling, cause by :: " + e.getMessage());
				execute(command);
			}
		});
	}

}
