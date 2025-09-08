package com.ye.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum ExamineStatusEnum {

    NOT_EXAMINE("待审核", 0),
    PASS_EXAMINE("通过", 1),
    REFUSE_EXAMINE("拒绝", 2);

    private final String text;

    private final int value;

    ExamineStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static ExamineStatusEnum getEnumByValue(int value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (ExamineStatusEnum anEnum : ExamineStatusEnum.values()) {
        if (anEnum.value == value) {
            return anEnum;
            } 
        }
        return null;
    }
}
