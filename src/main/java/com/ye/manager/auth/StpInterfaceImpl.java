package com.ye.manager.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;

import com.ye.Exception.BusinessException;
import com.ye.Exception.ErrorCode;
import com.ye.contant.UserConstant;
import com.ye.model.vo.user.UserVO;
import com.ye.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private UserAuthManager userAuthManager;

    /**
     * 权限校验
     * @param loginId 登录id
     * @param loginType 账户类型, 用于多账户类型的权限校验
     * @return 返回当前用户拥有的权限
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 检查是登录
        UserVO userVO =  (UserVO) StpUtil.getSessionByLoginId(loginId).get(UserConstant.USER_LOGIN_STATUS);
        if (userVO == null || StrUtil.isBlank(userVO.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_LOGIN);
        }
        List<String> permissionsByRole = userAuthManager.getPermissionsByRole(userVO.getUserRole());
        return permissionsByRole;
    }

    /**
     * 权限校验
     * @param loginId 登录id
     * @param loginType 账户类型, 用于多账户类型的权限校验
     * @return 返回当前用户拥有的权限
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.emptyList();
    }
}
