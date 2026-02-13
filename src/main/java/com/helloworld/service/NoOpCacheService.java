package com.helloworld.service;

import com.helloworld.entity.Sample;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.features.redis.enabled", havingValue = "false")
public class NoOpCacheService implements CacheService {

    @Override
    public Sample get(Long id) {
        return null;
    }

    @Override
    public void put(Sample sample) {
        // no-op
    }

    @Override
    public void evict(Long id) {
        // no-op
    }
}
