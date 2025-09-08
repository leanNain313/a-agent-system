package com.ye.ai.core;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.json.JSONUtil;
import com.ye.Exception.BusinessException;
import com.ye.Exception.ErrorCode;
import com.ye.Exception.ThrowUtils;
import com.ye.ai.CodeFileSaver;
import com.ye.ai.builder.VueProjectBuilder;
import com.ye.ai.enums.CodeGenTypeEnum;
import com.ye.ai.model.HtmlCodeResult;
import com.ye.ai.model.MultiFileCodeResult;
import com.ye.ai.model.message.AiResponseMessage;
import com.ye.ai.model.message.ToolExecutedMessage;
import com.ye.ai.model.message.ToolRequestMessage;
import com.ye.ai.parser.CodeParserExecutor;
import com.ye.ai.save.CodeFileSaverExecutor;
import com.ye.ai.service.AiCodeGeneratorService;
import com.ye.ai.service.AiCodeGeneratorServiceFactory;
import com.ye.contant.AppConstant;
import com.ye.contant.UserConstant;
import com.ye.model.vo.user.UserVO;
import com.ye.monitor.AiModelMetricsCollector;
import com.ye.monitor.MonitorContext;
import com.ye.monitor.MonitorContextHolder;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

/**
 * 门面类, 用以组装功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private AiModelMetricsCollector aiModelMetricsCollector;

    /**
     * 自动生成并保存
     *
     * @param userMessage     用户消息
     * @param codeGenTypeEnum 生成模式
     */
    public Flux<String> generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId,
                codeGenTypeEnum);
        ThrowUtils.throwIf(aiCodeGeneratorService == null, ErrorCode.PARAMS_ERROR);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCode(appId, userMessage);
                yield handleCodeStream(stringFlux, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCode(appId, userMessage);
                yield handleCodeStream(stringFlux, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCode(appId, userMessage);
                UserVO userVO = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
                yield handleTokenStream(tokenStream, appId, userVO.getId().toString());
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未知代码类型");
        };
    }

    /**
     * 处理token流，将其包装成Flux流，并添加AI模型统计功能
     */
    public Flux<String> handleTokenStream(TokenStream tokenStream, Long appId, String userId) {
        // 记录开始时间（用于响应时间统计）
        Instant startTime = Instant.now();

        return Flux.create(sink -> {
            tokenStream.onPartialResponse(response -> {
                        // ai回复内容
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(response);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecuteRequest) -> {
                        // 工具调用请求
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecuteRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted(toolExecution -> {
                        // 工具执行结果
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse(chatResponse -> {
                        // 获取监控上下文进行统计

                        String appId_str = appId.toString();
                        String modelName = chatResponse.modelName() != null ? chatResponse.modelName() : "unknown";

                        // 1. 记录AI模型请求成功
                        aiModelMetricsCollector.recordRequest(userId, appId_str, modelName, "success");

                        // 2. 记录Token消耗情况
                        if (chatResponse.metadata() != null && chatResponse.metadata().tokenUsage() != null) {
                            var tokenUsage = chatResponse.metadata().tokenUsage();
                            aiModelMetricsCollector.recordTokenUsage(userId, appId_str, modelName, "input",
                                    tokenUsage.inputTokenCount());
                            aiModelMetricsCollector.recordTokenUsage(userId, appId_str, modelName, "output",
                                    tokenUsage.outputTokenCount());
                            aiModelMetricsCollector.recordTokenUsage(userId, appId_str, modelName, "total",
                                    tokenUsage.totalTokenCount());


                            // 3. 记录响应时间
                            Duration responseTime = Duration.between(startTime, Instant.now());
                            aiModelMetricsCollector.recordResponseTime(userId, appId_str, modelName, responseTime);
                        }

                        // 构建应用
                        String dirPath = AppConstant.CODE_OUT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProject(dirPath);
                        // 执行完毕
                        sink.complete();
                    })
                    .onError(error -> {
                        // 获取监控上下文进行错误统计
                        String appId_str = appId.toString();
                        String modelName = "unknown"; // 错误时可能无法获取模型名称

                        // 4. 记录AI模型请求错误
                        aiModelMetricsCollector.recordRequest(userId, appId_str, modelName, "error");

                        // 5. 记录具体错误信息
                        aiModelMetricsCollector.recordError(userId, appId_str, modelName, error.getMessage());

                        // 记录响应时间（即使是错误响应）
                        Duration responseTime = Duration.between(startTime, Instant.now());
                        aiModelMetricsCollector.recordResponseTime(userId, appId_str, modelName, responseTime);

                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start(); // 开始生成
        });
    }

    /**
     * html模式自动生成代码并保存(流式)
     *
     * @param userMessage 用户消息
     * @return 原有的流
     */
    @Deprecated
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(1L,
                CodeGenTypeEnum.HTML);
        Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCode(1L, userMessage);
        // 但流式返回完毕时在保存代码
        StringBuilder stringBuilder = new StringBuilder();
        return stringFlux.doOnNext(chunk -> {
            // 拼接代码块
            stringBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式输出完成后保存代码
            String codeStr = stringBuilder.toString();
            HtmlCodeResult result = CodeParseTool.parseHtmlCode(codeStr);
            File file = CodeFileSaver.saveHtmlResult(result);
            log.info("html文件保存的路径：{}", file.getAbsolutePath());
        });
    }

    @Deprecated
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(1L,
                CodeGenTypeEnum.HTML);
        Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCode(1L, userMessage);
        StringBuilder stringBuilder = new StringBuilder();
        return stringFlux.doOnNext(chunk -> {
            // 拼接代码块
            stringBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式输出完成后保存代码
            String codeStr = stringBuilder.toString();
            MultiFileCodeResult result = CodeParseTool.parseMultiFileCode(codeStr);
            File file = CodeFileSaver.saveMultiFileCodeResult(result);
            log.info("文件保存的路径：{}", file.getAbsolutePath());
        });
    }

    /**
     * html模式自动生成代码并保存(流式)
     *
     * @param codeStream 返回的消息流
     * @return 原有的流
     */
    private Flux<String> handleCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        // 但流式返回完毕时在保存代码
        StringBuilder stringBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 拼接代码块
            stringBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式输出完成后保存代码
            String codeStr = stringBuilder.toString();
            Object result = CodeParserExecutor.executeParseTool(codeStr, codeGenTypeEnum);
            File file = CodeFileSaverExecutor.executeSaver(result, codeGenTypeEnum, appId);
            log.info("文件保存的路径：{}", file.getAbsolutePath());
        });
    }
}
