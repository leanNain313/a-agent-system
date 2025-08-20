package com.ai.ai.service;

import com.ai.ai.model.HtmlCodeResult;
import com.ai.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
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

}
