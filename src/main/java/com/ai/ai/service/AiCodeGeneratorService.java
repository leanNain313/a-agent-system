package com.ai.ai.service;

import com.ai.ai.model.HtmlCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
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
    Flux<String> generateHtmlCode(@UserMessage String userMessage);


    /**
     * 流式生成多文件代码
     *
     * @param userMessage 用户消息
     * @return 返回代码生成结果
     */
    @SystemMessage(fromResource = "prompt/multiFile.txt")
    Flux<String> generateMultiFileCode(@UserMessage String userMessage);


    @SystemMessage(fromResource = "prompt/html.txt")
    HtmlCodeResult codeTest(@UserMessage String userMessage);
}
