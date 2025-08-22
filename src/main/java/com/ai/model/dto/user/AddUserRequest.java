package com.ai.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "添加用户请求参数")
public class AddUserRequest {

    /**
     * 用户id
     */
    @Schema(description = "修改用户的id(必须)")
    private Long id;

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
     * 检查密码
     */
    @Schema(description = "二次检验密码(必须)")
    private String checkPassword;

    /**
     * 用户名称
     */
    @Schema(description = "用户名(必须)")
    private String userName;

    /**
     * 用户角色
     */
    @Schema(description = "用户角色(必须)")
    private String userRole;
}
