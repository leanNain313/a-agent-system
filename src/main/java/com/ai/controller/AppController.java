package com.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ai.Exception.BusinessException;
import com.ai.Exception.ErrorCode;
import com.ai.Exception.ThrowUtils;
import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.common.BaseResponse;
import com.ai.common.ResultPage;
import com.ai.common.ResultUtils;
import com.ai.contant.UserConstant;
import com.ai.manager.auth.model.UserPermissionConstant;
import com.ai.model.dto.app.*;
import com.ai.model.vo.app.AppVO;
import com.ai.model.vo.user.UserVO;
import com.ai.service.AppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/app")
@Tag(name = "应用接口")
public class AppController {

    @Resource
    private AppService appService;

    /**
     * 用户创建应用（须填写 initPrompt）
     */
    @PostMapping("/create")
    @Operation(summary = "用户创建应用")
    @SaCheckPermission(UserPermissionConstant.AI_USER)
    public BaseResponse<Long> createApp(@RequestBody AppCreateRequest request) {
        if (request == null || StrUtil.isBlank(request.getInitPrompt())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(CodeGenTypeEnum.getEnumByValue(request.getCodeType()) == null, ErrorCode.PARAMS_ERROR);
        UserVO loginUser = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        Long id = appService.createApp(request, loginUser.getId());
        return ResultUtils.success(id);
    }

    /**
     * 用户根据 id 修改自己的应用（仅名称）
     */
    @PutMapping("/update/my")
    @Operation(summary = "用户修改自己的应用名称")
    @SaCheckPermission(UserPermissionConstant.AI_USER)
    public BaseResponse<Object> updateMyApp(@RequestBody AppUpdateByUserRequest request) {
        if (request == null || request.getId() == null || StrUtil.isBlank(request.getAppName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO loginUser = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        appService.updateMyAppById(request, loginUser.getId());
        return ResultUtils.success();
    }

    /**
     * 用户根据 id 删除自己的应用
     */
    @PostMapping("/delete/my")
    @Operation(summary = "用户删除自己的应用")
    @SaCheckPermission(UserPermissionConstant.AI_USER)
    public BaseResponse<Object> deleteMyApp(@Parameter(description = "应用id(必须)") Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        UserVO loginUser = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        appService.removeMyAppById(id, loginUser.getId());
        return ResultUtils.success();
    }

    /**
     * 用户根据 id 查看应用详情
     */
    @GetMapping("/get/detail")
    @Operation(summary = "根据ID获取应用详情")
    @SaCheckPermission(UserPermissionConstant.AI_USER)
    public BaseResponse<AppVO> getDetail(@Parameter(description = "应用id(必须)") Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long loginUserId = null;
        UserVO loginUser = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        if (loginUser != null) {
            loginUserId = loginUser.getId();
        }
        AppVO vo = appService.getAppDetailById(id, loginUserId);
        return ResultUtils.success(vo);
    }

    /**
     * 用户分页查询自己的应用列表（支持名称查询，每页最多20）
     */
    @GetMapping("/page/my")
    @Operation(summary = "我的应用分页列表")
    @SaCheckPermission(UserPermissionConstant.AI_USER)
    public BaseResponse<ResultPage<AppVO>> pageMy(@ParameterObject AppPageMyRequest request) {
        if (request == null || ObjectUtil.isEmpty(request.getPageNo()) || ObjectUtil.isEmpty(request.getPageSize())) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        UserVO loginUser = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        ResultPage<AppVO> page = appService.pageMyApps(request, loginUser.getId());
        return ResultUtils.success(page);
    }

    /**
     * 用户分页查询精选应用列表（支持名称查询，每页最多20）
     */
    @GetMapping("/page/featured")
    @Operation(summary = "精选应用分页列表")
    public BaseResponse<ResultPage<AppVO>> pageFeatured(@ParameterObject AppPageRequest request) {
        if (request == null || ObjectUtil.isEmpty(request.getPageNo()) || ObjectUtil.isEmpty(request.getPageSize())) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        ResultPage<AppVO> page = appService.pageFeaturedApps(request);
        return ResultUtils.success(page);
    }

    /**
     * 管理员根据 id 删除任意应用
     */
    @PostMapping("/admin/delete")
    @Operation(summary = "管理员删除应用")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<Object> adminDelete(@Parameter(description = "应用id(必须)") Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        appService.removeAppByAdmin(id);
        return ResultUtils.success();
    }

    /**
     * 管理员根据 id 更新任意应用（名称、封面、优先级）
     */
    @PutMapping("/admin/update")
    @Operation(summary = "管理员更新应用")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<Object> adminUpdate(@RequestBody AppAdminUpdateRequest request) {
        if (request == null || request.getId() == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        appService.updateAppByAdmin(request);
        return ResultUtils.success();
    }

    /**
     * 管理员分页查询应用列表（支持除时间外的任何字段）
     */
    @GetMapping("/admin/page/list")
    @Operation(summary = "管理员分页查询应用列表")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<ResultPage<AppVO>> adminPage(@ParameterObject AppPageRequest request) {
        if (request == null || ObjectUtil.isEmpty(request.getPageNo()) || ObjectUtil.isEmpty(request.getPageSize())) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        ResultPage<AppVO> page = appService.pageAppsByAdmin(request);
        return ResultUtils.success(page);
    }

    /**
     * 管理员根据 id 查看应用详情
     */
    @GetMapping("/admin/get/detail")
    @Operation(summary = "管理员查看应用详情")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<AppVO> adminGetDetail(@Parameter(description = "应用id(必须)") Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        AppVO vo = appService.getAppDetailByAdmin(id);
        return ResultUtils.success(vo);
    }

    @Operation(summary = "聊天生成代码接口")
    @GetMapping(value = "/chat/generate/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatGenerateCode(AppChatRequest request) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.NULL_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(request.getMessage()) || request.getId() == null
                || StrUtil.isBlank(request.getCodeType()), ErrorCode.NULL_ERROR);
        ThrowUtils.throwIf(CodeGenTypeEnum.getEnumByValue(request.getCodeType()) == null, ErrorCode.PARAMS_ERROR);
        UserVO loginUser = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_LOGIN);
        Flux<String> stringFlux = appService.chatGenerateCode(request, loginUser.getId());
        return stringFlux.map(chunk -> {
            // 分装返回内容
            Map<String, String> data = Map.of("v", chunk);
            String jsonStr = JSONUtil.toJsonStr(data);
            return ServerSentEvent.<String>builder()
                    .data(jsonStr)
                    .build();
        }).concatWith(Mono.just(
                // 发送结束事件
                ServerSentEvent.<String>builder()
                        .event("done")
                        .data("")
                        .build()));
    }

    @SaCheckPermission(UserPermissionConstant.AI_DEPLOY)
    @Operation(summary = "一键部署网页")
    @PostMapping("/deploy")
    public BaseResponse<Object> deployWeb(@Parameter(description = "应用id") Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.NULL_ERROR);
        UserVO userVO = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(userVO == null, ErrorCode.NO_LOGIN);
        String url = appService.deployWeb(id, userVO);
        return ResultUtils.success(url);
    }
}
