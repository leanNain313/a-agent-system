package com.ye.ai.service;

import com.ye.ai.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;

public interface AiSmartRouterGeneratorService {

    /**
     * ai智能选择路由
     * @param message 用户消息
     * @return 返回枚举类
     */
    @SystemMessage(fromResource = "prompt/smartRouter.txt")
    CodeGenTypeEnum smartRouterSelect(String message);

}
