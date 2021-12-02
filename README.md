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

       3，支持自动处理 2^2 - 1 = 3 次时钟回拨（减初始 00 bits）

       4，降低计数序列号为 10 bit，即支持最大每秒 102.4 万个 ID 生成

    基于 springboot 代码使用示例：

        // 创建生成器配置
        @Bean
        public RevisionProperties revisionProperties() {
            RevisionProperties properties = RevisionProperties.buildDefault();
            properties.setInterval(Duration.ofSeconds(30L));
            properties.setTimeToLive(Duration.ofMinutes(10));
            properties.setThreshold(80);
            return properties;
        }

        // 创建生成器存储中间件接口
        @Bean
        public RevisionRepository revisionRepository(DataSource dataSource) {
            return new RevisionMysqlJdbcRepository("mytest", dataSource);
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

        timeToLive             初始化或延时时长，如果配置过长，应用重启、时钟回拨超过三次都会导致占用 workerId 一段可用时间，可能导致耗尽，建议10分钟

        threshold              延时阈值，范围(0,100)，需要根据 timeToLive 进行定义，在超时之前留有足够的时候去执行延时逻辑

        interval               延时检测时间间隔

    生成器逻辑：

        首先获取一个可用的 workerId 及其 可用时间范围，实例化生成器

        异步调度任务，对当前的 ID 生成器 可用时间范围 进行检测，到达阈值延长 workerId 可用时间范围

        调用方法生成 ID，如果 ID 等于 -1，表示生成器已经失效，可能原因：延时失败、时钟回拨超过三次

        当前 ID 生成器失效，触发同步获取另一个可用的 workId 及其 可用时间范围，实例化作为当前 ID 生成器

## segment

    号段 ID 生成器，基于 springboot 使用示例：

        // 创建号段配置
        @Bean
        public SegmentProperties segmentProperties() {
            SegmentProperties properties = SegmentProperties.buildDefault();
            properties.setRange(1000);
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

        threshold       号段预加载百分比阈值，当号段已使用达到阈值，进行备用预加载，范围 (0, 100)

        interval        号段预加载检测时间间隔，范围 (0, N)

        参数 threshold、interval 影响预加载号段，配置不合理，会导致当前号段被用光，预加载未完成

        如果预加载未完成，ID 生成器会进行直接加载，性能变低，且打乱递增

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

        实现接口 org.lushen.mrh.id.generator.segment.SegmentRepository

        实现类最好能提供 号段命名空间 规则，对不同服务的 号段 进行划分，使每个服务都有自己的号段，互不影响

    号段生成器：

        借鉴了美团 Leaf ID 生成器思想，采用 内存双号段 的思想，生成范围从 1 开始，直至 long 正数值耗尽

        具体参考实现类 org.lushen.mrh.id.generator.segment.achieve.DefaultSegmentIdGenerator
