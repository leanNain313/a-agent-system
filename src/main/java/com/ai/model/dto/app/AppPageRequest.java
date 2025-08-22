package com.ai.model.dto.app;

import com.ai.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页查询应用列表请求（管理员/精选）")
public class AppPageRequest extends PageRequest {

    @Schema(description = "名称模糊查询（可选）")
    private String appName;

    @Schema(description = "创建者用户ID（管理员可筛， 可选）")
    private Long userId;

    @Schema(description = "优先级（管理员可筛, 可选）")
    private Integer priority;

    @Schema(description = "代码类型（管理员可筛， 可选）")
    private String codeType;

}
