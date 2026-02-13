package com.helloworld.service;

import com.helloworld.entity.Sample;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed cache. Use configurable key prefix when multiple services share the same Redis (e.g. app.cache.key-prefix=svc-a:).
 */
@Service
@ConditionalOnProperty(name = "app.features.redis.enabled", havingValue = "true")
public class RedisCacheService implements CacheService {

    private final RedisTemplate<String, Sample> redisTemplate;
    private final String keyPrefix;
    private final long ttlSeconds;

    public RedisCacheService(
            RedisTemplate<String, Sample> redisTemplate,
            @Value("${app.cache.key-prefix:sample:}") String keyPrefix,
            @Value("${app.cache.ttl-seconds:300}") long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public Sample get(Long id) {
        return redisTemplate.opsForValue().get(keyPrefix + id);
    }

    @Override
    public void put(Sample sample) {
        if (sample != null && sample.getId() != null) {
            redisTemplate.opsForValue().set(keyPrefix + sample.getId(), sample, Duration.ofSeconds(ttlSeconds));
        }
    }

    @Override
    public void evict(Long id) {
        redisTemplate.delete(keyPrefix + id);
    }
}
