package com.ai.ai;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.ai.model.HtmlCodeResult;
import com.ai.ai.model.MultiFileCodeResult;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;

@Component
public class CodeFileSaver {

    // 文件保存根目录
    private static final String FILE_SAVE_DIR = System.getProperty("user.dir") + "/templates/code_output";

    /**
     * 保存html生成的代码结果
     */
    public static File saveHtmlResult(HtmlCodeResult result) {
        String baseDirPath = buildUnique(CodeGenTypeEnum.HTML.getValue());
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        return new File(baseDirPath);
    }


    /**
     * 保存 MultiFileCodeResult
     */
    public static File saveMultiFileCodeResult(MultiFileCodeResult result) {
        String baseDirPath = buildUnique(CodeGenTypeEnum.MULTI_FILE.getValue());
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
        return new File(baseDirPath);
    }

    /**
     * 构建目录的唯一雪花id
     */
    private static String buildUnique(String bizType) {
        String uniqueDirName = String.format("%s_%s", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    private static void writeToFile(String dirPath, String fileName, String content) {
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }


}
