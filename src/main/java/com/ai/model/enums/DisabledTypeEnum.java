package com.ai.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum DisabledTypeEnum {

    UPLOAD_IMAGE_TYPE("upload_image", "upload_image");

    private final String text;

    private final String value;

    DisabledTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static DisabledTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (DisabledTypeEnum anEnum : DisabledTypeEnum.values()) {
        if (anEnum.value.equals(value)) {
            return anEnum;
            }
        }
        return null;
    }
}
