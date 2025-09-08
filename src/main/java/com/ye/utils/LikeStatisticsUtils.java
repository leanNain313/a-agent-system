package com.ye.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LikeStatisticsUtils {

    @Resource
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "post:like:";

    // 点赞
    public void likePost(Long postId, Long userId) {
        String key = KEY_PREFIX + postId;
        redisTemplate.opsForSet().add(key, userId.toString());
    }

    // 取消点赞
    public void unlikePost(Long postId, Long userId) {
        String key = KEY_PREFIX + postId;
        redisTemplate.opsForSet().remove(key, userId.toString());
    }

    // 判断是否点赞
    public boolean hasLiked(Long postId, Long userId) {
        String key = KEY_PREFIX + postId;
        return redisTemplate.opsForSet().isMember(key, userId.toString());
    }

    // 获取点赞数量
    public long getLikeCount(Long postId) {
        String key = KEY_PREFIX + postId;
        return redisTemplate.opsForSet().size(key);
    }
}
