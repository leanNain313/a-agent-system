package com.ai.ai.save;

import com.ai.Exception.BusinessException;
import com.ai.Exception.ErrorCode;
import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.ai.model.HtmlCodeResult;
import com.ai.ai.model.MultiFileCodeResult;

import java.io.File;

/**
 * 代码写入执行器
 */
public class CodeFileSaverExecutor {

    private static final HtmlCodeFileSaverTemplate htmlCodeFileSaver = new HtmlCodeFileSaverTemplate();

    private static final MultiFileCodeFileSaverTemplate multiFileCodeFileSaver = new MultiFileCodeFileSaverTemplate();

    public static File executeSaver(Object codeResult, CodeGenTypeEnum codeGenType) {
        return switch (codeGenType) {
            case HTML -> htmlCodeFileSaver.saveCode((HtmlCodeResult) codeResult);
            case MULTI_FILE -> multiFileCodeFileSaver.saveCode((MultiFileCodeResult) codeResult);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        };
    }
}
