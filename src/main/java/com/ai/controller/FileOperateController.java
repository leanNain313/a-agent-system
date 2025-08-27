package com.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.ai.Exception.ErrorCode;
import com.ai.Exception.ThrowUtils;
import com.ai.common.BaseResponse;
import com.ai.common.ResultUtils;
import com.ai.manager.auth.model.UserPermissionConstant;
import com.ai.manager.cos.CosManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

import static com.ai.contant.AppConstant.IMAGE_TEMP_DIR;

@RestController
@RequestMapping("/file")
@Tag(name = "文件操作接口")
public class FileOperateController {

    @Resource
    private CosManager cosManager;

    @PostMapping("/upload")
    @SaCheckPermission(UserPermissionConstant.AI_USER)
    @Operation(summary = "上传用户头像")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile file) {
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.NULL_ERROR);
        String contentType = file.getContentType();
        String substring = "." + contentType.substring(contentType.length() - 4);
        String filePath = String.format("/avatar/%s", IdUtil.fastSimpleUUID() + substring);
        File tempFile = null;
        String tempPath = IMAGE_TEMP_DIR + File.separator + IdUtil.simpleUUID();
        try {
            // 创建按一个临时文件
            tempFile = File.createTempFile(tempPath, substring);
            file.transferTo(tempFile);
            // 上传文件
            String url = cosManager.uploadFile(filePath, tempFile);
            return ResultUtils.success(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // 删临时文件
            cosManager.deleteTempFile(tempFile);
        }
    }
}
