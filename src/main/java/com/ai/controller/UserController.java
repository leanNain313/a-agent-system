package com.ai.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;

import com.ai.Exception.BusinessException;
import com.ai.Exception.ErrorCode;
import com.ai.Exception.ThrowUtils;
import com.ai.common.BaseResponse;
import com.ai.common.ResultPage;
import com.ai.common.ResultUtils;
import com.ai.contant.UserConstant;
import com.ai.manager.auth.model.UserPermissionConstant;
import com.ai.model.dto.user.*;
import com.ai.model.vo.user.UserVO;
import com.ai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.security.PermissionCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户接口")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 用户注册
     * @param userRegisterRequest 请求封装
     * @return 1成功 0失败
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public BaseResponse<Object> userRegister(@RequestBody RegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.register(userRegisterRequest);
        return ResultUtils.success();
    }

    /**
     * 用户登录
     * @param userDoLoginRequest 请求封装类
     * @param request 网络请求，获取session
     * @return 返回用户信息（脱敏）
     */
    @PostMapping("/doLogin")
    @Operation(summary = "用户登录")
    public BaseResponse<UserVO> userDoLogin(@RequestBody LoginRequest userDoLoginRequest, HttpServletRequest request) {
        if (ObjectUtil.isEmpty(userDoLoginRequest) || ObjectUtil.isEmpty(request)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        UserVO result =  userService.doLogin(userDoLoginRequest, request);
        log.info("登录用户账户：{}", result.getUserAccount());
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户信息
     * @return 返回一个用户信息
     */
    @PostMapping("/getCurrentUser")
    @Operation(summary = "获取当前登录用户")
    public BaseResponse<UserVO> getCurrentUser() {
        UserVO userVO = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        if (userVO == null) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        return ResultUtils.success(userVO);
    }

    /**
     * 用户登出
     * @param request 请求
     * @return 返回结果
     */
    @PostMapping("/loginOut")
    @Operation(summary = "退出登录")
    public BaseResponse<Object> loginOut(HttpServletRequest request) {
        userService.loginOut(request);
        return ResultUtils.success();
    }

    /**
     * 管理员修改用户信息
     *
     * @param updateUserRequest 请求参数
     * @param request 请求
     */
    @PutMapping("/update/user")
    @Operation(summary = "管理员修改用户信息")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<Object> updateUserByAdmin(@RequestBody UpdateUserRequest updateUserRequest, HttpServletRequest request) {
        if (ObjectUtil.isEmpty(updateUserRequest)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (ObjectUtil.isEmpty(updateUserRequest.getId())) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        userService.updateUserByAdmin(updateUserRequest, request);
        return ResultUtils.success();
    }

    /**
     * 用户修改信息
     *
     * @param updateUserRequest 请求参数
     * @param request 请求
     */
    @PutMapping("/update/admin")
    @Operation(summary = "用户自己修改个人信息")
    public BaseResponse<Object> updateUserByUser(@RequestBody UpdateUserRequest updateUserRequest, HttpServletRequest request) {
        if (ObjectUtil.isEmpty(updateUserRequest)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (updateUserRequest.getId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "操作失败");
        }
        UserVO currentUser = userService.getCurrentUser(request);
        if (!currentUser.getId().equals(updateUserRequest.getId())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "操作失败");
        }
        userService.updateUserByUser(updateUserRequest, request);
        return ResultUtils.success();
    }

    /**
     * 移除用户
     *
     * @param id 用户id
     * @return
     */
    @PostMapping("/delete")
    @Operation(summary = "删除用户(管理员)")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<Object> removeUserById(Long id, HttpServletRequest request) {
        if (ObjectUtil.isEmpty(id)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        userService.removeUserById(id, request);
        return ResultUtils.success();
    }

    /**
     * 获取用户分页列表
     *
     * @param userPageRequest 用户分页请求
     * @return 用户分页列表
     */
    @GetMapping("/page/list")
    @Operation(summary = "获取用户分页列表（管理员)")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<ResultPage<UserVO>> getUserList(UserPageRequest userPageRequest) {
        if (ObjectUtil.isEmpty(userPageRequest)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (ObjectUtil.isEmpty(userPageRequest.getPageNo()) || ObjectUtil.isEmpty(userPageRequest.getPageSize())) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        ResultPage<UserVO> userList = userService.getUserList(userPageRequest);
        return ResultUtils.success(userList);
    }

    /**
     * 添加用户信息
     *
     * @param addUserRequest 用户请求信息封装类
     */
    @PostMapping("/add")
    @Operation(summary = "添加用户（管理员）")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<Object> addUser(AddUserRequest addUserRequest) {
        if (ObjectUtil.isEmpty(addUserRequest)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (StrUtil.isBlank(addUserRequest.getUserAccount()) ||
        StrUtil.isBlank(addUserRequest.getUserPassword()) ||
        StrUtil.isBlank(addUserRequest.getUserRole()) ||
        StrUtil.isBlank(addUserRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        userService.addUser(addUserRequest);
        return ResultUtils.success();
    }

    @PostMapping("/disable")
    @Operation(summary = "账户封禁接口")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<Object> disabledUser(AccountDisableRequest accountDisableRequest) {
        ThrowUtils.throwIf(accountDisableRequest == null, ErrorCode.NULL_ERROR);
        userService.disabledUser(accountDisableRequest);
        return ResultUtils.success();
    }
}
