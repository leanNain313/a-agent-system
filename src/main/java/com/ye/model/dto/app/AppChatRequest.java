package com.ye.model.dto.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "对话请求参数分装")
public class AppChatRequest {

    @Schema(description = "应用id(必须)")
    private Long id;

    @Schema(description = "用户消息(必须)")
    private String message;

    @Schema(description = "代码类型(必须):'html', 'multi_file'")
    private String codeType;

}
