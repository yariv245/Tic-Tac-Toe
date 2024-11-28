package com.example.tic_tac_toe.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.tic_tac_toe.util.CacheConstant.*;


@Configuration
@EnableCaching
public class CaffeineCacheConfig {
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                BOARD_ID_TO_PLAYER_TURN, USERNAME_TO_PLAYER,
                USERNAME_TO_BOARD_ID, BOARD_ID_TO_SESSIONS,
                BOARD_ID_TO_BOARD);
        cacheManager.setCaffeine(caffeineConfig());

        return cacheManager;
    }

    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500);
//                .expireAfterWrite(Duration.ofMinutes(10));
    }
}
