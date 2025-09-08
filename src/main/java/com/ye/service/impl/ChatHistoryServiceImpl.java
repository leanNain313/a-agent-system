package com.ye.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ye.Exception.BusinessException;
import com.ye.Exception.ErrorCode;
import com.ye.Exception.ThrowUtils;
import com.ye.common.ResultPage;
import com.ye.mapper.ChatHistoryMapper;
import com.ye.model.dto.chat.AppChatHistoryPageRequest;
import com.ye.model.dto.chat.ChatHistoryAdminPageRequest;
import com.ye.model.entity.App;
import com.ye.model.entity.ChatHistory;
import com.ye.model.enums.MessageTypeEnum;
import com.ye.model.vo.chat.ChatHistoryVO;
import com.ye.service.AppService;
import com.ye.service.ChatHistoryService;
import com.ye.service.UserService;
import com.ye.model.vo.user.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author 惘念
 * @description 针对表【chat_history(对话历史)】的数据库操作Service实现
 * @createDate 2025-08-24 10:30:54
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
        implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    @Resource
    private UserService userService;

    @Override
    public void saveChatMessage(Long appId, Long userId, String message, String messageTypeEnum) {
        ThrowUtils.throwIf(appId == null || userId == null || StrUtil.isBlank(message), ErrorCode.NULL_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(messageTypeEnum), ErrorCode.NULL_ERROR);
        MessageTypeEnum enumByValue = MessageTypeEnum.getEnumByValue(messageTypeEnum);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR);
        // 仅应用创建者允许写入
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.SYSTEM_ERROR, "应用不存在");
        if (!userId.equals(app.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        saveMessage(appId, userId, message, enumByValue);
    }

    @Override
    public ResultPage<ChatHistoryVO> pageAppHistory(AppChatHistoryPageRequest request, Long requesterUserId) {
        ThrowUtils.throwIf(request == null || request.getAppId() == null, ErrorCode.NULL_ERROR);
        App app = appService.getById(request.getAppId());
        ThrowUtils.throwIf(app == null, ErrorCode.SYSTEM_ERROR, "应用不存在");
        UserVO requester = userService.getUserDetailById(requesterUserId);
        // 仅创建者或管理员可见
        boolean isOwner = requester != null && requester.getId().equals(app.getUserId());
        boolean isAdmin = requester != null && StrUtil.equalsIgnoreCase(requester.getUserRole(), "admin");
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Integer pageNo = ObjectUtil.defaultIfNull(request.getPageNo(), 1);
        Integer pageSize = ObjectUtil.defaultIfNull(request.getPageSize(), 10);
        if (pageSize > 20) {
            pageSize = 20;
        }
        QueryWrapper<ChatHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("app_id", request.getAppId());
        wrapper.orderByDesc("create_time");
        Page<ChatHistory> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        List<ChatHistoryVO> list = BeanUtil.copyToList(page.getRecords(), ChatHistoryVO.class);
        ResultPage<ChatHistoryVO> rp = new ResultPage<>();
        rp.setData(list);
        rp.setTotal(page.getTotal());
        return rp;
    }

    @Override
    public ResultPage<ChatHistoryVO> pageAdminHistory(ChatHistoryAdminPageRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.NULL_ERROR);
        Integer pageNo = ObjectUtil.defaultIfNull(request.getPageNo(), 1);
        Integer pageSize = ObjectUtil.defaultIfNull(request.getPageSize(), 10);
        QueryWrapper<ChatHistory> wrapper = new QueryWrapper<>();
        if (request.getAppId() != null) {
            wrapper.eq("app_id", request.getAppId());
        }
        wrapper.orderByDesc("create_time");
        Page<ChatHistory> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        List<ChatHistoryVO> list = BeanUtil.copyToList(page.getRecords(), ChatHistoryVO.class);
        ResultPage<ChatHistoryVO> rp = new ResultPage<>();
        rp.setData(list);
        rp.setTotal(page.getTotal());
        return rp;
    }

    @Override
    public void removeByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null, ErrorCode.NULL_ERROR);
        QueryWrapper<ChatHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("app_id", appId);
        this.remove(wrapper);
    }

    @Override
    public int loadDataToChatMemory(Long appId, MessageWindowChatMemory messageWindowChatMemory, int maxCount) {
        try {
            QueryWrapper<ChatHistory> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("app_id", appId);
            queryWrapper.orderByDesc("create_time");
            String sql = String.format("limit 1, %d", maxCount);
            queryWrapper.last(sql);
            List<ChatHistory> list = this.list(queryWrapper);
            if (CollUtil.isEmpty(list)) {
                return 0;
            }
            // 反转时间老的在前面。新的在后面
            List<ChatHistory> chatHistoryList = list.reversed();
            int loadedCount = 0;
            // 清理缓存 防止重复加载
            messageWindowChatMemory.clear();
            for (ChatHistory chatHistory : chatHistoryList) {
                if (MessageTypeEnum.AI.equals(chatHistory.getMessageType())) {
                    messageWindowChatMemory.add(AiMessage.from(chatHistory.getMessage()));
                    loadedCount++;
                } else if (MessageTypeEnum.USER.equals(chatHistory.getMessageType())){
                    messageWindowChatMemory.add(UserMessage.from(chatHistory.getMessage()));
                    loadedCount++;
                }
            }
            log.info("appId:{}, 成功加载了：{}条消息", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("会话记忆加载失败：{}", e.getMessage());
            return 0;
        }
    }

    /**
     * 保存会话消息
     *
     * @param appId   应用id
     * @param userId  用户id
     * @param message 信息
     * @param type    消息类型：ye, user
     */
    private void saveMessage(Long appId, Long userId, String message, MessageTypeEnum type) {
        ChatHistory record = new ChatHistory();
        record.setAppId(appId);
        record.setUserId(userId);
        record.setMessage(message);
        record.setMessageType(type.getValue());
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        boolean ok = this.save(record);
        ThrowUtils.throwIf(!ok, ErrorCode.SYSTEM_ERROR, "保存对话失败");
    }
}
