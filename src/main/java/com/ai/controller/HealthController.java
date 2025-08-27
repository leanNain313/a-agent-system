package com.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ai.common.BaseResponse;
import com.ai.common.ResultUtils;
import com.ai.manager.auth.model.UserPermissionConstant;
import com.ai.utils.WebScreenImageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Tag(name = "健康接口")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "健康检查接口")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<String> healthTest() {
        return ResultUtils.success("health");
    }

    @PostMapping("/screen/tool/test")
    @Operation(summary = "截图工具测试")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<String> screenShotTest() {
        String testUrl = "https://www.codefather.cn";
        String webPageScreenshot = WebScreenImageUtils.executeScreenImage(testUrl);
        return ResultUtils.success("success:" + webPageScreenshot);
    }
}
