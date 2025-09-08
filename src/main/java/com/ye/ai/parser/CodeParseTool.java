package com.ye.ai.parser;

/**
 * 代码解析器策略接口
 *
 * @author ye
 */
public interface CodeParseTool<T> {

    /**
     * 解析代码内容
     * @param codeContent 原信息
     * @return 返回代码
     */
    T parseCode(String codeContent);

}
