package com.ai.langGraph4j.node;

import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.ai.service.AiCodeGeneratorService;
import com.ai.ai.service.AiSmartRouterGeneratorService;
import com.ai.langGraph4j.state.WorkflowContext;
import com.ai.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;

@Slf4j
public class RouterNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return AsyncNodeAction.node_async(stringMessagesState -> {
            WorkflowContext context = WorkflowContext.getContext(stringMessagesState);
            log.info("智能路由节点执行");

            // 过去增强后的提示词
            CodeGenTypeEnum codeGenTypeEnum;
            try {
                AiSmartRouterGeneratorService aiSmartRouterGeneratorService = SpringContextUtil.getBean(AiSmartRouterGeneratorService.class);
                codeGenTypeEnum = aiSmartRouterGeneratorService.smartRouterSelect(context.getOriginalPrompt());
            } catch (Exception e) {
                log.error("Ai选择路由失败");
                codeGenTypeEnum = CodeGenTypeEnum.HTML;
            }
            // 模拟增强
            context.setGenerationType(codeGenTypeEnum.HTML);
            context.setCurrentStep("智能路由");
            return WorkflowContext.saveContext(context);
        });
    }
}
