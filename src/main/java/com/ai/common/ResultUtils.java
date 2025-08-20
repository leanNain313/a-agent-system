package com.ai.common;


import com.ai.Exception.ErrorCode;

/**
 * 返回封装工具类
 *
 * @Author laiqi
 */
public class ResultUtils {

    /**
     * 成功
     * @param <T> 类型
     * @return 结果
     */
    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>();
    }

    /**
     * 成功
     * @param data 返回数据
     * @param <T> 类型
     * @return 结果
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(200, data);
    }

    /**
     * 失败
     * @param code 状态码
     * @param message 错误信息
     * @param description 描述
     * @param <T> 类型
     * @return 结果
     */
    public static <T> BaseResponse<T> error(int code, String message, String description) {
        return new BaseResponse<>(code, null, message, description);
    }

    /**
     * 失败
     * @param errorCode 里面有自定义的状态码
     * @param <T> 类型
     * @return 结果
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), errorCode.getDescription());
    }

    /**
     * 失败
     * @param errorCode 里面有自定义的状态码
     * @param description 可以重新自定义描述
     * @param <T> 类型
     * @return 结果
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String description) {
        return  new BaseResponse<>(errorCode.getCode(), errorCode.getMessage(), description);
    }

}
