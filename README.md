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

        dataCenterId       数据中心节点ID，范围 [0, 31)

        workerId           工作节点ID，范围 [0, 31)

    该生成器未解决时钟回拨问题，无法直接使用，服务器 NTP 会导致 ID 冲突

## revision

    基于 snowflake 自定义变种生成器，结构和特点：

       +------------------+------------------+------------------+------------------+------------------+
       +      1 bit       +      41 bit      +     10 bit       +      2 bit       +      10 bit      +
       +------------------+------------------+------------------+------------------+------------------+
       +     固定取整     +    毫秒时间戳    +     工作节点     + 时钟回拨滚动次数 +     计数序列号   +
       +------------------+------------------+------------------+------------------+------------------+

       1，支持时长 2^41 毫秒，大约69年

       2，支持 2^10 = 1024 个 workerId

       3，支持自动处理 2^2 - 1 = 3 次时钟回拨（减初始 00 bits，可用 01、10、11 bits）

       4，降低计数序列号为 10 bit，即支持最大每秒 102.4 万个 ID 生成

    基于 springboot 代码使用示例：

        // 创建生成器配置
        @Bean
        public RevisionProperties revisionProperties() {
            RevisionProperties properties = RevisionProperties.buildDefault();
            properties.setTimeToLive(Duration.ofMinutes(10));
            properties.setRemainingTimeToDelay(Duration.ofSeconds(30));
            properties.setThreshold(80);
            return properties;
        }

        // 创建生成器存储中间件接口，指定业务命名空间为 myservice-name
        @Bean
        public RevisionRepository revisionRepository(DataSource dataSource) {
            return new RevisionMysqlJdbcRepository("myservice-name", dataSource);
        }

        // 创建ID生成器
        @Bean
        public RevisionIdGenerator revisionIdGenerator(RevisionRepository repository, RevisionProperties properties) {
            return new AutoDelayRevisionIdGeneratorFactory(repository).create(properties);
        }

        // 注入ID生成器
        @Autowired
        private RevisionIdGenerator revisionIdGenerator;

        // 生成全局唯一 ID
        long id = idGenerator.generate();

    生成器配置：

        epochDate              系统上线时间，一旦指定不可变动

        timeToLive             初始或延时时长

        remainingTimeToDelay   剩余多少时长触发延时，根据 timeToLive 合理配置，在超时之前留有足够的时候去执行延时逻辑

    生成器延时调度逻辑：

        1，应用启动获取一个可用的 workerId  及其 可用时间范围，实例化作为 当前 ID 生成器

        2，应用启动异步调度任务，对当前的 ID 生成器剩余可用时长进行检测，判断是否进行延时

        3，调用方法生成 ID，如果 ID 等于 -1，表示生成器已经失效，触发同步获取另一个可用的 workId 及其 可用时间范围，实例化作为当前 ID 生成器

        4，步骤 2/3 是各自执行的，通过锁进行并发控制，直到应用停止为止

    存储接口实现：

        org.lushen.mrh.id.generator.revision.achieve.RevisionMemoryRepository         基于内存存储(仅测试使用)

        org.lushen.mrh.id.generator.revision.achieve.RevisionMysqlJdbcRepository      基于mysql存储(建议使用)

        org.lushen.mrh.id.generator.revision.achieve.RevisionRedisRepository          基于redis存储(存在风险)

        org.lushen.mrh.id.generator.revision.achieve.RevisionZookeeperRepository      基于zookeeper存储(存在风险)

    存储接口使用 mysql 需要建数据库表：

        CREATE TABLE `revision_alloc` (
            `worker_id` int(4) NOT NULL COMMENT '工作节点ID',
            `namespace` varchar(100) NOT NULL COMMENT '业务命名空间',
            `expired` bigint(19) NOT NULL COMMENT '最大过期时间戳(毫秒)',
            `create_time` datetime NOT NULL COMMENT '创建时间',
            `modify_time` datetime DEFAULT NULL COMMENT '更新时间'
            PRIMARY KEY (`worker_id`,`namespace`)
        ) ENGINE=InnoDB COMMENT='revisionid 生成器信息存储表';

    存储接口扩展：

        org.lushen.mrh.id.generator.revision.RevisionRepository                      实现此接口，并提供 业务命名空间 功能

    业务命名空间，例如有两个业务不同的服务 service-A、service-B，创建存储接口：

        // 各自拥有 1024 个 workerId，互相之间不影响

        // service-A
        @Bean
        public RevisionRepository revisionRepository(DataSource dataSource) {
            return new RevisionMysqlJdbcRepository("service-A", dataSource);
        }

        // service-B
        @Bean
        public RevisionRepository revisionRepository(DataSource dataSource) {
            return new RevisionMysqlJdbcRepository("service-B", dataSource);
        }

## segment

    号段 ID 生成器，借鉴了美团 Leaf ID 生成器

    基于 springboot 使用示例：

        // 创建号段配置
        @Bean
        public SegmentProperties segmentProperties() {
            SegmentProperties properties = SegmentProperties.buildDefault();
            properties.setRange(1000);
            properties.setRemaining(200);
            return properties;
        }

        // 创建号段分配中间件接口，并指定号段命名空间
        @Bean
        public SegmentRepository segmentRepository(DataSource dataSource) {
            return new SegmentMysqlJdbcRepository("mytest", dataSource);
        }

        // 号段ID生成器
        @Bean
        public SegmentIdGenerator segmentIdGenerator(SegmentRepository repository, SegmentProperties properties) {
            return new DefaultSegmentIdGeneratorFactory(repository).create(properties);
        }

        // 注入号段ID生成器
        @Autowired
        private SegmentIdGenerator idGenerator;

        // 生成全局唯一 ID
        long id = idGenerator.generate();

    号段配置：

        range           每次拉取多少号段到内存中，根据并发进行，频繁重启会导致未使用号段被浪费，范围 (0, N)

        remaining       号段剩余多少，进行备用预加载，范围 (0, range)，如果来不及预加载，ID 生成时会进行直接加载

    号段分配中间件，已存在接口实现：

        org.lushen.mrh.id.generator.segment.achieve.SegmentMemoryRepository       基于内存进行分配(仅测试使用)

        org.lushen.mrh.id.generator.segment.achieve.SegmentMysqlJdbcRepository    基于mysql数据库进行分配(建议使用)

        org.lushen.mrh.id.generator.segment.achieve.SegmentRedisRepository        基于redis进行分配(存在风险)

        org.lushen.mrh.id.generator.segment.achieve.SegmentZookeeperRepository    基于zookeeper进行分配(存在风险)

    号段分配中间件，mysql 需要创建数据库表：

        CREATE TABLE `segment_alloc` (
            `namespace` varchar(100) NOT NULL COMMENT '命名空间',
            `max_value` bigint(19) NOT NULL COMMENT '最大已使用ID(不包括此值)',
            `create_time` datetime NOT NULL COMMENT '创建时间',
            `modify_time` datetime DEFAULT NULL COMMENT '更新时间',
            `version` bigint(19) NOT NULL COMMENT '版本号',
            PRIMARY KEY (`namespace`)
        ) ENGINE=InnoDB COMMENT='号段 ID 生成器信息存储表';

    号段分配中间件，接口扩展：

        实现接口 org.lushen.mrh.id.generator.segment.SegmentRepository            实现此接口，提供 号段命名空间 功能
