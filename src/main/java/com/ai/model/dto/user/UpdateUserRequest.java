package com.ai.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改用户请求参数")
public class UpdateUserRequest {

    /**
     * 用户id
     */
    @Schema(description = "修改用户的id")
    private Long id;

    /**
     * 密码
     */
    @Schema(description = "密码， 不得少于8位， 不得包含特殊字符")
    private String userPassword;

    /**
     * 用户名称
     */
    @Schema(description = "用户名")
    private String userName;

    /**
     * 用户角色
     */
    @Schema(description = "用户角色")
    private String userRole;
}
