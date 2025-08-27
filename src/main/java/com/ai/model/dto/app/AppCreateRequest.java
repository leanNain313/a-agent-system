package com.ai.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户创建应用请求")
public class AppCreateRequest {

    /**
     * 应用名称
     */
    @Schema(description = "应用名称(可选)")
    private String appName;

    /**
     * 初始化 Prompt（必填）
     */
    @Schema(description = "初始化 Prompt(必须)")
    private String initPrompt;

//    /**
//     * 代码生成类型（可选，透传枚举字符串）
//     */
//    @Schema(description = "透传枚举字符串, html, multi_file(必须)")
//    private String codeType;
}
