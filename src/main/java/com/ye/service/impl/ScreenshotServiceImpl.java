package com.ye.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.ye.Exception.ErrorCode;
import com.ye.Exception.ThrowUtils;
import com.ye.manager.cos.CosManager;
import com.ye.service.ScreenshotService;
import com.ye.utils.WebScreenImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class ScreenshotServiceImpl implements ScreenshotService {

    private final CosManager cosManager;

    public ScreenshotServiceImpl(CosManager cosManager) {
        this.cosManager = cosManager;
    }

    /**
     * 自动截图
     *
     * @param webUrl 网页路由
     * @return 返回路由
     */
    @Override
    public String generateScreenshot(String webUrl) {
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.SYSTEM_ERROR, "网页的路由为空");
        // 生成本地截图
        String imagePath = WebScreenImageUtils.executeScreenImage(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(imagePath), ErrorCode.SYSTEM_ERROR, "截图失败！");
        try {
            String cosUrl = updateFileToCos(imagePath);
            ThrowUtils.throwIf(StrUtil.isBlank(cosUrl), ErrorCode.SYSTEM_ERROR, "cos上传失败");
            return cosUrl;
        } catch (Exception e) {
            log.error("cos上传失败:{}", e.getMessage());
        } finally {
            // 清理本地文件
            cleanupLocalFile(imagePath);
        }
        return null;
    }

    /**
     * 将文件上传到cos
     * @param filePath 文件路径
     * @return 返回路由
     */
    private String updateFileToCos(String filePath) {
        if (StrUtil.isBlank(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            log.warn("自动截图的图片不存在");
            return null;
        }
        String fileName = IdUtil.simpleUUID() + ".jpg";
        String cosKey = generateScreenshotKey(fileName);
        return cosManager.uploadFile(cosKey, file);
    }

    /**
     *
     * @param fileName 文件名称
     * @return 返回生成键
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清除本地文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            File parentDir = localFile.getParentFile();
            FileUtil.del(parentDir);
            log.info("本地截图文件已清理: {}", localFilePath);
        }
    }
}
