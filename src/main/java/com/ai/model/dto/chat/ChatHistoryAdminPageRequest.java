package com.ai.model.dto.chat;

import com.ai.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "管理员聊天历史分页请求")
public class ChatHistoryAdminPageRequest extends PageRequest {

    @Schema(description = "按应用筛选，可选")
    private Long appId;
}
