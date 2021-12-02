
CREATE TABLE `revision_alloc` (
  `worker_id` int(4) NOT NULL COMMENT '工作节点ID',
  `namespace` varchar(100) NOT NULL COMMENT '业务命名空间',
  `expired` bigint(19) NOT NULL COMMENT '最大过期时间戳(毫秒)',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `modify_time` datetime DEFAULT NULL COMMENT '更新时间'
  PRIMARY KEY (`worker_id`,`namespace`)
) ENGINE=InnoDB COMMENT='revisionid 生成器信息存储表';
