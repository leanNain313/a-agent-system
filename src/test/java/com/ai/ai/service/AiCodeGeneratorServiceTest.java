//package com.ai.ai.service;
//
//import com.ai.ai.CodeFileSaver;
//import com.ai.ai.model.HtmlCodeResult;
//import com.ai.ai.model.MultiFileCodeResult;
//import jakarta.annotation.Resource;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@Slf4j
//class AiCodeGeneratorServiceTest {
//
//    @Resource
//    private AiCodeGeneratorService aiCodeGeneratorService;
//
//    @Test
//    void generateHtmlCode() {
//        HtmlCodeResult result = aiCodeGeneratorService.("做个登录页面30行以内");
//        Assertions.assertNotNull(result);
//    }
//
//    @Test
//    void generateMultiFileCode() {
//        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode("做个登录页面30行以内");
//        CodeFileSaver.saveMultiFileCodeResult(result);
//        Assertions.assertNotNull(result);
//    }
//}