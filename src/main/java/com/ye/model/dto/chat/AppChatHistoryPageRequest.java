package com.ye.model.dto.chat;

import com.ye.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "应用内聊天历史分页请求")
public class AppChatHistoryPageRequest extends PageRequest {

    @Schema(description = "应用ID(必须)")
    private Long appId;
}
