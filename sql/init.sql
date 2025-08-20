create database ai_agent_system;

CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                        `user_account` VARCHAR(256) NOT NULL COMMENT '账号',
                        `user_password` VARCHAR(256) NOT NULL COMMENT '密码',
                        `user_name` VARCHAR(256) NULL COMMENT '用户昵称',
                        `user_avatar` VARCHAR(1024) NULL COMMENT '用户头像',
                        `user_profile` VARCHAR(512) NULL COMMENT '用户简介',
                        `user_role` VARCHAR(64) NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban等',
                        `unionId` VARCHAR(256) NULL COMMENT '微信开放平台id',
                        `mpOpenId` VARCHAR(256) NULL COMMENT '公众号openId',
                        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `is_delete` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除（0-未删，1-已删）',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_user_account` (`user_account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
