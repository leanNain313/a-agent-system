package com.ai.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "获取用户分页列表")
public class UserPageRequest {

    /**
     * 起始页
     */
    @Schema(description = "起始页")
    private Integer pageNo;

    /**
     * 页面大小
     */
    @Schema(description = "页面大小")
    private Integer pageSize;

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
