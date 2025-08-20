package com.ai.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "账号注册参数类")
public class RegisterRequest implements Serializable {

    /**
     * 账户
     */
    @Schema(description = "账户")
    private String userAccount;

    /**
     * 密码
     */
    @Schema(description = "密码， 不得少于8位， 不得包含特殊字符")
    private String userPassword;

    /**
     * 检查密码
     */
    @Schema(description = "二次检验密码")
    private String checkPassword;
}
