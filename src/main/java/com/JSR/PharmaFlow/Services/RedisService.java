package com.JSR.PharmaFlow.Services;


import com.JSR.PharmaFlow.Cache.RedisConfig;
import com.JSR.PharmaFlow.Entity.Users;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {


    private final RedisTemplate<String  , Users> redisTemplate;


    public RedisService(RedisTemplate<String, Users> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public void updateUserCache(Users user) {
        String redisKey = "users:" + user.getEmail();
        redisTemplate.opsForValue().set(redisKey, user);
    }
}
