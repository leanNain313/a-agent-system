package com.ai.ai.parser;

import com.ai.Exception.BusinessException;
import com.ai.Exception.ErrorCode;
import com.ai.ai.enums.CodeGenTypeEnum;

/**
 * 代码解析执行器
 */
public class CodeParserExecutor {

    private static final HtmlCodeParseTool htmlCodeParseTool = new HtmlCodeParseTool();

    private static final MultiFileCodeParseTool multiFileCodeParseTool = new MultiFileCodeParseTool();

    public static Object executeParseTool(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParseTool.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParseTool.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未知代码类型");
        };
    }
}
