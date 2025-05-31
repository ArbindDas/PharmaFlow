package com.JSR.PharmaFlow.Cache;

import com.JSR.PharmaFlow.Entity.Users;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value ( "${spring.redis.host}")
    private String redisHost;

    @Value ( "${spring.redis.port}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory ( ) {
        return new LettuceConnectionFactory ( redisHost , redisPort );
    }

    @Bean
    public RedisCacheManager cacheManager ( RedisConnectionFactory redisConnectionFactory , ObjectMapper objectMapper ) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig ( )
                .entryTtl ( Duration.ofMinutes ( 20 ) )
                .disableCachingNullValues ( )
                .serializeValuesWith ( RedisSerializationContext.SerializationPair.fromSerializer (
                        new GenericJackson2JsonRedisSerializer ( objectMapper )
                ) );

        return RedisCacheManager.builder ( redisConnectionFactory )
                .cacheDefaults ( cacheConfig )
                .build ( );
    }

    @Bean
    public RedisTemplate < String, Users > redisTemplate ( RedisConnectionFactory connectionFactory ) {

        RedisTemplate < String, Users > template = new RedisTemplate <> ( );
        template.setConnectionFactory ( connectionFactory );

        ObjectMapper objectMapper = new ObjectMapper ( );
        objectMapper.registerModule ( new JavaTimeModule ( ) );
        objectMapper.disable ( SerializationFeature.WRITE_DATES_AS_TIMESTAMPS );

        // Use constructor instead of deprecated setObjectMapper
        Jackson2JsonRedisSerializer < Users > serializer = new Jackson2JsonRedisSerializer <> ( objectMapper , Users.class );

        template.setKeySerializer ( new StringRedisSerializer ( ) );
        template.setValueSerializer ( serializer );
        template.setHashKeySerializer ( new StringRedisSerializer ( ) );
        template.setHashValueSerializer ( serializer );

        template.afterPropertiesSet ( );
        return template;
    }
}
