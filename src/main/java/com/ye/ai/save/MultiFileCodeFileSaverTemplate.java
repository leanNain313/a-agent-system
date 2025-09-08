package com.ye.ai.save;

import cn.hutool.core.util.StrUtil;
import com.ye.Exception.ErrorCode;
import com.ye.Exception.ThrowUtils;
import com.ye.ai.enums.CodeGenTypeEnum;
import com.ye.ai.model.MultiFileCodeResult;

public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult>{
    @Override
    protected void saveFiles(MultiFileCodeResult result, String dirPath) {
        writeToFile(dirPath, "index.html", result.getHtmlCode());
        writeToFile(dirPath, "style.css", result.getCssCode());
        writeToFile(dirPath, "script.js", result.getJsCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    /**
     * 重写参数校验
     * @param result 代码解析结果
     */
    @Override
    protected void validateInput(MultiFileCodeResult result) {
        super.validateInput(result);
        // 至少要有html代码
        ThrowUtils.throwIf(StrUtil.isBlank(result.getHtmlCode()), ErrorCode.NULL_ERROR, "html代码解析结果为空");
//        ThrowUtils.throwIf(StrUtil.isBlank(result.getCssCode()), ErrorCode.NULL_ERROR, "css代码解析结果为空");
//        ThrowUtils.throwIf(StrUtil.isBlank(result.getJsCode()), ErrorCode.NULL_ERROR, "js代码解析结果为空");
    }
}
