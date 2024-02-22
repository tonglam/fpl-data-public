package com.tong.fpl.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Create by tong on 2021/8/30
 */
@Component
public class RedisUtils {

    private static RedisTemplate<String, Object> redisTemplate;

    public static void pipelineHashCache(Map<String, Map<String, Object>> cacheMap, long expire, TimeUnit timeUnit) {
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                cacheMap.forEach((k, v) -> {
                    redisTemplate.opsForHash().putAll(k, v);
                    if (expire != -1) {
                        redisTemplate.expire(k, expire, timeUnit);
                    }
                });
                connection.close();
                return null;
            }
        });
    }

    public static void pipelineSetCache(Map<String, Set<Object>> cacheMap, long expire, TimeUnit timeUnit) {
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.openPipeline();
                cacheMap.forEach((k, v) ->
                        v.forEach(value -> {
                            redisTemplate.opsForSet().add(k, value);
                            if (expire != -1) {
                                redisTemplate.expire(k, expire, timeUnit);
                            }
                        }));
                connection.close();
                return null;
            }
        });
    }

    public static void setHashValue(String key, Object hashKey, Object value) {
        if (!(hashKey instanceof String)) {
            hashKey = String.valueOf(hashKey);
        }
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T readHashValue(String key, Object hashKey) {
        if (!(hashKey instanceof String)) {
            hashKey = String.valueOf(hashKey);
        }
        return (T) redisTemplate.opsForHash().get(key, hashKey);
    }

    public static Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public static void removeCacheByKey(String key) {
        Set<String> keys = redisTemplate.keys(key + "*");
        if (CollectionUtils.isEmpty(keys)) {
            return;
        }
        redisTemplate.delete(keys);
    }

    @Autowired
    private void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisUtils.redisTemplate = redisTemplate;
    }

}
