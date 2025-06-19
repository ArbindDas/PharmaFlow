package com.JSR.PharmaFlow.Services;

import com.JSR.PharmaFlow.Repository.JwtTokenRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TokenService {
    private final JwtTokenRepository jwtTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public TokenService( JwtTokenRepository jwtTokenRepository,
                         RedisTemplate<String, String> redisTemplate) {
        this.jwtTokenRepository = jwtTokenRepository;
        this.redisTemplate = redisTemplate;
    }

    public void invalidateAllTokensForUser(String email) {
        // Invalidate JWT tokens in database (if using persistent token storage)
        jwtTokenRepository.deleteAllByUsername(email);

        // Invalidate Redis cache entries
        String pattern = "auth_tokens:" + email + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }

        // Optional: Publish event to other services
        redisTemplate.convertAndSend("user.email.changed", email);
    }
}
