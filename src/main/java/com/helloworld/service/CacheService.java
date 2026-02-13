package com.helloworld.service;

import com.helloworld.entity.Sample;

/**
 * Cache abstraction: use Redis when enabled, no-op otherwise (fallback to DB).
 * Currently used for the sample resource; extend or duplicate for other entities when needed.
 */
public interface CacheService {

    Sample get(Long id);

    void put(Sample sample);

    void evict(Long id);
}
