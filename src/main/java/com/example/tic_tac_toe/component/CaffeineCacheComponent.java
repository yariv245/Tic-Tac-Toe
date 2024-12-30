package com.example.tic_tac_toe.component;

import java.util.Optional;

public interface CaffeineCacheComponent {
    <T> Optional<T> find(String cacheName, String key, Class<T> toClass);

    void put(String cacheName, String key, Object value);

    boolean evictIfPresent(String cacheName, String key);
}
