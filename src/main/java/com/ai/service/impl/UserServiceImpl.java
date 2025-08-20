package com.ai.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.Exception.BusinessException;
import com.ai.Exception.ErrorCode;
import com.ai.Exception.ThrowUtils;
import com.ai.common.ResultPage;
import com.ai.contant.UserConstant;
import com.ai.mapper.UserMapper;
import com.ai.model.dto.user.*;
import com.ai.model.entity.User;
import com.ai.model.enums.DeviceTypeEnum;
import com.ai.model.enums.DisabledTypeEnum;
import com.ai.model.vo.user.UserVO;
import com.ai.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
* @author 惘念
* @description 针对表【user】的数据库操作Service实现
* @createDate 2025-07-16 20:36:48
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {


    /**
     * 注册
     */
    @Override
    @Scheduled
    public void register(RegisterRequest registerRequest) {
        // 不得为空
        if (StrUtil.isBlank(registerRequest.getUserAccount()) ||
                StrUtil.isBlank(registerRequest.getUserPassword()) ||
                StrUtil.isBlank(registerRequest.getCheckPassword())
        ) {
            throw new BusinessException(ErrorCode.DATA_RULE_ERROR);
        }
        // 账户不得少于4位
        if (registerRequest.getUserAccount().length() < 4) {
            throw new BusinessException(ErrorCode.DATA_RULE_ERROR);
        }
        // 密码不得少于8位
        if (registerRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.DATA_RULE_ERROR);
        }
        // 过滤非法字符
        String regEx = "[^\\\\w\\\\s]";
        Matcher matcher = Pattern.compile(regEx).matcher(registerRequest.getUserAccount());
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.DATA_RULE_ERROR);
        }
        // 两次密码输入相同
        if (!registerRequest.getUserPassword().equals(registerRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.DATA_RULE_ERROR);
        }
        // 判断账户是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", registerRequest.getUserAccount());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.ACCOUNT_ERROR);
        }
        // 对密码进行加密储存
        String handledPassword = this.handlePassword(registerRequest.getUserPassword());
        User user = new User();
        user.setUserAccount(registerRequest.getUserAccount());
        user.setUserPassword(handledPassword);
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        log.info("注册用户的名为: " + registerRequest.getUserAccount());
    }

    @Override
    public UserVO doLogin(LoginRequest loginRequest , HttpServletRequest request) {
        String userAccount = loginRequest.getUserAccount();
        String userPassword = loginRequest.getUserPassword();
        // 不得为空
        if (StrUtil.isBlank(userAccount) || StrUtil.isBlank(userPassword)) {
            throw new BusinessException(ErrorCode.DATA_RULE_ERROR);
        }
        // 账户不得少于4位, 密码不得少于8位
        if (userAccount.length() < 4 || userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.DATA_RULE_ERROR);
        }
        // 判断是否含有非法字符
        String regEx = "[^\\\\w\\\\s]";
        Matcher matcher = Pattern.compile(regEx).matcher(userAccount);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.DATA_RULE_ERROR);
        }
        // 校验设备类型
        if (DeviceTypeEnum.getEnumByValue(loginRequest.getDeviceType()) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 加盐判断
        String handledPassword = this.handlePassword(loginRequest.getUserPassword());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        queryWrapper.eq("user_password", handledPassword);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        // 检查账户是否被封禁
        if (StpUtil.isDisable(user.getId())) {
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLE_ERROR);
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        // 记录用户登录状态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS, userVO);
        // 登录并记录登录状态生成sa-token 注意这个过去时间和 SpringSession 一致的
        ThrowUtils.throwIf(StrUtil.isBlank(loginRequest.getDeviceType()), ErrorCode.NULL_ERROR);
        StpUtil.login(user.getId());
        StpUtil.getSession().set(UserConstant.USER_LOGIN_STATUS, userVO);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        log.info("登录用户id：{}， 登录设备类型：{}, token:{}", user.getId(), loginRequest.getDeviceType(), tokenInfo.getTokenValue());
        userVO.setToken(tokenInfo.getTokenValue());
        return userVO;
    }

    /**
     * 登出
     */
    @Override
    public void loginOut(HttpServletRequest request) {
        UserVO userVO = (UserVO) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(userVO == null, ErrorCode.NO_LOGIN);
        // 去除token
//        StpUtil.kickout(userVO.getId());
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS);
    }

    /**
     * 获取当前登录用户信息
     */
    @Override
    public UserVO getCurrentUser(HttpServletRequest request) {
        return (UserVO) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
    }

    /**
     * 修改用户信息
     * @param updateUserRequest 修改用户信息
     */
    @Override
    public void updateUserByAdmin(UpdateUserRequest updateUserRequest, HttpServletRequest request) {
        // 对密码进行加密储存
        if (!StrUtil.isBlank(updateUserRequest.getUserPassword())) {
            String handledPassword = this.handlePassword(updateUserRequest.getUserPassword());
            updateUserRequest.setUserPassword(handledPassword);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        UserVO currentUser = this.getCurrentUser(request);
        queryWrapper.eq("id", currentUser.getId());
        User user = BeanUtil.copyProperties(updateUserRequest, User.class);
        boolean update = this.update(user, queryWrapper);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "修改失败");
    }

    /**
     * 管理员修改用户信息
     * @param updateUserRequest 参数
     */
    @Override
    public void updateUserByUser(UpdateUserRequest updateUserRequest, HttpServletRequest request) {
        // 对密码进行加密储存
        String handledPassword = this.handlePassword(updateUserRequest.getUserPassword());
        updateUserRequest.setUserPassword(handledPassword);
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", updateUserRequest.getId());
        User user = BeanUtil.copyProperties(updateUserRequest, User.class);
        user.setUserRole("user");
        boolean update = this.update(user, queryWrapper);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "修改失败");
    }

    /**
     * 删除失败
     */
    @Override
    public void removeUserById(Long id, HttpServletRequest request) {
        this.loginOut(request);
        boolean remove = this.removeById(id);
        ThrowUtils.throwIf(!remove, ErrorCode.SYSTEM_ERROR, "删除失败");
    }

    /**
     * 获去用户列表
     * @param userPageRequest 请求参数
     * @return 返回分页列表
     */
    @Override
    public ResultPage<UserVO> getUserList(UserPageRequest userPageRequest) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (!StrUtil.isBlank(userPageRequest.getUserName())) {
            queryWrapper.like("user_name", userPageRequest.getUserName());
        }
        if (!StrUtil.isBlank(userPageRequest.getUserRole())) {
            queryWrapper.eq("user_role", userPageRequest.getUserRole());
        }
        Page<User> page = this.page(new Page<>(userPageRequest.getPageNo(), userPageRequest.getPageSize()), queryWrapper);
        List<User> records = page.getRecords();
        List<UserVO> userVOList = BeanUtil.copyToList(records, UserVO.class);
        // 查询账户封禁状态
        List<UserVO> collect = userVOList.stream().peek(userVO -> userVO.setIsDisabled(StpUtil.isDisable(userVO.getId()))).collect(Collectors.toList());
        ResultPage<UserVO> resultPage = new ResultPage<>();
        resultPage.setData(collect);
        resultPage.setTotal(page.getTotal());
        return resultPage;
    }

    /**
     * 添加账户
     * @param addUserRequest 请求参数
     */
    @Override
    public void addUser(AddUserRequest addUserRequest) {
        if (!addUserRequest.getUserPassword().equals(addUserRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_REPEAT_ERROR);
        }
        User user = BeanUtil.copyProperties(addUserRequest, User.class);
        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "添加失败");
    }


    /**
     * 账户的封禁与解除
     */
    @Override
    public void disabledUser(AccountDisableRequest accountDisableRequest) {
        // 完全封禁
        if (accountDisableRequest.getDisableType() == 0) {
            // 先踢下线， 再进行永久封禁
            StpUtil.kickout(accountDisableRequest.getUserId());
            StpUtil.disable(accountDisableRequest.getUserId(), accountDisableRequest.getDisableTime());
        }
        // 封禁部分功能
        if (accountDisableRequest.getDisableType() == 1) {
            // 校验封禁类型
            ThrowUtils.throwIf(StrUtil.isBlank(accountDisableRequest.getFunctionType()), ErrorCode.NULL_ERROR);
            ThrowUtils.throwIf(DisabledTypeEnum.getEnumByValue(accountDisableRequest.getFunctionType()) == null, ErrorCode.PARAMS_ERROR);
            StpUtil.disable(accountDisableRequest.getUserId(), accountDisableRequest.getFunctionType(), accountDisableRequest.getDisableTime());
        }
        // 解除封禁
        if (accountDisableRequest.getDisableType() == 2) {
            // 完全解除封禁
            if (StrUtil.isBlank(accountDisableRequest.getFunctionType())) {
                StpUtil.untieDisable(accountDisableRequest.getUserId());
            } else {
                // 解除部分封禁
                ThrowUtils.throwIf(DisabledTypeEnum.getEnumByValue(accountDisableRequest.getFunctionType()) == null, ErrorCode.PARAMS_ERROR, "封禁类型不存在");
                StpUtil.untieDisable(accountDisableRequest.getUserId(), accountDisableRequest.getFunctionType());
            }
        }
    }

    /**
     * 密码加密
     */
    private String handlePassword(String password) {
        return DigestUtils.md5DigestAsHex((UserConstant.USER_PASSWORD_SALT +password)
                .getBytes(StandardCharsets.UTF_8));
    }
}
