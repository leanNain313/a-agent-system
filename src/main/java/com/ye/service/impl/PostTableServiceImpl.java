package com.ye.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ye.Exception.BusinessException;
import com.ye.Exception.ErrorCode;
import com.ye.Exception.ThrowUtils;
import com.ye.common.ResultPage;
import com.ye.mapper.PostTableMapper;
import com.ye.mapper.CommentTableMapper;
import com.ye.model.dto.post.*;
import com.ye.model.entity.PostTable;
import com.ye.model.entity.CommentTable;
import com.ye.model.vo.post.PostVO;
import com.ye.model.vo.user.UserVO;
import com.ye.service.CommentTableService;
import com.ye.service.PostTableService;
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
 * @description 针对表【post_table(帖子表)】的数据库操作Service实现
 * @createDate 2025-01-27
 */
@Service
@Slf4j
public class PostTableServiceImpl extends ServiceImpl<PostTableMapper, PostTable>
        implements PostTableService {

    @Resource
    private UserService userService;

    @Resource
    private CommentTableMapper commentTableMapper;

    @Resource
    private CommentTableService commentTableService;

    @Override
    public PostVO createPost(PostCreateRequest request, Long userId) {
        if (request == null || userId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (StrUtil.isBlank(request.getTitle()) || StrUtil.isBlank(request.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题和内容不能为空");
        }

        PostTable postTable = new PostTable();
        postTable.setTitle(request.getTitle());
        postTable.setContent(request.getContent());
        postTable.setUserId(userId);
        postTable.setCreateTime(new Date());
        postTable.setUpdateTime(new Date());
        postTable.setIsDelete(0);
        postTable.setAuditStatus(0); // 默认未审核
        postTable.setPriority(0);

        boolean saved = this.save(postTable);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "发帖失败");

        return BeanUtil.copyProperties(postTable, PostVO.class);
    }

    @Override
    public void deleteMyPost(Long postId, Long userId) {
        if (postId == null || userId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        PostTable exist = this.getById(postId);
        ThrowUtils.throwIf(exist == null, ErrorCode.SYSTEM_ERROR, "帖子不存在");

        if (!userId.equals(exist.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只能删除自己的帖子");
        }

        boolean removed = this.removeById(postId);
        ThrowUtils.throwIf(!removed, ErrorCode.SYSTEM_ERROR, "删除失败");
    }

    @Override
    public void updateMyPost(PostUpdateRequest request, Long userId) {
        if (request == null || request.getId() == null || userId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (StrUtil.isBlank(request.getTitle()) || StrUtil.isBlank(request.getContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题和内容不能为空");
        }

        PostTable exist = this.getById(request.getId());
        ThrowUtils.throwIf(exist == null, ErrorCode.SYSTEM_ERROR, "帖子不存在");

        if (!userId.equals(exist.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH, "只能修改自己的帖子");
        }

        PostTable update = new PostTable();
        update.setId(request.getId());
        update.setTitle(request.getTitle());
        update.setContent(request.getContent());
        update.setEditTime(new Date());
        update.setUpdateTime(new Date());

        boolean updated = this.updateById(update);
        ThrowUtils.throwIf(!updated, ErrorCode.SYSTEM_ERROR, "修改失败");
    }

    @Override
    public ResultPage<PostVO> queryPosts(PostQueryRequest request) {
        if (request == null || request.getPageNo() == null || request.getQueryMode() == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        Integer pageNo = ObjectUtil.defaultIfNull(request.getPageNo(), 1);
        Integer pageSize = ObjectUtil.defaultIfNull(request.getPageSize(), 10);
        if (pageSize > 20) {
            pageSize = 20;
        }

        QueryWrapper<PostTable> wrapper = new QueryWrapper<>();
        // 只返回审核通过的帖子
        wrapper.eq("audit_status", 1);

        if (StrUtil.isNotBlank(request.getSearchText())) {
            wrapper.and(qw -> qw.like("title", request.getSearchText()).or().like("content", request.getSearchText()));
        }

        // 排序逻辑：最热首先根据priority字段进行排序再根据点赞数量进行排序
        if (request.getQueryMode() == 1) {
            // 最热：优先级为主，点赞数量为辅
            wrapper.orderByDesc("priority");
            // 这里暂时按创建时间排序，后续可以根据点赞数量排序
            wrapper.orderByDesc("create_time");
        } else {
            // 最新
            wrapper.orderByDesc("create_time");
        }

        Page<PostTable> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        List<PostVO> postVOS = page.getRecords().stream().map(post -> {
            PostVO postVO = BeanUtil.copyProperties(post, PostVO.class);
            // 设置点赞数量
            postVO.setLikeCount(getLikeCount(post.getLikeSet()));
            // 设置评论数量
            postVO.setCommentCount(getCommentCount(post.getId()));
            return postVO;
        }).collect(Collectors.toList());

        // 补充用户信息
        List<PostVO> result = postToVO(postVOS);

        ResultPage<PostVO> rp = new ResultPage<>();
        rp.setData(result);
        rp.setTotal(page.getTotal());
        return rp;
    }

    @Override
    public ResultPage<PostVO> adminQueryPosts(PostAdminQueryRequest request) {
        if (request == null || request.getPageNo() == null || request.getPageSize() == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        Integer pageNo = ObjectUtil.defaultIfNull(request.getPageNo(), 1);
        Integer pageSize = ObjectUtil.defaultIfNull(request.getPageSize(), 10);

        QueryWrapper<PostTable> wrapper = new QueryWrapper<>();
        wrapper.eq("is_delete", 0);

        if (StrUtil.isNotBlank(request.getSearchText())) {
            wrapper.like("title", request.getSearchText());
        }

        if (request.getUserId() != null) {
            wrapper.eq("user_id", request.getUserId());
        }

        if (request.getAuditStatus() != null) {
            wrapper.eq("audit_status", request.getAuditStatus());
        }

        wrapper.orderByDesc("create_time");

        Page<PostTable> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        List<PostVO> postVOS = page.getRecords().stream().map(post -> {
            PostVO postVO = BeanUtil.copyProperties(post, PostVO.class);
            // 设置点赞数量
            postVO.setLikeCount(getLikeCount(post.getLikeSet()));
            // 设置评论数量
            postVO.setCommentCount(getCommentCount(post.getId()));
            return postVO;
        }).collect(Collectors.toList());

        // 补充用户信息
        List<PostVO> result = postToVO(postVOS);

        ResultPage<PostVO> rp = new ResultPage<>();
        rp.setData(result);
        rp.setTotal(page.getTotal());
        return rp;
    }

    @Override
    public void auditPost(PostAuditRequest request) {
        if (request == null || request.getId() == null || request.getAuditStatus() == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        PostTable exist = this.getById(request.getId());
        ThrowUtils.throwIf(exist == null, ErrorCode.SYSTEM_ERROR, "帖子不存在");

        PostTable update = new PostTable();
        update.setId(request.getId());
        update.setAuditStatus(request.getAuditStatus());
        update.setExplain(request.getAuditReason());
        update.setUpdateTime(new Date());

        boolean updated = this.updateById(update);
        ThrowUtils.throwIf(!updated, ErrorCode.SYSTEM_ERROR, "审核失败");
    }

    @Override
    public PostVO getPostDetail(Long postId) {
        if (postId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        PostTable post = this.getById(postId);
        ThrowUtils.throwIf(post == null, ErrorCode.SYSTEM_ERROR, "帖子不存在");

        PostVO postVO = BeanUtil.copyProperties(post, PostVO.class);
        // 设置点赞数量
        postVO.setLikeCount(getLikeCount(post.getLikeSet()));
        // 设置评论数量
        postVO.setCommentCount(getCommentCount(post.getId()));

        // 补充用户信息
        UserVO userVO = userService.getUserDetailById(post.getUserId());
        postVO.setUserVO(userVO);

        return postVO;
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
     * 获取评论数量
     */
    private Integer getCommentCount(Long postId) {
        QueryWrapper<CommentTable> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", postId);
        wrapper.eq("is_delete", 0);
        return Math.toIntExact(commentTableMapper.selectCount(wrapper));
    }

    /**
     * 补充用户信息
     */
    private List<PostVO> postToVO(List<PostVO> postList) {
        return postList.stream().peek(postVO -> {
            UserVO userVO = userService.getUserDetailById(postVO.getUserId());
            ThrowUtils.throwIf(userVO == null, ErrorCode.SYSTEM_ERROR, "关联用户为空");
            postVO.setUserVO(userVO);
        }).collect(Collectors.toList());
    }
}