package com.ye.ai.parser;

import com.ye.ai.model.HtmlCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * html单文件代码解析器
 */
public class HtmlCodeParseTool implements CodeParseTool<HtmlCodeResult>{

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html(?:\\s*\\n|\\s+)([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);


    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        HtmlCodeResult result = new HtmlCodeResult();
        // 提取 HTML 代码
        String htmlCode = extractFirstCodeByPattern(codeContent, HTML_CODE_PATTERN);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        } else {
            // 如果没有找到代码块，将整个内容作为HTML
            result.setHtmlCode(codeContent.trim());
        }
        return result;
    }

    /**
     * 提取第一个匹配（单文件模式用）
     */
    private static String extractFirstCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
