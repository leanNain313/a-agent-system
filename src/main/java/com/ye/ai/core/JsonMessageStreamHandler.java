package com.ye.ai.core;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ye.ai.builder.VueProjectBuilder;
import com.ye.ai.model.message.*;
import com.ye.ai.tools.BaseTool;
import com.ye.ai.tools.ToolManager;
import com.ye.model.enums.MessageTypeEnum;
import com.ye.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class JsonMessageStreamHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ToolManager toolManager;

    /**
     * 处理 TokenStream（VUE_PROJECT）
     * 解析 JSON 消息并重组为完整的响应格式
     *
     * @param originFlux         原始流
     * @param chatHistoryService 聊天历史服务
     * @param appId              应用ID
     * @param userId             登录用户
     * @return 处理后的流
     */
    public Flux<String> handleMessageStream(Flux<String> originFlux,
                               ChatHistoryService chatHistoryService,
                               long appId, Long userId) {
        // 收集数据用于生成后端记忆格式
        StringBuilder chatHistoryStringBuilder = new StringBuilder();
        // 用于跟踪已经见过的工具ID，判断是否是第一次调用
        Set<String> seenToolIds = new HashSet<>();
        return originFlux
                .map(chunk -> {
                    // 解析每个 JSON 消息块
                    return handleJsonMessageChunk(chunk, chatHistoryStringBuilder, seenToolIds);
                })
                .filter(StrUtil::isNotEmpty) // 过滤空字串
                .doOnComplete(() -> {
                    // 流式响应完成后，添加 AI 消息到对话历史
                    String aiResponse = chatHistoryStringBuilder.toString();
                    chatHistoryService.saveChatMessage(appId, userId, aiResponse, MessageTypeEnum.AI.getValue());
//                    // 构建应用
//                    String dirPath = AppConstant.CODE_OUT_DIR + "/vue_project_" + appId;
//                    vueProjectBuilder.buildProject(dirPath);
                })
                .doOnError(error -> {
                    // 如果AI回复失败，也要记录错误消息
                    String errorMessage = "AI回复失败: " + error.getMessage();
                    chatHistoryService.saveChatMessage(appId, userId, errorMessage, MessageTypeEnum.AI.getValue());
                });
    }

    /**
     * 解析并收集 TokenStream 数据
     */
    private String handleJsonMessageChunk(String chunk, StringBuilder chatHistoryStringBuilder, Set<String> seenToolIds) {
        // 解析 JSON
        StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
        StreamMessageTypeEnum typeEnum = StreamMessageTypeEnum.getEnumByValue(streamMessage.getType());
        switch (typeEnum) {
            case AI_RESPONSE -> {
                AiResponseMessage aiMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                String data = aiMessage.getData();
                // 直接拼接响应
                chatHistoryStringBuilder.append(data);
                return data;
            }
            case TOOL_REQUEST -> {
                ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                String toolId = toolRequestMessage.getId();
                // 检查是否是第一次看到这个工具 ID
//                if (toolId != null && !seenToolIds.contains(toolId)) {
//                    // 第一次调用这个工具，记录 ID 并完整返回工具信息
//                    seenToolIds.add(toolId);
//                    BaseTool tool = toolManager.getTool(toolRequestMessage.getName());
//                    return tool.getToolRequest();
//                } else {
//                    // 不是第一次调用这个工具，直接返回空
//                    return "";
//                }
                BaseTool tool = toolManager.getTool(toolRequestMessage.getName());
                // TODO: 测试使用, 生产环境需要去掉
                chatHistoryStringBuilder.append(toolRequestMessage.getArguments());
                    return tool.getToolRequest();

            }
            case TOOL_EXECUTED -> {
                ToolExecutedMessage toolExecutedMessage = JSONUtil.toBean(chunk, ToolExecutedMessage.class);
                JSONObject jsonObject = JSONUtil.parseObj(toolExecutedMessage.getArguments());
                // 获取工具实例
                BaseTool tool = toolManager.getTool(toolExecutedMessage.getName());
                tool.getToolExecutedResult(jsonObject);
//                String relativeFilePath = jsonObject.getStr("relativeFilePath");
//                String suffix = FileUtil.getSuffix(relativeFilePath);
//                String content = jsonObject.getStr("content");
                String result = tool.getToolExecutedResult(jsonObject);
                // 输出前端和要持久化的内容
                String output = String.format("\n\n%s\n\n", result);
                chatHistoryStringBuilder.append(output);
                return output;
            }
            default -> {
                log.error("不支持的消息类型: {}", typeEnum);
                return "";
            }
        }
    }
}
