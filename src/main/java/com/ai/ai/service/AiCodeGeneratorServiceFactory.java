package com.ai.ai.service;

import com.ai.Exception.BusinessException;
import com.ai.Exception.ErrorCode;
import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.ai.tools.FileWriteTool;
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

    @Resource(name = "openAiStreamingChatModel")
    private StreamingChatModel openAiStreamingChatModel;

    @Resource(name = "reasoningStreamingChatModel")
    private StreamingChatModel reasoningStreamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    // 最大会话记忆轮数
    private static final int MAX_COUNT = 20;


    /**
     * 注册大模型, 创建大模型实例
     * @return 返回大模型实例
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return createAiCodeGeneratorService(2L, CodeGenTypeEnum.MULTI_FILE);
    }


    /**
     * Ai服务实例缓存
     */
    private final Cache<String, AiCodeGeneratorService> cache = Caffeine.newBuilder()
            .maximumSize(1000) // 储存实例的最大数量
            .expireAfterWrite(Duration.ofMinutes(30)) // 写入后30分钟过期
            .expireAfterAccess(Duration.ofMinutes(10)) // 10分钟不操作过期
            .removalListener(((key, value, cause) -> log.info("ai服务实例被移除, appid：{}， {}", key, cause))) // 实例移除监听器
            .build();

    /**
     * 取出实例没有则构建缓存
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        String cacheKey = buildKey(appId, codeGenTypeEnum);
        return cache.get(cacheKey, key -> createAiCodeGeneratorService(appId, codeGenTypeEnum));
    }

    /**
     * 创建ai实例
     * @param appId 应用id
     * @return 返回一个实例
     */
    public AiCodeGeneratorService createAiCodeGeneratorService(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore) // 会话记忆的存储介质
                .maxMessages(MAX_COUNT) // 会话记忆维持的最大对话条数
                .build();
//         从数据库中加载会话记忆
        chatHistoryService.loadDataToChatMemory(appId, messageWindowChatMemory, MAX_COUNT);
        return switch (codeGenTypeEnum) {
            case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(reasoningStreamingChatModel)
                    .chatMemoryProvider(memoryId -> messageWindowChatMemory) // 构建会会话记忆, 使用@Merory注解时必须这样构建
                    .tools(new FileWriteTool())
                    .build();
            case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                    .chatModel(chatModel)
                    .streamingChatModel(openAiStreamingChatModel)
                    .chatMemory(messageWindowChatMemory)
                    .build();
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持该类型代码生成");
        };
    }

    /**
     * 构建缓存键
     */
    private String buildKey(Long appId, CodeGenTypeEnum codeGenTypeEnum) {
        return codeGenTypeEnum.getValue() + "_" + appId.toString();
    }
}
