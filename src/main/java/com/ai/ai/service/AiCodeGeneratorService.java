package com.ai.ai.service;

import com.ai.ai.model.HtmlCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

public interface AiCodeGeneratorService {

    /**
     * 流式化生成html代码
     *
     * @param userMessage 用户消息
     * @return 返回代码生成结果
     */
    @SystemMessage(fromResource = "prompt/html.txt")
    Flux<String> generateHtmlCode(String userMessage);


    /**
     * 流式生成多文件代码
     *
     * @param userMessage 用户消息
     * @return 返回代码生成结果
     */
    @SystemMessage(fromResource = "prompt/multiFile.txt")
    Flux<String> generateMultiFileCode(String userMessage);

    /**
     * 流式生成vue项目
     * @param appId 会话id(应用id)
     * @param userMessage 用户消息
     * @return 流式返回内容
     */
    @SystemMessage(fromResource = "prompt/vue_project.md")
    TokenStream generateVueProjectCode(@MemoryId Long appId, @UserMessage String userMessage);

    @SystemMessage(fromResource = "prompt/html.txt")
    HtmlCodeResult codeTest(@UserMessage String userMessage);
}
