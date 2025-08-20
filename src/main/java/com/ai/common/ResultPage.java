package com.ai.common;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "返回结果封装类")
public class  ResultPage <T> {

    /**
     * 消息总数
     */
    @Schema(description = "消息总数")
    private Long total;

    /**
     * 数据内容列表
     */
    @Schema(description = "数据内容列表")
    private List<T> Data;
}
