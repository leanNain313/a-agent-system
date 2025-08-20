package com.ai.model.vo.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "登录用户信息")
public class UserVO {

    /**
     * id
     */
    @Schema(description = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 用户账户
     */
    @Schema(description = "账户")
    private String userAccount;

    /**
     * 用户名称
     */
    @Schema(description = "用户名")
    private String userName;

    /**
     * 用户头像
     */
    @Schema(description = "头像地址")
    private String userAvatar;

    /**
     * 用户简介
     */
    @Schema(description = "个人简介")
    private String userProfile;

    /**
     * 用户角色
     */
    @Schema(description = "用户角色")
    private String userRole;

    /**
     * 编辑时间
     */
    @Schema(description = "编辑时间")
    private Date editTime;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private Date updateTime;

    /**
     * 微信开放平台id
     */
    @Schema(description = "微信开放平台id")
    private String unionid;

    /**
     * 公众号openId
     */
    @Schema(description = "公众号openId")
    private String mpopenid;

    /**
     * 是否被封禁
     */
    @Schema(description = "是否被封禁")
    private Boolean isDisabled;

    /**
     * 此次会话的token
     */
    @Schema(description = "此次会话的token")
    private String token;

}
