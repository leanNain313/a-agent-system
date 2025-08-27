package com.ai.ai.service;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiSmartRouterGeneratorFactory {

    @Resource
    private ChatModel chatModel;

    /**
     * 创建智能ai路由实例
     */
    @Bean
    public AiSmartRouterGeneratorService aiSmartRouterGeneratorService() {
        return AiServices.builder(AiSmartRouterGeneratorService.class)
                .chatModel(chatModel)
                .build();
    }
}
