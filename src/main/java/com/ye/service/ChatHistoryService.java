package com.ye.service;

import com.ye.model.entity.ChatHistory;
import com.ye.model.dto.chat.AppChatHistoryPageRequest;
import com.ye.model.dto.chat.ChatHistoryAdminPageRequest;
import com.ye.model.vo.chat.ChatHistoryVO;
import com.ye.common.ResultPage;
import com.baomidou.mybatisplus.extension.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * @author 惘念
 * @description 针对表【chat_history(对话历史)】的数据库操作Service
 * @createDate 2025-08-24 10:30:54
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    /** 保存用户消息 */
    void saveChatMessage(Long appId, Long userId, String message, String messageTypeEnum);

//    /** 保存 AI 回复消息 */
//    void saveAiMessage(Long appId, Long userId, String message);
//
//    /** 保存错误消息（AI 回复失败） */
//    void saveErrorMessage(Long appId, Long userId, String errorMessage);

    /** 应用内分页查询历史（仅应用创建者或管理员） */
    ResultPage<ChatHistoryVO> pageAppHistory(AppChatHistoryPageRequest request, Long requesterUserId);

    /** 管理员分页查询所有历史（按时间倒序） */
    ResultPage<ChatHistoryVO> pageAdminHistory(ChatHistoryAdminPageRequest request);

    /** 根据应用ID删除所有历史 */
    void removeByAppId(Long appId);

    int loadDataToChatMemory(Long appId, MessageWindowChatMemory messageWindowChatMemory, int maxCount);
}
