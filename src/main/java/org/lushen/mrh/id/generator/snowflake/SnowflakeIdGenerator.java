package org.lushen.mrh.id.generator.snowflake;

import org.lushen.mrh.id.generator.IdGenerator;

/**
 * snowflake ID 生成器
 * 
 * +------------------+------------------+------------------+------------------+------------------+
 * +      1 bit       +      41 bit      +      5 bit       +      5 bit       +      12 bit      +
 * +------------------+------------------+------------------+------------------+------------------+
 * +     固定取整          +    毫秒时间戳        +   数据中心节点       +     工作节点          +     计数序列号      +
 * +------------------+------------------+------------------+------------------+------------------+
 * 
 * @author hlm
 */
public interface SnowflakeIdGenerator extends IdGenerator {}
