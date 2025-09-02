package com.ai.ai.core;

import cn.hutool.json.JSONUtil;
import com.ai.Exception.BusinessException;
import com.ai.Exception.ErrorCode;
import com.ai.Exception.ThrowUtils;
import com.ai.ai.CodeFileSaver;
import com.ai.ai.builder.VueProjectBuilder;
import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.ai.model.HtmlCodeResult;
import com.ai.ai.model.MultiFileCodeResult;
import com.ai.ai.model.message.AiResponseMessage;
import com.ai.ai.model.message.ToolExecutedMessage;
import com.ai.ai.model.message.ToolRequestMessage;
import com.ai.ai.parser.CodeParserExecutor;
import com.ai.ai.save.CodeFileSaverExecutor;
import com.ai.ai.service.AiCodeGeneratorService;
import com.ai.ai.service.AiCodeGeneratorServiceFactory;
import com.ai.contant.AppConstant;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

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


    /**
     * 自动生成并保存
     * @param userMessage 用户消息
     * @param codeGenTypeEnum 生成模式
     */
    public Flux<String> generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appId) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        ThrowUtils.throwIf(aiCodeGeneratorService == null, ErrorCode.PARAMS_ERROR);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield handleCodeStream(stringFlux, CodeGenTypeEnum.HTML, appId);
            }
            case MULTI_FILE -> {
                Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield handleCodeStream(stringFlux, CodeGenTypeEnum.MULTI_FILE, appId);
            }
            case VUE_PROJECT -> {
                TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCode(appId, userMessage);
                yield handleTokenStream(tokenStream,appId);
            }
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "未知代码类型");
        };
    }

    /**
     * 处理token流，将其包装成Flux流
     */

    public Flux<String> handleTokenStream(TokenStream tokenStream, Long appId) {
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
                        // 构建应用
                        String dirPath = AppConstant.CODE_OUT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProject(dirPath);
                        // 执行完毕
                        sink.complete();
                    })
                    .onError(error -> {
                        // 执行错误
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start(); // 开始生成
        });
    }

    /**
     * html模式自动生成代码并保存(流式)
     * @param userMessage 用户消息
     * @return 原有的流
     */
    @Deprecated
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(1L,CodeGenTypeEnum.HTML);
        Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCode( userMessage);
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
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(1L, CodeGenTypeEnum.HTML);
        Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCode(userMessage);
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
