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
import com.ai.model.enums.AuthCodeType;
import com.ai.model.vo.user.UserVO;
import com.ai.service.EmailService;
import com.ai.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.StringUtil;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户接口")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private EmailService emailService;


    /**
     * 用户注册
     * @param userRegisterRequest 请求封装
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
     * 用户注册
     * @param userRegisterRequest 请求封装
     */
    @PostMapping("/reset")
    @Operation(summary = "重置密码")
    public BaseResponse<Object> userResetPassword(@RequestBody RegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.register(userRegisterRequest);
        return ResultUtils.success();
    }

    @Operation(summary = "发送验证码，有效期5分钟")
    @PostMapping("/code")
    public BaseResponse<Object> send(@Parameter(description = "邮箱") @Email String email, @Parameter(description = "验证码类型：" +
            "'login', 'register', 'reset'") String type) {
        ThrowUtils.throwIf(StrUtil.isBlank(email) || StrUtil.isBlank(type), ErrorCode.NULL_ERROR);
        ThrowUtils.throwIf(AuthCodeType.getEnumByValue(type) == null, ErrorCode.PARAMS_ERROR);
        // 防止刷验证码
        String emailCode = emailService.getEmailCode(email, type);
        if (!StrUtil.isBlank(emailCode)) {
            Long codeExpire = emailService.getCodeExpire(email, type);
            ThrowUtils.throwIf(codeExpire.compareTo(240L) > 0, ErrorCode.SYSTEM_ERROR, "操作频繁， 请稍后重试");
        }
        boolean b = emailService.sendEmailCode(email, type);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "验证码发送失败");
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
     */
    @PutMapping("/update/user")
    @Operation(summary = "管理员修改用户信息")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<Object> updateUserByAdmin(@RequestBody UpdateUserRequest updateUserRequest) {
        if (ObjectUtil.isEmpty(updateUserRequest)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (ObjectUtil.isEmpty(updateUserRequest.getId())) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        userService.updateUserByAdmin(updateUserRequest);
        return ResultUtils.success();
    }

    /**
     * 用户修改信息
     *
     * @param updateUserRequest 请求参数
     */
    @PutMapping("/update/admin")
    @Operation(summary = "用户自己修改个人信息")
    public BaseResponse<Object> updateUserByUser(@RequestBody UpdateUserRequest updateUserRequest) {
        if (ObjectUtil.isEmpty(updateUserRequest)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (updateUserRequest.getId() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "操作失败");
        }
        UserVO userVO = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        if (!userVO.getId().equals(updateUserRequest.getId())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "操作失败");
        }
        userService.updateUserByUser(updateUserRequest);
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
    public BaseResponse<Object> removeUserById(@Parameter(description = "用户id(必须)")Long id, HttpServletRequest request) {
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
    public BaseResponse<ResultPage<UserVO>> getUserList(@ParameterObject UserPageRequest userPageRequest) {
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
    public BaseResponse<Object> addUser(@RequestBody AddUserRequest addUserRequest) {
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

    /**
     * 用户封禁
     */
    @PostMapping("/disable")
    @Operation(summary = "账户封禁接口")
    @SaCheckPermission(UserPermissionConstant.USER_MANAGE)
    public BaseResponse<Object> disabledUser(@RequestBody AccountDisableRequest accountDisableRequest) {
        ThrowUtils.throwIf(accountDisableRequest == null, ErrorCode.NULL_ERROR);
        userService.disabledUser(accountDisableRequest);
        return ResultUtils.success();
    }


    /**
     * 根据id查询用户详情
     * @param id 用户id
     */
    @GetMapping("/detail")
    @Operation(summary = "根据id获取用户详情")
    @SaCheckPermission(UserPermissionConstant.AI_USER)
    public BaseResponse<UserVO> getUserDetailById(@Parameter(description = "用户id(必须)") Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.NULL_ERROR);
        UserVO userVO = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        if (userVO == null)  {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        if (!userVO.getUserRole().equals(UserConstant.ADMIN_USER) && !userVO.getId().equals(id)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        UserVO userDetailById = userService.getUserDetailById(id);
        return ResultUtils.success(userDetailById);
    }
}
