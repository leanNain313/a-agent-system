package com.ai.manager.auth.model;

import lombok.Data;

import java.util.List;

@Data
public class UserAuthConfig {

    /**
     * 权限列表
     */
    private List<UserPermission> permissions;

    /**
     * 角色列表
     */
    private List<UserRole> roles;


}
