package com.ai.ai.save;

import cn.hutool.core.util.StrUtil;
import com.ai.Exception.ErrorCode;
import com.ai.Exception.ThrowUtils;
import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.ai.model.HtmlCodeResult;

public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult>{

    @Override
    protected void saveFiles(HtmlCodeResult result, String dirPath) {
        writeToFile(dirPath, "index.html", result.getHtmlCode());
    }

    /**
     * 获取代码类型
     */
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    /**
     * 重写参数校验
     * @param result 代码解析结果
     */
    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        ThrowUtils.throwIf(StrUtil.isBlank(result.getHtmlCode()), ErrorCode.NULL_ERROR, "html代码解析结果为空");
    }
}
