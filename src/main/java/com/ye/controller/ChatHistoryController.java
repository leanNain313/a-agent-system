package com.ye.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.ye.Exception.BusinessException;
import com.ye.Exception.ErrorCode;
import com.ye.common.BaseResponse;
import com.ye.common.ResultPage;
import com.ye.common.ResultUtils;
import com.ye.contant.UserConstant;
import com.ye.manager.auth.model.UserPermissionConstant;
import com.ye.model.dto.chat.AppChatHistoryPageRequest;
import com.ye.model.dto.chat.ChatHistoryAdminPageRequest;
import com.ye.model.vo.chat.ChatHistoryVO;
import com.ye.model.vo.user.UserVO;
import com.ye.service.ChatHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@Tag(name = "对话历史接口")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 应用内对话历史分页（仅创建者与管理员）
     */
    @GetMapping("/page/app")
    @Operation(summary = "应用内对话历史分页")
    @SaCheckPermission(UserPermissionConstant.AI_USER)
    public BaseResponse<ResultPage<ChatHistoryVO>> pageAppHistory(@ParameterObject AppChatHistoryPageRequest request) {
        if (request == null || request.getAppId() == null || request.getPageNo() == null
                || request.getPageSize() == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        UserVO loginUser = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        ResultPage<ChatHistoryVO> page = chatHistoryService.pageAppHistory(request, loginUser.getId());
        return ResultUtils.success(page);
    }

    /**
     * 管理员查看所有对话历史
     */
    @GetMapping("/admin/page/list")
    @Operation(summary = "管理员对话历史分页列表（按时间倒序）")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<ResultPage<ChatHistoryVO>> adminPage(@ParameterObject ChatHistoryAdminPageRequest request) {
        if (request == null || request.getPageNo() == null || request.getPageSize() == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        ResultPage<ChatHistoryVO> page = chatHistoryService.pageAdminHistory(request);
        return ResultUtils.success(page);
    }
}
