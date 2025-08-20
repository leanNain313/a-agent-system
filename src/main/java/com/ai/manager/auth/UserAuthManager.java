package com.ai.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ai.manager.auth.model.UserAuthConfig;
import com.ai.manager.auth.model.UserRole;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class UserAuthManager {

    public static UserAuthConfig USER_AUTH_CONFIG;

    static {
        String json = ResourceUtil.readUtf8Str("UserAuthConfig.json");
        USER_AUTH_CONFIG = JSONUtil.toBean(json, UserAuthConfig.class);
    }

    /**
     * 根据角色获取权限列表
     */
    public List<String> getPermissionsByRole(String UserRole) {
        if (StrUtil.isBlank(UserRole)) {
            return new ArrayList<>();
        }
        // 寻找角色， 没有返回空
        UserRole role = USER_AUTH_CONFIG
                .getRoles()
                .stream()
                .filter(r -> UserRole.equals(r.getKey())) // true保留， false舍弃
                .findFirst()
                .orElse(null);
        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }



}
