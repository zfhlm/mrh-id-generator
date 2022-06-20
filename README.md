# mrh-revisionid-generator

    分布式 ID 生成器

## snowflake

    snowflake ID 结构和特点：

       +------------------+------------------+------------------+------------------+------------------+
       +      1 bit       +      41 bit      +      5 bit       +      5 bit       +      12 bit      +
       +------------------+------------------+------------------+------------------+------------------+
       +    固定取整      +    毫秒时间戳    +   数据中心节点   +     工作节点     +     计数序列号   +
       +------------------+------------------+------------------+------------------+------------------+

       1，支持时长 2^41 毫秒，大约69年

       2，支持 2^5 + 2^5 = 1024 个应用节点

       3，每毫秒可以生成 ID 数 2^12 = 4096 个，即每秒生成 ID 数 4096000 个

       4，时钟回退，刚好获取到重复 ID 会导致业务主键冲突

    使用示例一：

        // 从启动参数创建配置，启动命令例如：
        // java -jar -Dsnowflake.epoch=2021-11-01 -Dsnowflake.datacenter=0 -Dsnowflake.worker=0 application.jar
        SnowflakeProperties properties = SnowflakeProperties.buildFromSystem();

        // 创建 ID 生成器
        IdGenerator idGenerator = new DefaultSnowflakeIdGeneratorFactory().create(properties);
        long id = idGenerator.generate();

    使用示例二：

        // 创建配置
        SnowflakeProperties properties = new SnowflakeProperties();
        properties.setEpochDate(LocalDate.parse("2021-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        properties.setDataCenterId(0);
        properties.setWorkerId(0);

        // 创建 ID 生成器
        IdGenerator idGenerator = new DefaultSnowflakeIdGeneratorFactory().create(properties);
        long id = idGenerator.generate();

    参数配置：

        epochDate          系统上线时间，一旦指定不可变动

        dataCenterId       数据中心节点ID，范围 [0, 32)

        workerId           工作节点ID，范围 [0, 32)

    存在缺陷：

        原生的生成器未解决时钟回拨问题，无法直接使用，服务器 NTP 时钟回拨会导致 ID 冲突

## revision

    基于 snowflake 的实现原理，自定义变种生成器，结构和特点：

       +------------------+------------------+------------------+------------------+------------------+
       +      1 bit       +      41 bit      +     10 bit       +      2 bit       +      10 bit      +
       +------------------+------------------+------------------+------------------+------------------+
       +     固定取整     +    毫秒时间戳    +     工作节点     + 时钟回拨滚动次数 +     计数序列号   +
       +------------------+------------------+------------------+------------------+------------------+

       1，支持时长 2^41 毫秒，大约69年 (与 snowflake 一致)

       2，支持 2^10 = 1024 个 workerId (将 snowflake 的两个参数 workerId、dataCenterId 合并为一个参数)

       3，支持自动处理 2^2 - 1 = 3 次时钟回拨（减初始 00 bits，可用 01、10、11 bits） (自定义参数，占用 2 bit)

       4，降低计数序列号为 10 bit，即支持最大每秒 102.4 万个 ID 生成 (比 snowflake 的计数序列号少 2 bit)

    基于 springboot 代码使用示例：

        // 创建生成器配置
        @Bean
        public RevisionProperties revisionProperties() {
            RevisionProperties properties = RevisionProperties.buildDefault();
            properties.setNamespace("my-cluster-name.my-service-name");
            properties.setTimeToLive(Duration.ofMinutes(10));
            properties.setRemainingTimeToDelay(Duration.ofSeconds(30));
            properties.setEpochDate(LocalDate.parse("2021-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            return properties;
        }

        // 创建生成器存储中间件接口
        @Bean
        public RevisionRepository revisionRepository(DataSource dataSource) {
            return new RevisionMysqlJdbcRepository(dataSource);
        }

        // 创建ID生成器
        @Bean
        public IdGenerator revisionIdGenerator(RevisionRepository repository, RevisionProperties properties) {
            return new RevisionIdGeneratorFactory(repository).create(properties);
        }

        // 注入ID生成器
        @Autowired
        private IdGenerator revisionIdGenerator;

        // 生成全局唯一 ID
        long id = idGenerator.generate();

    生成器配置：

        namespace              同个数据源的情况下，根据业务命名空间进行区分，不同命名空间互不影响

        epochDate              系统上线时间，一旦指定不可变动

        timeToLive             生成器每次实例化可用时长

        remainingTimeToDelay   剩余多少时长，才实例化备用生成器，根据 timeToLive 合理配置，在过期之前留有足够的时候去执行

    生成器逻辑：

        1，随机获取一个可用的 workerId，实例化当前生成器

        2，执行异步调度任务，对当前生成器剩余可用时长进行检测，判断是否实例化备用生成器

        3，生成器已过期，切换备用生成器为当前生成器

        4，生成器不可用，同步实例化当前生成器

    存储接口实现：

        org.lushen.mrh.id.generator.revision.RevisionRepository                       接口声明，可扩展其他存储实现

        org.lushen.mrh.id.generator.revision.achieve.RevisionMemoryRepository         基于内存存储(仅测试使用)

        org.lushen.mrh.id.generator.revision.achieve.RevisionMysqlJdbcRepository      基于mysql存储(建议使用)

        org.lushen.mrh.id.generator.revision.achieve.RevisionRedisRepository          基于redis存储(存在风险)

        # 使用 mysql 需要建数据库表：
        CREATE TABLE `revision_alloc` (
          `namespace` varchar(100) NOT NULL COMMENT '业务命名空间',
          `worker_id` int(4) NOT NULL COMMENT '工作节点ID',
          `last_timestamp` bigint(19) NOT NULL COMMENT '最后被使用的时间戳(毫秒)',
          `create_time` datetime NOT NULL COMMENT '创建时间',
          `modify_time` datetime DEFAULT NULL COMMENT '更新时间'
          PRIMARY KEY (`worker_id`,`namespace`)
        ) ENGINE=InnoDB COMMENT='revisionid 生成器信息存储表';

## segment

    号段 ID 生成器，借鉴了美团 Leaf ID 生成器

    基于 springboot 使用示例：

        // 创建号段配置，命名空间建议使用 clusterName.serviceName 的方式进行命名
        @Bean
        public SegmentProperties segmentProperties() {
            SegmentProperties properties = SegmentProperties.buildDefault();
            properties.setNamespace("mrh-cluster.service-test")
            properties.setRange(1000);
            properties.setRemaining(200);
            return properties;
        }

        // 创建号段存储中间件接口
        @Bean
        public SegmentRepository segmentRepository(DataSource dataSource) {
            return new SegmentMysqlJdbcRepository(dataSource);
        }

        // 号段ID生成器
        @Bean
        public IdGenerator segmentIdGenerator(SegmentRepository repository, SegmentProperties properties) {
            return new DefaultSegmentIdGeneratorFactory(repository).create(properties);
        }

        // 注入号段ID生成器
        @Autowired
        private IdGenerator idGenerator;

        // 生成全局唯一 ID
        long id = idGenerator.generate();

    号段配置：

        namespace       同个数据源的情况下，根据业务命名空间进行区分，不同命名空间互不影响

        range           每次拉取多少号段到内存中，根据并发合理配置，频繁重启会导致未使用号段被浪费，范围 (0, N)

        remaining       号段剩余多少，进行备用预加载，范围 (0, range)

    号段分配中间件，已存在接口实现：

        org.lushen.mrh.id.generator.segment.SegmentRepository                     接口声明，实现类各自定义存储实现

        org.lushen.mrh.id.generator.segment.achieve.SegmentMemoryRepository       基于内存进行分配(仅测试使用)

        org.lushen.mrh.id.generator.segment.achieve.SegmentMysqlJdbcRepository    基于mysql数据库进行分配

        org.lushen.mrh.id.generator.segment.achieve.SegmentRedisRepository        基于redis进行分配(存在风险)

        # 使用 mysql 需要创建数据库表
        CREATE TABLE `segment_alloc` (
            `namespace` varchar(100) NOT NULL COMMENT '命名空间',
            `max_value` bigint(19) NOT NULL COMMENT '最大已使用ID',
            `create_time` datetime NOT NULL COMMENT '创建时间',
            `modify_time` datetime DEFAULT NULL COMMENT '更新时间',
            `version` bigint(19) NOT NULL COMMENT '版本号',
            PRIMARY KEY (`namespace`)
        ) ENGINE=InnoDB COMMENT='号段 ID 生成器信息存储表';
