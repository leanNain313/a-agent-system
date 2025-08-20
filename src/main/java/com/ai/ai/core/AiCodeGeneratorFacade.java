package com.ai.ai.core;

import com.ai.Exception.BusinessException;
import com.ai.Exception.ErrorCode;
import com.ai.ai.CodeFileSaver;
import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.ai.model.HtmlCodeResult;
import com.ai.ai.model.MultiFileCodeResult;
import com.ai.ai.service.AiCodeGeneratorService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 自动生成并保存
     * @param userMessage 用户消息
     * @param codeGenTypeEnum 生成模式
     */
    public Flux<String> generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        return switch (codeGenTypeEnum) {
            case HTML -> generateAndSaveMultiFileCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持该类型");
            }
        };
    }

    /**
     * html模式自动生成代码并保存
     * @param userMessage 用户信息
     */
//    private File generateAndSaveHtmlCode(String userMessage) {
//        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
//        return CodeFileSaver.saveHtmlResult(result);
//    }

    /**
     * 多文件模式自动生成代码并保存
     * @param userMessage
     * @return
     */
//   private File generateAndSaveMultiFileCode(String userMessage) {
//        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
//        return CodeFileSaver.saveMultiFileCodeResult(result);
//    }

    /**
     * html模式自动生成代码并保存(流式)
     * @param userMessage 用户消息
     * @return 原有的流
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCode(userMessage);
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

    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
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
}
