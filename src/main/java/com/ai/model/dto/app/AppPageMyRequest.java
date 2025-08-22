package com.ai.model.dto.app;

import com.ai.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页查询我的应用列表请求")
public class AppPageMyRequest extends PageRequest {

    @Schema(description = "按名称模糊搜索(可选)")
    private String appName;

}
