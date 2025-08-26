package com.ai.ai.core;

import com.ai.ai.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Test
    void generateAndSaveCode() {
        Flux<String> stringFlux = aiCodeGeneratorFacade.generateAndSaveCode("简单生成一个登录网页不超多20行， 标题是小y", CodeGenTypeEnum.MULTI_FILE, 3L);
        List<String> result = stringFlux.collectList().block();
        assertNotNull(result);
    }
}