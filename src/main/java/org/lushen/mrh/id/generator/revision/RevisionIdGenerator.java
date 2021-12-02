package org.lushen.mrh.id.generator.revision;

import org.lushen.mrh.id.generator.IdGenerator;

/**
 * revision ID 生成器，基于 snowflake 的变种生成器，指定可用时段，并实现以下结构：
 * 
 * +------------------+------------------+------------------+------------------+------------------+
 * +      1 bit       +      41 bit      +     10 bit       +      2 bit       +      10 bit      +
 * +------------------+------------------+------------------+------------------+------------------+
 * +     固定取整          +    毫秒时间戳        +     工作节点          + 时钟回拨滚动次数    +     计数序列号      +
 * +------------------+------------------+------------------+------------------+------------------+
 * 
 * @author hlm
 */
public interface RevisionIdGenerator extends IdGenerator {}
