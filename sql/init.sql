create
database ai_agent_system;

CREATE TABLE `user`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_account`  VARCHAR(256) NOT NULL COMMENT '账号',
    `user_password` VARCHAR(256) NOT NULL COMMENT '密码',
    `user_name`     VARCHAR(256) NULL COMMENT '用户昵称',
    `user_avatar`   VARCHAR(1024) NULL COMMENT '用户头像',
    `user_profile`  VARCHAR(512) NULL COMMENT '用户简介',
    `user_role`     VARCHAR(64)  NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban等',
    `unionId`       VARCHAR(256) NULL COMMENT '微信开放平台id',
    `mpOpenId`      VARCHAR(256) NULL COMMENT '公众号openId',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_delete`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删，1-已删）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_account` (`user_account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 应用表
create table app
(
    id           bigint auto_increment comment 'id' primary key,
    app_name      varchar(256) null comment '应用名称',
    cover        varchar(512) null comment '应用封面',
    init_prompt   text null comment '应用初始化的 prompt',
    code_type  varchar(64) null comment '代码生成类型（枚举）',
    deploy_key    varchar(64) null comment '部署标识',
    deployed_time datetime null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    user_id       bigint                             not null comment '创建用户id',
    edit_time     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deployKey (deploy_key),  -- 确保部署标识唯一
    INDEX idx_appName (app_name),     -- 提升基于应用名称的查询性能
    INDEX idx_userId (user_id)      -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_unicode_ci;

