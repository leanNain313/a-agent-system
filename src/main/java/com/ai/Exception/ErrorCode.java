package com.ai.Exception;

/**
 * 自定义业务错误码
 * @Author laiqi
 */
public enum ErrorCode {

    SUCCESS(200, "成功", "响应成功"),
    PARAMS_ERROR(40000, "请求参数错误", "判断请求错误类型"),
    NULL_ERROR(40001, "请求参数为空", "判断请求错误类型复"),
    DATA_RULE_ERROR(40002, "数据不符合规则", "用户账户密码的校验"),
    ACCOUNT_ERROR(40003, "账户重复", "校验账户是否重"),
    PASSWORD_ERROR(40004, "密码错误", "校验账户密码是否正确"),
    PASSWORD_REPEAT_ERROR(40005, "两次密码输入不一致", "校验密码"),
    NO_LOGIN(40100, "请检查您的登录", "没有登陆或者登录过期"),
    NO_AUTH(40101, " 权限不足", "权限不足"),
    SYSTEM_ERROR(50000, "系统内部异常", "服务器出错"),
    ACCOUNT_DISABLE_ERROR(40007, "账户被封禁", ""),
    FUNCTION_DISABLE_ERROR(40008, "该功能被封禁", ""),
    PROJECT_BUILD_ERROR(40009, "项目构建失败", "检查代码, 以及依赖"),
    CODE_SEND_ERROR(40011, "验证码发送失败", "验证码发送失败"),
    CODE_ERROR(40012, "验证码错误", "验证码错误"),
    AUTH_ERROR(40013, "二级验证失败", "进行二级认证，防止重要数据误删"),
    REPEAT_OPERATE_ERROR(40014, "请不重复操作， 请稍后重试", "操作频繁"),
    CODE_OVERDUE_ERROR(400010, "验证码已过期", "验证码过期， 或者根本不存在");

    /**
     * 错误状态码
     */
    private final int code;

    /**
     * 错误信息
     */
    private final String message;

    /**
     * 描述
     */
    private final String description;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }
}
