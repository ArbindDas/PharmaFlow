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

//    public void deleteFromRedis(Users users) {
//        String safeEmailKey = sanitizeKey(users.getEmail());
//        String safeFullNameKey = sanitizeKey(users.getFullName());
//        String idKey = String.valueOf(users.getId());
//
//
//        log.info("Deleting Redis keys: user:{}, user:{}, user:{}", idKey, safeFullNameKey, safeEmailKey);
//
//
//        usersRedisTemplate.delete("user:" + idKey);
//        usersRedisTemplate.delete("user:" + safeFullNameKey);
//        usersRedisTemplate.delete("user:" + safeEmailKey);
//    }

    public void deleteFromRedis(Users user) {
        // Delete individual user keys
        String safeEmailKey = sanitizeKey(user.getEmail());
        String safeFullNameKey = sanitizeKey(user.getFullName());
        String idKey = String.valueOf(user.getId());

        log.info("Deleting Redis keys: user:{}, user:{}, user:{}", idKey, safeFullNameKey, safeEmailKey);

        usersRedisTemplate.delete("user:" + idKey);
        usersRedisTemplate.delete("user:" + safeFullNameKey);
        usersRedisTemplate.delete("user:" + safeEmailKey);

        // Update the 'all_users' cache (if it exists) by removing the deleted user
        updateAllUsersCacheAfterDeletion(user.getId());
    }

    public static String sanitizeKey(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9]", "_");
    }

    private void updateAllUsersCacheAfterDeletion(Long deletedUserId) {
        String redisKey = "all_users";

        // Check if the 'all_users' cache exists
        Object cached = usersRedisTemplate.opsForValue().get(redisKey);
        if (cached instanceof List <?>) {
            try {
                // Convert to a modifiable list and filter out the deleted user
                List<Users> cachedUsers = (List<Users>) cached;
                List<Users> updatedUsers = cachedUsers.stream()
                        .filter(u -> !u.getId().equals(deletedUserId))
                        .collect( Collectors.toList());

                // Update the cache with the filtered list (keeping the same TTL)
                if (!updatedUsers.isEmpty()) {
                    usersRedisTemplate.opsForValue().set(
                            redisKey,
                            ( Users ) updatedUsers ,
                            2,
                            TimeUnit.MINUTES
                    );
                    log.info("Updated 'all_users' cache after deletion of user {}", deletedUserId);
                } else {
                    // If no users left, delete the cache to force a fresh load next time
                    usersRedisTemplate.delete(redisKey);
                }
            } catch (ClassCastException e) {
                log.warn("Failed to update 'all_users' cache - invalid data type, clearing cache", e);
                usersRedisTemplate.delete(redisKey);
            }
        }
}
}
