package com.ye.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户更新自己应用请求（仅支持名称）")
public class AppUpdateByUserRequest {

    /**
     * 应用ID
     */
    @Schema(description = "应用ID(必传)")
    private Long id;

    /**
     * 新的应用名称
     */
    @Schema(description = "新的应用名称(可选)")
    private String appName;
}
