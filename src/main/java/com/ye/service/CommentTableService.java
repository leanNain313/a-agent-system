package com.ye.service;

import com.ye.model.entity.CommentTable;
import com.ye.model.dto.comment.*;
import com.ye.model.vo.comment.CommentVO;
import com.ye.common.ResultPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author 惘念
 * @description 针对表【comment_table(评论表)】的数据库操作Service
 * @createDate 2025-01-27
 */
public interface CommentTableService extends IService<CommentTable> {

    /** 发布评论 */
    CommentVO createComment(CommentCreateRequest request, Long userId);

    /** 删除评论（只能删除自己的评论） */
    void deleteMyComment(Long commentId, Long userId);

    /** 获取评论分页 */
    ResultPage<CommentVO> pageComments(CommentPageRequest request);
}