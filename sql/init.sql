create
    database ai_agent_system;

CREATE TABLE `user`
(
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_account`  VARCHAR(256)  NOT NULL COMMENT '账号',
    `user_password` VARCHAR(256)  NOT NULL COMMENT '密码',
    `user_name`     VARCHAR(256)  NULL COMMENT '用户昵称',
    `user_avatar`   VARCHAR(1024) NULL COMMENT '用户头像',
    `user_profile`  VARCHAR(512)  NULL COMMENT '用户简介',
    `user_role`     VARCHAR(64)   NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban等',
    `unionId`       VARCHAR(256)  NULL COMMENT '微信开放平台id',
    `mpOpenId`      VARCHAR(256)  NULL COMMENT '公众号openId',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`     TINYINT       NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删，1-已删）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_account` (`user_account`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户表';

-- 应用表
create table app
(
    id            bigint auto_increment comment 'id' primary key,
    app_name      varchar(256)                       null comment '应用名称',
    cover         varchar(512)                       null comment '应用封面',
    init_prompt   text                               null comment '应用初始化的 prompt',
    code_type     varchar(64)                        null comment '代码生成类型（枚举）',
    deploy_key    varchar(64)                        null comment '部署标识',
    deployed_time datetime                           null comment '部署时间',
    priority      int      default 0                 not null comment '优先级',
    user_id       bigint                             not null comment '创建用户id',
    edit_time     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deploy_key), -- 确保部署标识唯一
    INDEX idx_appName (app_name),         -- 提升基于应用名称的查询性能
    INDEX idx_userId (user_id)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;

-- 对话历史表
create table chat_history
(
    id          bigint auto_increment comment 'id' primary key,
    message     text                               not null comment '消息',
    messageType varchar(32)                        not null comment 'user/ye',
    appId       bigint                             not null comment '应用id',
    userId      bigint                             not null comment '创建用户id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    INDEX idx_appId (appId), -- 提升基于应用的查询性能
    INDEX idx_createTime (createTime),   -- 提升基于时间的查询性能
    INDEX idx_appId_createTime (appId, createTime) -- 游标查询核心索引
) comment '对话历史' collate = utf8mb4_unicode_ci;

-- 帖子表
CREATE TABLE post_table (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '帖子ID',
                            title VARCHAR(255) NOT NULL COMMENT '帖子标题',
                            content TEXT NOT NULL COMMENT '帖子内容',
                            like_set JSON DEFAULT NULL COMMENT '点赞用户集合(JSON存user_id数组)',
                            user_id BIGINT NOT NULL COMMENT '发帖用户ID',
                            create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            edit_time TIMESTAMP NULL DEFAULT NULL COMMENT '编辑时间',
                            update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            is_delete TINYINT(1) DEFAULT 0 COMMENT '是否删除(0=正常,1=删除)',
                            audit_status INT DEFAULT 0 COMMENT '审核状态(0=未通过,1=通过)',
                            explain varchar(256) null comment '审核原因',
                            priority int default 0 null comment '优先级',
                            INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子表';


-- 评论表
CREATE TABLE comment_table (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '评论ID',
                               content TEXT NOT NULL COMMENT '评论内容',
                               like_set JSON DEFAULT NULL COMMENT '点赞用户集合(JSON存user_id数组)',
                               father_id BIGINT NOT NULL COMMENT '父评论ID(为空表示顶级评论)',
                               user_id BIGINT NOT NULL COMMENT '评论用户ID',
                               comment_level INT DEFAULT 1 COMMENT '评论层级(1=一级评论,2=二级...)',
                               create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               edit_time TIMESTAMP NULL DEFAULT NULL COMMENT '编辑时间',
                               update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               is_delete TINYINT(1) DEFAULT 0 COMMENT '是否删除(0=正常,1=删除)',
                               post_id bigint null comment '帖子id',
                               INDEX idx_user_id (user_id),
                               INDEX idx_father_id (father_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';
