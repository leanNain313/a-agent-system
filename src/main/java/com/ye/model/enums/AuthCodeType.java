package com.ye.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum AuthCodeType {

    LOGIN_CODE("登录验证码", "login"),
    REGISTER_CODE("注册验证码", "register"),
    TWO_AUTH_CODE("二级校验验证码", "twoAuth"),
    RESET_CODE("重置密码验证码", "reset");

    private final String text;

    private final String value;

    AuthCodeType(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /** 根据 value 获取枚举 */
    public static AuthCodeType getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (AuthCodeType anEnum : AuthCodeType.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
