package com.ye.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

/**
 * 评论表
 * 
 * @TableName comment_table
 */
@TableName(value = "comment_table")
public class CommentTable {
    /**
     * 评论ID
     */
    @TableId
    private Long id;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 点赞用户集合(JSON存user_id数组)
     */
    private String likeSet;

    /**
     * 父评论ID(为空表示顶级评论)
     */
    private Long fatherId;

    /**
     * 评论用户ID
     */
    private Long userId;

    /**
     * 评论层级(1=一级评论,2=二级...)
     */
    private Integer commentLevel;

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
     * 帖子id
     */
    private Long postId;

    /**
     * 评论ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 评论ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 评论内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 评论内容
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
     * 父评论ID(为空表示顶级评论)
     */
    public Long getFatherId() {
        return fatherId;
    }

    /**
     * 父评论ID(为空表示顶级评论)
     */
    public void setFatherId(Long fatherId) {
        this.fatherId = fatherId;
    }

    /**
     * 评论用户ID
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 评论用户ID
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 评论层级(1=一级评论,2=二级...)
     */
    public Integer getCommentLevel() {
        return commentLevel;
    }

    /**
     * 评论层级(1=一级评论,2=二级...)
     */
    public void setCommentLevel(Integer commentLevel) {
        this.commentLevel = commentLevel;
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
     * 帖子id
     */
    public Long getPostId() {
        return postId;
    }

    /**
     * 帖子id
     */
    public void setPostId(Long postId) {
        this.postId = postId;
    }
}