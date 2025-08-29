package com.ai.langGraph4j.node;

import com.ai.langGraph4j.ai.ImageCollectionService;
import com.ai.langGraph4j.model.ImageResource;
import com.ai.langGraph4j.model.enums.ImageCategoryEnum;
import com.ai.langGraph4j.state.WorkflowContext;
import com.ai.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

import java.util.Arrays;
import java.util.List;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图片收集节点
 */
@Slf4j
public class ImageCollectorNode {
    public static AsyncNodeAction<MessagesState<String>> create() {
        return node_async(state -> {
            WorkflowContext context = WorkflowContext.getContext(state);
            log.info("执行节点: 图片收集");
            String prompt = context.getOriginalPrompt();
            // 获取框架代理的模型实例
            ImageCollectionService imageCollectionService = SpringContextUtil.getBean(ImageCollectionService.class);
            // 调用Ai服务进行收集
            String imageStr = "";
            try {
                imageStr = imageCollectionService.collectImages(prompt);
            } catch (Exception e) {
                log.error("图片收集失败：{}", e.getMessage());
            }
            context.setCurrentStep("图片收集");
            context.setImageListStr(imageStr);
            return WorkflowContext.saveContext(context);
        });
    }
}
