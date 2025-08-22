package com.ai.ai.save;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.ai.Exception.ErrorCode;
import com.ai.Exception.ThrowUtils;
import com.ai.ai.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * 抽象代码保存方法, 模板方法模式
 * @param <T>
 */
public abstract class CodeFileSaverTemplate<T> {

    // 文件保存根目录
    protected static final String FILE_SAVE_DIR = System.getProperty("user.dir") + "/templates/code_output";

    public final File saveCode(T result) {
        // 校验参数
        validateInput(result);
        // 构建唯一目录
        String dirPath = buildUnique();
        // 保存文件
        saveFiles(result ,dirPath);
        return new File(dirPath);
    }

    /**
     * 参数校验
     * @param result 代码解析结果
     */
    protected void validateInput(T result) {
        ThrowUtils.throwIf(result == null, ErrorCode.NULL_ERROR, "代码解析结果为空");
    }

    /**
     * 构建目录的唯一雪花id
     */
    protected final String buildUnique() {
        String bizType = this.getCodeType().getValue();
        String uniqueDirName = String.format("%s_%s", bizType, IdUtil.getSnowflakeNextIdStr());
        String dirPath = FILE_SAVE_DIR + File.separator + uniqueDirName;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 写入单个文件
     */
    protected final void writeToFile(String dirPath, String fileName, String content) {
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
    }

    /**
     * 保存文件， 具体有子类实现
     * @param result 解析结果
     * @param dirPath 文件路径
     */
    protected abstract void saveFiles(T result, String dirPath);

    /**
     * 获取代码生成类型
     */
    protected abstract CodeGenTypeEnum getCodeType();
}
