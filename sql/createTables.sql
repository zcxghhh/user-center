-- auto-generated definition
create table user
(
    id           bigint auto_increment comment 'id'
        primary key,
    username     varchar(256)                       null comment '用户昵称',
    userAccount  varchar(256)                       null comment '登录账号',
    gender       tinyint                            null comment '用户性别',
    userPassword varchar(512)                       null comment '密码',
    avatarUrl    varchar(1024)                      null comment '头像',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 null comment '表示是否有效  0正常',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 null comment '逻辑删除',
    userRole     int      default 0                 not null comment '角色
0 普通用户
1 管理员
',
    planetCode   varchar(512)                       null comment '星球编号'
)
    comment '用户表';

