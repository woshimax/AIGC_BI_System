
-- 创建库
create database if not exists bi_db;

-- 切换库
use bi_db;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount (userAccount)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 图表信息表
create table if not exists chart
(
    id         bigint auto_increment comment 'id' primary key,
    `name`     varchar(128)                       null comment '图表名称',
    goal       text                               null comment '分析目标',
    chartData  text                               null comment '图表数据',
    chartType  varchar(128)                       null comment '图表类型',
    genChart   text                               null comment '生成的图表数据',
    genResult  text                               null comment '生成的分析结论',
    status     varchar(10)                        not null default 'wait' comment 'wait,succeed,running,failed',
    execMessage text                              null comment '执行信息',
    userId     bigint                             null comment '创建者id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '图表信息表' collate = utf8mb4_unicode_ci;

create table if not exists chart_1813458944191479810(
    日期   bigint not null comment "日期",
    用户数 bigint not null comment "用户数"
)comment '原始数据分表1813458944191479810' collate = utf8mb4_unicode_ci;
create table if not exists chart_1813621224148865026(
                                                        日期   bigint not null comment "日期",
                                                        用户数 bigint not null comment "用户数"
)comment '原始数据分表1813458944191479810' collate = utf8mb4_unicode_ci;

-- 图表测试表
create table if not exists chart_test
(
    id         bigint auto_increment comment 'id' primary key,
    `name`     varchar(128)                       null comment '图表名称',
    goal       text                               null comment '分析目标',
    chartData  text                               null comment '图表数据',
    chartType  varchar(128)                       null comment '图表类型',
    genChart   text                               null comment '生成的图表数据',
    genResult  text                               null comment '生成的分析结论',
    status     varchar(10)                        not null default 'wait' comment 'wait,succeed,running,failed',
    execMessage text                              null comment '执行信息',
    userId     bigint                             null comment '创建者id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '图表信息表' collate = utf8mb4_unicode_ci;

