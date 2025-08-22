package com.ai.ai.parser;

import com.ai.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiFileCodeParseTool implements CodeParseTool<MultiFileCodeResult> {

    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html(?:\\s*\\n|\\s+)([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css(?:\\s*\\n|\\s+)([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)(?:\\s*\\n|\\s+)([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();

        // 提取并合并各类代码
        String htmlCode = extractAllCodeByPattern(codeContent, HTML_CODE_PATTERN);
        String cssCode = extractAllCodeByPattern(codeContent, CSS_CODE_PATTERN);
        String jsCode = extractAllCodeByPattern(codeContent, JS_CODE_PATTERN);

        if (!htmlCode.isEmpty()) result.setHtmlCode(htmlCode.trim());
        if (!cssCode.isEmpty()) result.setCssCode(cssCode.trim());
        if (!jsCode.isEmpty()) result.setJsCode(jsCode.trim());

        return result;
    }

    /**
     * 提取所有匹配并合并（多文件模式用）
     */
    private static String extractAllCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            sb.append(matcher.group(1).trim()).append("\n\n");
        }
        return sb.toString();
    }
}
