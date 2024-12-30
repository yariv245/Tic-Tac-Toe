package com.example.tic_tac_toe.component.impl;

import com.example.tic_tac_toe.component.CaffeineCacheComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CaffeineCacheComponentImpl implements CaffeineCacheComponent {
    private final CacheManager cacheManager;

    public <T> Optional<T> find(String cacheName, String key, Class<T> toClass) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null)
            return Optional.empty();

        return Optional.ofNullable(cache.get(key, toClass));
    }

    public void put(String cacheName, String key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null)
            return;

        cache.put(key, value);
    }

    public boolean evictIfPresent(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);

        if (cache == null)
            return false;

        return cache.evictIfPresent(key);
    }
}
