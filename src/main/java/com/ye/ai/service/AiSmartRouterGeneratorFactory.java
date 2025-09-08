package com.ye.ai.service;

import com.ye.utils.SpringContextUtil;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiSmartRouterGeneratorFactory {

    /**
     * 创建智能ai路由实例
     */
    @Bean
    public AiSmartRouterGeneratorService createAiSmartRouterGeneratorService() {
        ChatModel routingChatModelPrototype = SpringContextUtil.getBean("routingChatModelPrototype", ChatModel.class);
        return AiServices.builder(AiSmartRouterGeneratorService.class)
                .chatModel(routingChatModelPrototype)
                .build();
    }
}
