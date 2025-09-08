package com.ye.ai.core;

import com.ye.ai.model.HtmlCodeResult;
import com.ye.ai.model.MultiFileCodeResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ye
 */
@Deprecated
public class CodeParseTool {

    // 允许 ```html 后跟换行或空格
    private static final Pattern HTML_CODE_PATTERN = Pattern.compile("```html(?:\\s*\\n|\\s+)([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern CSS_CODE_PATTERN = Pattern.compile("```css(?:\\s*\\n|\\s+)([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    private static final Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)(?:\\s*\\n|\\s+)([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);

    /**
     * 解析html单文件代码
     *
     * @param codeContent 代码
     * @return 返回代码封装类
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent) {
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
     * 多文件代码解析器
     * @param codeContent 源码
     * @return 返回代码封装类
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
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
     * 提取第一个匹配（单文件模式用）
     */
    private static String extractFirstCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
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
