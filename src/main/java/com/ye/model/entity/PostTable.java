package com.ye.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * 帖子表
 * 
 * @TableName post_table
 */
@TableName(value = "post_table")
public class PostTable {
    /**
     * 帖子ID
     */
    @TableId
    private Long id;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 点赞用户集合(JSON存user_id数组)
     */
    private String likeSet;

    /**
     * 发帖用户ID
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0=正常,1=删除)
     */
    private Integer isDelete;

    /**
     * 审核状态(0=未通过,1=通过)
     */
    private Integer auditStatus;

    /**
     * 审核原因
     */
    private String explain;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 帖子ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 帖子ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 帖子标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 帖子标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 帖子内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 帖子内容
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 点赞用户集合(JSON存user_id数组)
     */
    public String getLikeSet() {
        return likeSet;
    }

    /**
     * 点赞用户集合(JSON存user_id数组)
     */
    public void setLikeSet(String likeSet) {
        this.likeSet = likeSet;
    }

    /**
     * 发帖用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 发帖用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
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
     * 是否删除(0=正常,1=删除)
     */
    public Integer getIsDelete() {
        return isDelete;
    }

    /**
     * 是否删除(0=正常,1=删除)
     */
    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    /**
     * 审核状态(0=未通过,1=通过)
     */
    public Integer getAuditStatus() {
        return auditStatus;
    }

    /**
     * 审核状态(0=未通过,1=通过)
     */
    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }

    /**
     * 审核原因
     */
    public String getExplain() {
        return explain;
    }

    /**
     * 审核原因
     */
    public void setExplain(String explain) {
        this.explain = explain;
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
}