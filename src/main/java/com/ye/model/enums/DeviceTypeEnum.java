package com.ye.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum DeviceTypeEnum {

    WEB_TYPE("web", "web"),
    ANDROID_APP_TYPE("android", "android"),
    IOS_APP_TYPE("ios", "ios");

    private final String text;

    private final String value;

    DeviceTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static DeviceTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (DeviceTypeEnum anEnum : DeviceTypeEnum.values()) {
        if (anEnum.value.equals(value)) {
            return anEnum;
            }
        }
        return null;
    }
}
