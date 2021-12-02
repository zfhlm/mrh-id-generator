package org.lushen.mrh.id.generator.snowflake;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * snowflake 配置
 * 
 * @author hlm
 */
public class SnowflakeProperties {

	/**
	 * snowflake epoch 日期常量
	 */
	private static final String SNOWFLAKE_EPOCH_DEFAULT = "2021-11-01";

	/**
	 * snowflake epoch 参数名称
	 */
	private static final String SNOWFLAKE_EPOCH_KEY = "snowflake.epoch";

	/**
	 * snowflake dataCenterId 参数名称
	 */
	private static final String SNOWFLAKE_DATACENTER_KEY = "snowflake.datacenter";

	/**
	 * snowflake workerId 参数名称
	 */
	private static final String SNOWFLAKE_WORKER_KEY = "snowflake.worker";

	/**
	 * 从启动参数中加载为配置对象，命令参数示例： -Dsnowflake.epoch=2021-11-01 -Dsnowflake.datacenter=0 -Dsnowflake.worker=0
	 * 
	 * @return
	 */
	public static final SnowflakeProperties buildFromSystem() {

		String dataCenterId = System.getProperty(SNOWFLAKE_DATACENTER_KEY);
		String workerId = System.getProperty(SNOWFLAKE_WORKER_KEY);
		String epoch = System.getProperty(SNOWFLAKE_EPOCH_KEY, SNOWFLAKE_EPOCH_DEFAULT);
		if(dataCenterId == null) {
			throw new IllegalArgumentException("No dataCenterId available !");
		}
		if(workerId == null) {
			throw new IllegalArgumentException("No workerId available !");
		}

		SnowflakeProperties properties = new SnowflakeProperties();
		properties.setEpochDate(LocalDate.parse(epoch, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		properties.setDataCenterId(Integer.parseInt(dataCenterId));
		properties.setWorkerId(Integer.parseInt(workerId));
		return properties;
	}

	protected LocalDate epochDate;	// 系统上线日期

	protected int dataCenterId;		// 数据中心节点ID

	protected int workerId;			// 工作节点ID

	public LocalDate getEpochDate() {
		return epochDate;
	}

	public void setEpochDate(LocalDate epochDate) {
		this.epochDate = epochDate;
	}

	public int getDataCenterId() {
		return dataCenterId;
	}

	public void setDataCenterId(int dataCenterId) {
		this.dataCenterId = dataCenterId;
	}

	public int getWorkerId() {
		return workerId;
	}

	public void setWorkerId(int workerId) {
		this.workerId = workerId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[epochDate=");
		builder.append(epochDate);
		builder.append(", dataCenterId=");
		builder.append(dataCenterId);
		builder.append(", workerId=");
		builder.append(workerId);
		builder.append("]");
		return builder.toString();
	}

}
