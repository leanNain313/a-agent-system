package com.ai.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "分页参数请求类")
public class PageRequest implements Serializable {

    /**
     * 起始页面
     */
    @Schema(description = "起始页码(必须)")
    private Integer pageNo;

    /**
     * 页面大小
     */
    @Schema(description = "页面大小(必须, 最多20条)")
    private Integer pageSize;

//    /**
//     * 排序字段
//     */
//    @Schema(description = "排序字段(可选)")
//    private String sortFiled;
//
//    /**
//     * 排序顺序
//     */
//    @Schema(description = "排序顺序")
//    private String sortOrder = "desc";


}
