package com.ye.common;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 返回结果封装类
 * 
 * @param <T>
 */
@Data
@Schema(description = "返回结果封装类")
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = -8306791619272002396L;
    /**
     * 状态码
     */
    @Schema(description = "状态码")
    private int code;
    /**
     * 返回的数据
     */
    @Schema(description = "返回的数据")
    private T data;
    /**
     * 错误信息
     */
    @Schema(description = "错误信息")
    private String message;
    /**
     * 、
     * 描述
     */
    @Schema(description = "描述")
    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse() {
        this(200, null, "成功", "");
    }

    public BaseResponse(int code, T data) {
        this(code, data, "成功", "");
    }

    public BaseResponse(int code, String message, String description) {
        this(code, null, message, description);
    }
}
