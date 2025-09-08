package com.ye.langGraph4j.ai;

import com.ye.langGraph4j.model.QualityResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 代码质量检查服务
 */
public interface CodeQualityCheckService {

    /**
     * 检查代码质量
     * AI 会分析代码并返回质量检查结果
     */
    @SystemMessage(fromResource = "prompt/imageCollection.txt")
    QualityResult checkCodeQuality(@UserMessage String codeContent);
}
