package com.JSR.PharmaFlow.Services;


import com.JSR.PharmaFlow.Cache.RedisConfig;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Utility.RedisKeyCleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.JSR.PharmaFlow.Utility.RedisKeyCleanup.sanitizeKey;

@Service
@Slf4j
public class RedisService {


    private final RedisTemplate<String  , Users> usersRedisTemplate;


    public RedisService(RedisTemplate<String, Users> usersRedisTemplate) {
        this.usersRedisTemplate = usersRedisTemplate;
    }

    public void updateUserCache(Users user) {
        try {
            String safeEmailKey = RedisKeyCleanup.sanitizeKey(user.getEmail());
            String safeFullNameKey = RedisKeyCleanup.sanitizeKey(user.getFullName());
            String idKey = String.valueOf(user.getId());

            log.debug(" Caching user by ID key: user:{}", idKey);
            usersRedisTemplate.opsForValue().set("user:" + idKey, user);

            log.debug(" Caching user by full name key: user:{}", safeFullNameKey);
            usersRedisTemplate.opsForValue().set("user:" + safeFullNameKey, user);

            log.debug(" Caching user by email key: user:{}", safeEmailKey);
            usersRedisTemplate.opsForValue().set("user:" + safeEmailKey, user);

        } catch (Exception e) {
            log.error(" Error while caching user in Redis: ID={}, Email={}", user.getId(), user.getEmail(), e);
            throw e; // Rethrow if you want the controller to catch it
        }
    }



}
