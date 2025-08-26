package com.ai.ai.core;

import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class StreamHandlerExecutor {

    @Resource
    private JsonMessageStreamHandler jsonMessageStreamHandler;

    /**
     * 处理传统流（HTML, MULTI_FILE）
     * 直接收集完整的文本响应
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param userId          登录用户
     * @return 处理后的流
     */
    public Flux<String> executeHandler(Flux<String> originFlux,
                                       ChatHistoryService chatHistoryService,
                                       long appId, Long userId, CodeGenTypeEnum codeGenTypeEnum) {
        return switch (codeGenTypeEnum) {
            case VUE_PROJECT -> jsonMessageStreamHandler.handleMessageStream(originFlux, chatHistoryService, appId, userId);
            case HTML, MULTI_FILE -> new SimpleTextStreamHandler().handleStream(originFlux, chatHistoryService, appId, userId);
        };
    }
}
