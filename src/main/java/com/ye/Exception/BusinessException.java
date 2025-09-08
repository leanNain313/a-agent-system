package com.ye.Exception;


/**
 * 自定义异常
 * test
 * @Author laiqi
 */
public class BusinessException extends RuntimeException{

    private static final long serialVersionUID = 9214238697623405018L;
    /**
     * 状态码
     */
    private int code;
    /**
     * 描述
     */
    private String description;

    /**
     * 自定义错误信息，状态码，描述
     * @param message 错误信息
     * @param code 状态码
     * @param description 描述
     */
    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message, String description) {
        super(message);
        this.code = errorCode.getCode();
        this.description = description;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
