package com.ye.service;

import com.ye.model.entity.PostTable;
import com.ye.model.dto.post.*;
import com.ye.model.vo.post.PostVO;
import com.ye.common.ResultPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author 惘念
 * @description 针对表【post_table(帖子表)】的数据库操作Service
 * @createDate 2025-01-27
 */
public interface PostTableService extends IService<PostTable> {

    /** 发帖 */
    PostVO createPost(PostCreateRequest request, Long userId);

    /** 删除帖子（只能删除自己的帖子） */
    void deleteMyPost(Long postId, Long userId);

    /** 修改帖子（只能修改自己的帖子） */
    void updateMyPost(PostUpdateRequest request, Long userId);

    /** 用户查询帖子（只返回审核通过的帖子） */
    ResultPage<PostVO> queryPosts(PostQueryRequest request);

    /** 管理员查询帖子（返回全部帖子以及审核状态） */
    ResultPage<PostVO> adminQueryPosts(PostAdminQueryRequest request);

    /** 管理员审核帖子 */
    void auditPost(PostAuditRequest request);

    /** 获取帖子详情 */
    PostVO getPostDetail(Long postId);
}