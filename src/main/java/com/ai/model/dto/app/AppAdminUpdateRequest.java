package com.ai.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员更新应用请求")
public class AppAdminUpdateRequest {

    /**
     * 应用ID
     */
    @Schema(description = "应用ID(必须)")
    private Long id;

    /**
     * 应用名称
     */
    @Schema(description = "应用名称(可选)")
    private String appName;

    /**
     * 应用封面
     */
    @Schema(description = "应用封面(可选)")
    private String cover;

    /**
     * 优先级
     */
    @Schema(description = "优先级(可选)")
    private Integer priority;
}
