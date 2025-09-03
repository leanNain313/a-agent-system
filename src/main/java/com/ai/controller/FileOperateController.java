package com.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.Exception.ErrorCode;
import com.ai.Exception.ThrowUtils;
import com.ai.common.BaseResponse;
import com.ai.common.ResultUtils;
import com.ai.contant.RedisConstant;
import com.ai.contant.UserConstant;
import com.ai.manager.auth.model.UserPermissionConstant;
import com.ai.manager.cos.CosManager;
import com.ai.model.enums.DisabledTypeEnum;
import com.ai.model.vo.user.UserVO;
import com.ai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.ai.contant.AppConstant.IMAGE_TEMP_DIR;

@Slf4j
@RestController
@RequestMapping("/file")
@Tag(name = "文件操作接口")
public class FileOperateController {

    @Resource
    private CosManager cosManager;

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/upload")
    @SaCheckPermission(UserPermissionConstant.AI_USER)
    @Operation(summary = "上传用户头像")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile file) {
        ThrowUtils.throwIf(file.isEmpty(), ErrorCode.NULL_ERROR);
        // 校验该功能是否被封禁
        UserVO userVO = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(userVO == null, ErrorCode.NO_LOGIN);
        if (StpUtil.isDisable(userVO.getId())) {
            long disableTime = StpUtil.getDisableTime(userVO.getId(), DisabledTypeEnum.FILE_UPLOAD_TYPE.getValue());
            userService.disabledHandle(disableTime);
        }
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

    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码")
    public void getCheckCodeImage(HttpServletResponse response, @Email(message = "邮箱格式错误") String email) {
        ThrowUtils.throwIf(StrUtil.isEmpty(email), ErrorCode.NULL_ERROR, "请先填写邮箱");
        // 设置响应类型为图片
        response.setContentType("image/png");
        // 禁止缓存
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 创建干扰验证码
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(100, 30, 4, 4);
        // 设置为四则运算验证码
        MathGenerator mathGenerator = new MathGenerator();
        captcha.setGenerator(mathGenerator);
        // 生成验证码
        captcha.createCode();
        String code = captcha.getCode();
        String key = RedisConstant.LOGIN_CODE + email;
        // 获取表达式生成的结果
        redisTemplate.opsForValue().set(key, code, 300, TimeUnit.SECONDS); // 过期时间300s
        log.info("图形验证码的结果为：{}", code);
        try {
            // 输出到响应流
            captcha.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("验证码输出失败", e);
        } finally {
            try {
                response.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
