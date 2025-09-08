package com.ye.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.ye.Exception.BusinessException;
import com.ye.Exception.ErrorCode;
import com.ye.Exception.ThrowUtils;
import com.ye.common.ResultPage;
import com.ye.contant.UserConstant;
import com.ye.manager.cos.CosManager;
import com.ye.mapper.UserMapper;
import com.ye.model.dto.user.*;
import com.ye.model.entity.User;
import com.ye.model.enums.AuthCodeType;
import com.ye.model.enums.DeviceTypeEnum;
import com.ye.model.enums.DisabledTypeEnum;
import com.ye.model.vo.user.AccountFunctionStateVO;
import com.ye.model.vo.user.UserVO;
import com.ye.service.EmailService;
import com.ye.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Resource
    private CosManager cosManager;

    @Resource
    private EmailService emailService;


    /**
     * 注册
     */
    @Override
    @Scheduled
    public void register(RegisterRequest registerRequest) {
        // 不得为空
        if (StrUtil.isBlank(registerRequest.getUserAccount()) ||
                StrUtil.isBlank(registerRequest.getUserPassword()) ||
                StrUtil.isBlank(registerRequest.getCheckPassword()) ||
                StrUtil.isBlank(registerRequest.getCode())
        ) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
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
        // 校验验证码的有效性
        String code = emailService.getEmailCode(registerRequest.getUserAccount(), AuthCodeType.REGISTER_CODE.getValue());
        if (StrUtil.isEmpty(code)) {
            throw new BusinessException(ErrorCode.CODE_OVERDUE_ERROR);
        }
        // 校验验证码是否正确
        if (!code.equals(registerRequest.getCode())) {
            throw new BusinessException(ErrorCode.CODE_ERROR);
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
        log.info("注册用户的邮箱名为: " + registerRequest.getUserAccount());
    }

    @Override
    public UserVO doLogin(LoginRequest loginRequest) {
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
            long disableTime = StpUtil.getDisableTime(user.getId());
            this.disabledHandle(disableTime);
        }
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        // 登录并记录登录状态生成sa-token 注意这个过去时间和 SpringSession 一致的
        ThrowUtils.throwIf(StrUtil.isBlank(loginRequest.getDeviceType()), ErrorCode.NULL_ERROR);
        StpUtil.login(user.getId());
        StpUtil.getSession().set(UserConstant.USER_LOGIN_STATUS, userVO);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        log.info("登录用户id：{}， 登录设备类型：{}, token:{}", user.getId(), loginRequest.getDeviceType(), tokenInfo.getTokenValue());
        userVO.setToken(tokenInfo.getTokenValue());
        return userVO;
    }

    @Override
    public void resetPassword(RegisterRequest registerRequest) {
        // 不得为空
        if (StrUtil.isBlank(registerRequest.getUserAccount()) ||
                StrUtil.isBlank(registerRequest.getUserPassword()) ||
                StrUtil.isBlank(registerRequest.getCheckPassword()) ||
                StrUtil.isBlank(registerRequest.getCode())
        ) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 密码不得少于8位
        if (registerRequest.getUserPassword().length() < 8) {
            throw new BusinessException(ErrorCode.DATA_RULE_ERROR);
        }
        // 两次密码输入相同
        if (!registerRequest.getUserPassword().equals(registerRequest.getCheckPassword())) {
            throw new BusinessException(ErrorCode.DATA_RULE_ERROR);
        }
        // 校验验证码的有效性
        String code = emailService.getEmailCode(registerRequest.getUserAccount(), AuthCodeType.RESET_CODE.getValue());
        if (StrUtil.isEmpty(code)) {
            throw new BusinessException(ErrorCode.CODE_OVERDUE_ERROR);
        }
        // 校验验证码是否正确
        if (!code.equals(registerRequest.getCode())) {
            throw new BusinessException(ErrorCode.CODE_ERROR);
        }
        User exists = this.lambdaQuery()
                .eq(User::getUserAccount, registerRequest.getUserAccount())
                .one();
        ThrowUtils.throwIf(exists == null, ErrorCode.SYSTEM_ERROR, "账户不存在");
        String handledPassword = this.handlePassword(registerRequest.getUserPassword());
        UpdateWrapper<User> queryWrapper = new UpdateWrapper<>();
        queryWrapper.eq("user_account", registerRequest.getUserAccount());
        queryWrapper.set("user_password", handledPassword);
        User user = BeanUtil.copyProperties(registerRequest, User.class);
        user.setUserPassword(handledPassword);
        boolean update = this.update(user, queryWrapper);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "重置失败");
        // 注销原有的登录状态
        StpUtil.logout(exists.getId());
    }

    /**
     * 登出
     */
    @Override
    public void loginOut() {
        // 去除token
        StpUtil.logout();
    }

    /**
     * 管理员修改用户信息
     * @param updateUserRequest 修改用户信息
     */
    @Override
    public void updateUserByAdmin(UpdateUserRequest updateUserRequest) {
        // 对密码进行加密储存
        if (!StrUtil.isBlank(updateUserRequest.getUserPassword())) {
            String handledPassword = this.handlePassword(updateUserRequest.getUserPassword());
            updateUserRequest.setUserPassword(handledPassword);
        }
        User byId = this.getById(updateUserRequest.getId());
        ThrowUtils.throwIf(byId == null, ErrorCode.SYSTEM_ERROR, "该用户不存在");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        UserVO userVO = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        queryWrapper.eq("id", userVO.getId());
        User user = BeanUtil.copyProperties(updateUserRequest, User.class);
        if (!StrUtil.isBlank(user.getUserAvatar()) && !StrUtil.isBlank(user.getUserAvatar())) {
            cosManager.deleteFile(byId.getUserAvatar());
        }
        boolean update = this.update(user, queryWrapper);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "修改失败");
        // 密码修改移除登录状态
        if (!StrUtil.isEmpty(updateUserRequest.getUserPassword())) {
            StpUtil.logout(updateUserRequest.getId());
        }
    }

    /**
     * 修改用户信息
     * @param updateUserRequest 参数
     */
    @Override
    public void updateUserByUser(UpdateUserRequest updateUserRequest) {
        // 对密码进行加密储存
        if (!StrUtil.isBlank(updateUserRequest.getUserPassword())) {
            String handledPassword = this.handlePassword(updateUserRequest.getUserPassword());
            updateUserRequest.setUserPassword(handledPassword);
        }
        User byId = this.getById(updateUserRequest.getId());
        ThrowUtils.throwIf(byId == null, ErrorCode.SYSTEM_ERROR, "该用户不存在");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", updateUserRequest.getId());
        User user = BeanUtil.copyProperties(updateUserRequest, User.class);
        user.setUserRole("user");
        if (!StrUtil.isBlank(user.getUserAvatar()) && !StrUtil.isBlank(user.getUserAvatar())) {
            cosManager.deleteFile(byId.getUserAvatar());
        }
        boolean update = this.update(user, queryWrapper);
        ThrowUtils.throwIf(!update, ErrorCode.SYSTEM_ERROR, "修改失败");
        // 密码修改移除登录状态
        if (!StrUtil.isEmpty(updateUserRequest.getUserPassword())) {
            StpUtil.logout(updateUserRequest.getId());
        }
    }

    /**
     * 删除失败
     */
    @Override
    public void removeUserById(Long id) {
        // 校验二级认证
        ThrowUtils.throwIf(!StpUtil.isSafe(), ErrorCode.AUTH_ERROR);
        boolean remove = this.removeById(id);
        ThrowUtils.throwIf(!remove, ErrorCode.SYSTEM_ERROR, "删除失败");
        // 移除登录状态
        StpUtil.logout(id);
    }

    /**
     * 获取用户列表
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
        List<UserVO> collect = userVOList.stream().peek(userVO -> userVO.setAccountFunctionStateVO(paramBuild(userVO.getId())))
                .collect(Collectors.toList());
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
     * 账户部分功能封禁处理
     * @param disableTime 封禁剩余时间
     */
    @Override
    public void disabledHandle(long disableTime) {
        ThrowUtils.throwIf(disableTime == -1, ErrorCode.FUNCTION_DISABLE_ERROR, "该功能被永久封禁，如有疑问请联系管理员");
        LocalDateTime now = LocalDateTime.now();
        // 加上封禁时间
        LocalDateTime localDateTime = now.plusSeconds(disableTime);
        // 定义格式化器
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = localDateTime.format(dateTimeFormatter);
        String message = String.format("该功能已被封禁， 解除时间为:%s, 如有疑问请联系管理员", time);
        throw new BusinessException(ErrorCode.FUNCTION_DISABLE_ERROR, message);
    }

    /**
     * 二级校验
     * @param code 验证码
     */
    @Override
    public void AuthLevelToTwo(String code) {
        ThrowUtils.throwIf(StrUtil.isBlank(code), ErrorCode.NULL_ERROR);
        UserVO userVO = (UserVO) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATUS);
        ThrowUtils.throwIf(userVO == null, ErrorCode.NO_LOGIN);
        String emailCode = emailService.getEmailCode(userVO.getUserAccount(),AuthCodeType.TWO_AUTH_CODE.getValue());
        ThrowUtils.throwIf(StrUtil.isEmpty(emailCode), ErrorCode.CODE_OVERDUE_ERROR);
        ThrowUtils.throwIf(!emailCode.equals(code), ErrorCode.CODE_ERROR);
        StpUtil.openSafe(300); // 默认校验时长300秒
    }

    /**
     * 根据id获取用户详情
     * @param id 用户id
     */
    @Override
    public UserVO getUserDetailById(Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = this.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.SYSTEM_ERROR, "不存在该用户");
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
        userVO.setAccountFunctionStateVO(paramBuild(userVO.getId()));
        return userVO;
    }

    /**
     * 参数构建
     * @param id 用户id
     * @return 返回实体类
     */
    private AccountFunctionStateVO paramBuild(Long id) {
        return AccountFunctionStateVO.builder()
                .allFunction(StpUtil.isDisable(id)) // 所有功能
                .aiFunction(StpUtil.isDisable(id, DisabledTypeEnum.SEED_MESSAGE_TYPE.getValue())) // ai功能
                .deployFunction(StpUtil.isDisable(id, DisabledTypeEnum.DEPLOY_TYPE.getValue())) // 部署功能
                .fileUpLoadFunction(StpUtil.isDisable(id, DisabledTypeEnum.FILE_UPLOAD_TYPE.getValue())) // 文件上传
                .allFunctionTime(StpUtil.getDisableTime(id))
                .aiFunctionTime(StpUtil.getDisableTime(id, DisabledTypeEnum.SEED_MESSAGE_TYPE.getValue()))
                .deployFunctionTime(StpUtil.getDisableTime(id, DisabledTypeEnum.DEPLOY_TYPE.getValue()))
                .fileUpLoadFunctionTime(StpUtil.getDisableTime(id, DisabledTypeEnum.FILE_UPLOAD_TYPE.getValue()))
                .build();
    }

    /**
     * 密码加密
     */
    private String handlePassword(String password) {
        return DigestUtils.md5DigestAsHex((UserConstant.USER_PASSWORD_SALT +password)
                .getBytes(StandardCharsets.UTF_8));
    }

}
