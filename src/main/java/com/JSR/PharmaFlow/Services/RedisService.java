package com.JSR.PharmaFlow.Services;


import com.JSR.PharmaFlow.Cache.RedisConfig;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Utility.RedisKeyCleanup;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import static com.JSR.PharmaFlow.Utility.RedisKeyCleanup.sanitizeKey;

@Service
public class RedisService {


    private final RedisTemplate<String  , Users> usersRedisTemplate;


    public RedisService(RedisTemplate<String, Users> usersRedisTemplate) {
        this.usersRedisTemplate = usersRedisTemplate;
    }

    public void updateUserCache(Users user) {
        String safeEmailKey = RedisKeyCleanup.sanitizeKey(user.getEmail());
        String safeFullNameKey = RedisKeyCleanup.sanitizeKey(user.getFullName());
        String idKey = String.valueOf(user.getId());

        usersRedisTemplate.opsForValue().set("user:" + idKey, user);
        usersRedisTemplate.opsForValue().set("user:" + safeFullNameKey, user);
        usersRedisTemplate.opsForValue().set("user:" + safeEmailKey, user);
    }


}
