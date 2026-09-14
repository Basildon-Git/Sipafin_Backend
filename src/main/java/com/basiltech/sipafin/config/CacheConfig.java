package com.basiltech.sipafin.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cm = new CaffeineCacheManager(
        );
        cm.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(120, TimeUnit.SECONDS)
                .maximumSize(500));
        return cm;
    }
}
