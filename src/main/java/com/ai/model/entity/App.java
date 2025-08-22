package com.ai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * 应用
 * @TableName app
 */
@TableName(value ="app")
public class App {
    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     */
    private String codeType;

    /**
     * 部署标识
     */
    private String deployKey;

    /**
     * 部署时间
     */
    private Date deployedTime;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * id
     */
    public Long getId() {
        return id;
    }

    /**
     * id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 应用名称
     */
    public String getAppName() {
        return appName;
    }

    /**
     * 应用名称
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * 应用封面
     */
    public String getCover() {
        return cover;
    }

    /**
     * 应用封面
     */
    public void setCover(String cover) {
        this.cover = cover;
    }

    /**
     * 应用初始化的 prompt
     */
    public String getInitPrompt() {
        return initPrompt;
    }

    /**
     * 应用初始化的 prompt
     */
    public void setInitPrompt(String initPrompt) {
        this.initPrompt = initPrompt;
    }

    /**
     * 代码生成类型（枚举）
     */
    public String getCodeType() {
        return codeType;
    }

    /**
     * 代码生成类型（枚举）
     */
    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    /**
     * 部署标识
     */
    public String getDeployKey() {
        return deployKey;
    }

    /**
     * 部署标识
     */
    public void setDeployKey(String deployKey) {
        this.deployKey = deployKey;
    }

    /**
     * 部署时间
     */
    public Date getDeployedTime() {
        return deployedTime;
    }

    /**
     * 部署时间
     */
    public void setDeployedTime(Date deployedTime) {
        this.deployedTime = deployedTime;
    }

    /**
     * 优先级
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * 优先级
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * 创建用户id
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 创建用户id
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 编辑时间
     */
    public Date getEditTime() {
        return editTime;
    }

    /**
     * 编辑时间
     */
    public void setEditTime(Date editTime) {
        this.editTime = editTime;
    }

    /**
     * 创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * 创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 更新时间
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * 更新时间
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 是否删除
     */
    public Integer getIsDelete() {
        return isDelete;
    }

    /**
     * 是否删除
     */
    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        App other = (App) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getAppName() == null ? other.getAppName() == null : this.getAppName().equals(other.getAppName()))
                && (this.getCover() == null ? other.getCover() == null : this.getCover().equals(other.getCover()))
                && (this.getInitPrompt() == null ? other.getInitPrompt() == null : this.getInitPrompt().equals(other.getInitPrompt()))
                && (this.getCodeType() == null ? other.getCodeType() == null : this.getCodeType().equals(other.getCodeType()))
                && (this.getDeployKey() == null ? other.getDeployKey() == null : this.getDeployKey().equals(other.getDeployKey()))
                && (this.getDeployedTime() == null ? other.getDeployedTime() == null : this.getDeployedTime().equals(other.getDeployedTime()))
                && (this.getPriority() == null ? other.getPriority() == null : this.getPriority().equals(other.getPriority()))
                && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
                && (this.getEditTime() == null ? other.getEditTime() == null : this.getEditTime().equals(other.getEditTime()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
                && (this.getIsDelete() == null ? other.getIsDelete() == null : this.getIsDelete().equals(other.getIsDelete()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getAppName() == null) ? 0 : getAppName().hashCode());
        result = prime * result + ((getCover() == null) ? 0 : getCover().hashCode());
        result = prime * result + ((getInitPrompt() == null) ? 0 : getInitPrompt().hashCode());
        result = prime * result + ((getCodeType() == null) ? 0 : getCodeType().hashCode());
        result = prime * result + ((getDeployKey() == null) ? 0 : getDeployKey().hashCode());
        result = prime * result + ((getDeployedTime() == null) ? 0 : getDeployedTime().hashCode());
        result = prime * result + ((getPriority() == null) ? 0 : getPriority().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getEditTime() == null) ? 0 : getEditTime().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        result = prime * result + ((getIsDelete() == null) ? 0 : getIsDelete().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", appName=").append(appName);
        sb.append(", cover=").append(cover);
        sb.append(", initPrompt=").append(initPrompt);
        sb.append(", codeType=").append(codeType);
        sb.append(", deployKey=").append(deployKey);
        sb.append(", deployedTime=").append(deployedTime);
        sb.append(", priority=").append(priority);
        sb.append(", userId=").append(userId);
        sb.append(", editTime=").append(editTime);
        sb.append(", createTime=").append(createTime);
        sb.append(", updateTime=").append(updateTime);
        sb.append(", isDelete=").append(isDelete);
        sb.append("]");
        return sb.toString();
    }
}