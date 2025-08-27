package com.ai.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "部署请求封装类")
public class AppDeployRequest {

    @Schema(description = "应用id")
    private Long id;

    @Schema(description = "代码类型（'vuu_project', 'html', 'multi_file'）")

    private String codeType;
}
