package com.ai.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "登录参数封装")
public class LoginRequest implements Serializable {

    /**
     * 账户
     */
    @Schema(description = "账户(必须)")
    private String userAccount;

    /**
     * 密码
     */
    @Schema(description = "密码， 不得少于8位， 不得包含特殊字符(必须)")
    private String userPassword;

    /**
     * 登录设备类型例如： web, App, 小程序
     */
    @Schema(description = "登录设备类型(传其中的一个)： web, android, ios")
    private String deviceType;

    /**
     * 此次登录的客户端设备唯一标识id
     */
    @Schema(description = "此次登录的客户端设备唯一标识id(可不传)")
    private String deviceId;

    /**
     * 图形验证码
     */
    @Schema(description = "图形验证码")
    private String imageCode;
}
