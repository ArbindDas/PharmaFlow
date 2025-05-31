package com.JSR.PharmaFlow.Services;



import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestService {


    @Cacheable (value = "testCache", key = "#id")
    public String cacheTest(Long id) {
        log.info("Executing cacheTest for id: {}", id);
        return "cached-value-for-" + id;
    }

}
