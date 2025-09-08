package com.ye.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ye.Exception.BusinessException;
import com.ye.Exception.ErrorCode;
import com.ye.Exception.ThrowUtils;
import com.ye.common.ResultPage;
import com.ye.mapper.CommentTableMapper;
import com.ye.mapper.PostTableMapper;
import com.ye.model.dto.comment.*;
import com.ye.model.entity.CommentTable;
import com.ye.model.entity.PostTable;
import com.ye.model.vo.comment.CommentVO;
import com.ye.model.vo.user.UserVO;
import com.ye.service.CommentTableService;
import com.ye.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 惘念
 * @description 针对表【comment_table(评论表)】的数据库操作Service实现
 * @createDate 2025-01-27
 */
@Service
@Slf4j
public class CommentTableServiceImpl extends ServiceImpl<CommentTableMapper, CommentTable>
        implements CommentTableService {

    @Resource
    private UserService userService;

    @Resource
    private PostTableMapper postTableMapper;

    @Override
    public CommentVO createComment(CommentCreateRequest request, Long userId) {
        if (request == null || userId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (request.getPostId() == null || StrUtil.isBlank(request.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "帖子ID和评论内容不能为空");
        }

        // 验证帖子是否存在
        PostTable post = postTableMapper.selectById(request.getPostId());
        ThrowUtils.throwIf(post == null, ErrorCode.SYSTEM_ERROR, "帖子不存在");

        CommentTable commentTable = new CommentTable();
        commentTable.setContent(request.getContent());
        commentTable.setPostId(request.getPostId());
        commentTable.setUserId(userId);
        commentTable.setCreateTime(new Date());
        commentTable.setUpdateTime(new Date());
        commentTable.setIsDelete(0);

        // 处理父评论逻辑
        if (request.getFatherId() != null) {
            CommentTable fatherComment = this.getById(request.getFatherId());
            ThrowUtils.throwIf(fatherComment == null, ErrorCode.SYSTEM_ERROR, "父评论不存在");
            ThrowUtils.throwIf(!fatherComment.getPostId().equals(request.getPostId()), ErrorCode.PARAMS_ERROR,
                    "父评论与帖子不匹配");

            commentTable.setFatherId(request.getFatherId());
            commentTable.setCommentLevel(fatherComment.getCommentLevel() + 1);
        } else {
            commentTable.setFatherId(0L); // 顶级评论
            commentTable.setCommentLevel(1);
        }

        boolean saved = this.save(commentTable);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "评论发布失败");

        return BeanUtil.copyProperties(commentTable, CommentVO.class);
    }

    @Override
    public void deleteMyComment(Long commentId, Long userId) {
        if (commentId == null || userId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        CommentTable exist = this.getById(commentId);
        ThrowUtils.throwIf(exist == null, ErrorCode.SYSTEM_ERROR, "评论不存在");

        if (!userId.equals(exist.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只能删除自己的评论");
        }

        boolean removed = this.removeById(commentId);
        ThrowUtils.throwIf(!removed, ErrorCode.SYSTEM_ERROR, "删除失败");
    }

    @Override
    public ResultPage<CommentVO> pageComments(CommentPageRequest request) {
        if (request == null || request.getPostId() == null || request.getPageNo() == null
                || request.getPageSize() == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        Integer pageNo = ObjectUtil.defaultIfNull(request.getPageNo(), 1);
        Integer pageSize = ObjectUtil.defaultIfNull(request.getPageSize(), 10);

        QueryWrapper<CommentTable> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", request.getPostId());
        wrapper.eq("is_delete", 0);
        wrapper.orderByAsc("comment_level").orderByAsc("create_time");

        Page<CommentTable> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        List<CommentVO> commentVOS = page.getRecords().stream().map(comment -> {
            CommentVO commentVO = BeanUtil.copyProperties(comment, CommentVO.class);
            // 设置点赞数量
            commentVO.setLikeCount(getLikeCount(comment.getLikeSet()));
            return commentVO;
        }).collect(Collectors.toList());

        // 补充用户信息
        List<CommentVO> result = commentToVO(commentVOS);

        ResultPage<CommentVO> rp = new ResultPage<>();
        rp.setData(result);
        rp.setTotal(page.getTotal());
        return rp;
    }

    /**
     * 获取点赞数量
     */
    private Integer getLikeCount(String likeSet) {
        if (StrUtil.isBlank(likeSet)) {
            return 0;
        }
        try {
            List<Long> likes = JSONUtil.toList(likeSet, Long.class);
            return likes.size();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 补充用户信息
     */
    private List<CommentVO> commentToVO(List<CommentVO> commentList) {
        return commentList.stream().peek(commentVO -> {
            UserVO userVO = userService.getUserDetailById(commentVO.getUserId());
            ThrowUtils.throwIf(userVO == null, ErrorCode.SYSTEM_ERROR, "关联用户为空");
            commentVO.setUserVO(userVO);
        }).collect(Collectors.toList());
    }
}