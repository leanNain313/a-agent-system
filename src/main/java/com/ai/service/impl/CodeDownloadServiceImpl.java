package com.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.ai.Exception.BusinessException;
import com.ai.Exception.ErrorCode;
import com.ai.Exception.ThrowUtils;
import com.ai.service.CodeDownloadService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileFilter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

@Service
@Slf4j
public class CodeDownloadServiceImpl implements CodeDownloadService {

    // 需要过滤的文件夹
    private static final Set<String> IGNORED_NAMES = Set.of("node_modules", ".git", "dist", "build", ".DS_Store", ".env", "target", ".mvn", ".idea", ".vscode");

    // 需要过滤的文件拓展名
    private static final Set<String> IGNORED_EXTENSIONS = Set.of(".log", ".tmp", ".cache");

    private boolean isPathAllowed(Path projectRoot, Path fullPath) {
        // 获取相对路径
        Path relativize = projectRoot.relativize(fullPath);
        for (Path part : relativize) {
            String partName = part.toString();

            // 检查是否在忽略名称列表中（比如 .git、node_modules）
            if (IGNORED_NAMES.contains(partName)) {
                return false;
            }

            // 检查文件扩展名是否在忽略列表中（比如 .class、.log）
            if (IGNORED_EXTENSIONS.stream().anyMatch(partName::endsWith)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 下载项目源码
     * @param rootPath 项目根路径
     * @param downloadName 下载文件的名称
     */
    @Override
    public void codeDownload(String rootPath, String downloadName, HttpServletResponse response) {
        ThrowUtils.throwIf(StrUtil.isBlank(rootPath) || StrUtil.isBlank(downloadName), ErrorCode.NULL_ERROR);
        File rootFile = new File(rootPath);
        ThrowUtils.throwIf(!rootFile.exists(), ErrorCode.SYSTEM_ERROR, "资源不存在下载失败");
        ThrowUtils.throwIf(!rootFile.isDirectory(), ErrorCode.PARAMS_ERROR, "项目路径不是一个目录");
        // 设置 HTTP 响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        response.addHeader("Content-Disposition",
                String.format("attachment; filename=\"%s.zip\"", downloadName));
        // 定义文件过滤器
        FileFilter filter = file -> isPathAllowed(rootFile.toPath(), file.toPath());
        // 压缩
        try{
            ZipUtil.zip(response.getOutputStream(), StandardCharsets.UTF_8, false,filter, rootFile);
            log.info("打包下载项目成功: {} -> {}.zip", rootPath, downloadName);
        } catch (Exception e) {
            log.error("压缩失败:{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "压缩失败");
        }
    }
}
