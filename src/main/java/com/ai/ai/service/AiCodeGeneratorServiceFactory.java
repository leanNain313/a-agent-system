package com.ai.ai.service;

import com.ai.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * ai模型注册工厂
 */
@Configuration
@Slf4j
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    // 最大会话记忆轮数
    private final int MAX_COUNT = 20;


    /**
     * 注册大模型, 创建大模型实例
     * @return 返回大模型实例
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return createAiCodeGeneratorService(2L);
    }


    /**
     * Ai服务实例缓存
     */
    private final Cache<Long, AiCodeGeneratorService> cache = Caffeine.newBuilder()
            .maximumSize(1000) // 储存实例的最大数量
            .expireAfterWrite(Duration.ofMinutes(30)) // 写入后30分钟过期
            .expireAfterAccess(Duration.ofMinutes(10)) // 10分钟不操作过期
            .removalListener(((key, value, cause) -> {
                log.info("ai服务实例被移除, appid：{}， {}", key, cause);
            })) // 实例移除监听器
            .build();

    /**
     * 取出实例
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId) {
        return cache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 创建ai实例
     * @param appId 应用id
     * @return 返回一个实例
     */
    public AiCodeGeneratorService createAiCodeGeneratorService(Long appId) {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore) // 会话记忆的存储介质
                .maxMessages(MAX_COUNT) // 会话记忆维持的最大对话条数
                .build();
//         从数据库中加载会话记忆

        chatHistoryService.loadDataToChatMemory(appId, messageWindowChatMemory, MAX_COUNT);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                // 构建会话记忆
                .chatMemory(messageWindowChatMemory)
                .build();
    }
}
