package com.JSR.PharmaFlow.Utility;

import com.JSR.PharmaFlow.Entity.Users;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RedisKeyCleanup {

    private final RedisTemplate<String, Users> usersRedisTemplate;

    @Autowired
    public RedisKeyCleanup(RedisTemplate<String, Users> usersRedisTemplate) {
        this.usersRedisTemplate = usersRedisTemplate;
    }

    public void deleteFromRedis(Users users) {
        String safeEmailKey = sanitizeKey(users.getEmail());
        String safeFullNameKey = sanitizeKey(users.getFullName());
        String idKey = String.valueOf(users.getId());


        log.info("Deleting Redis keys: user:{}, user:{}, user:{}", idKey, safeFullNameKey, safeEmailKey);


        usersRedisTemplate.delete("user:" + idKey);
        usersRedisTemplate.delete("user:" + safeFullNameKey);
        usersRedisTemplate.delete("user:" + safeEmailKey);
    }


    public static String sanitizeKey(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9]", "_");
    }

}
