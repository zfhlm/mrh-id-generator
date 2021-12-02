
CREATE TABLE `segment_alloc` (
  `namespace` varchar(100) NOT NULL COMMENT '命名空间',
  `max_value` bigint(19) NOT NULL COMMENT '最大已使用ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '更新时间',
  `version` bigint(19) NOT NULL COMMENT '版本号',
  PRIMARY KEY (`namespace`)
) ENGINE=InnoDB COMMENT='号段 ID 生成器信息存储表';
