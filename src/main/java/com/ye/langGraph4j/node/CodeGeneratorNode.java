package com.ye.langGraph4j.node;

import com.ye.ai.core.AiCodeGeneratorFacade;
import com.ye.contant.AppConstant;
import com.ye.langGraph4j.state.WorkflowContext;
import com.ye.utils.SpringContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Slf4j
public class CodeGeneratorNode {

    public static AsyncNodeAction<MessagesState<String>> create() {
        return AsyncNodeAction.node_async(stringMessagesState -> {
            WorkflowContext context = WorkflowContext.getContext(stringMessagesState);
            log.info("代码生成节点执行");

            // TODO 实际增强提示词的逻辑
            AiCodeGeneratorFacade aiCodeGeneratorFacade = SpringContextUtil.getBean(AiCodeGeneratorFacade.class);
            // 默认为0
            Long appId = 0L;
            Flux<String> stringFlux = aiCodeGeneratorFacade.generateAndSaveCode(context.getEnhancedPrompt(), context.getGenerationType(), appId);
            stringFlux.blockLast(Duration.ofMinutes(10)); // 最大等待10分钟
            String generatedCodeDir = String.format("%s/%s_%s", AppConstant.CODE_OUT_DIR, context.getGenerationType().getValue(), appId);
            log.info("AI 代码生成完成，生成目录: {}", generatedCodeDir);
            // 模拟处理
            context.setGeneratedCodeDir(generatedCodeDir);
            context.setCurrentStep("代码生成");
            return WorkflowContext.saveContext(context);
        });
    }
}
