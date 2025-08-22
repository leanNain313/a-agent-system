package com.ai.model.dto.user;

import com.ai.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "获取用户分页列表")
public class UserPageRequest extends PageRequest {

    /**
     * 用户名称
     */
    @Schema(description = "用户名(可选)")
    private String userName;

    /**
     * 用户角色
     */
    @Schema(description = "用户角色(可选)")
    private String userRole;

}
