package com.helloworld.service;

import com.helloworld.entity.Sample;
import com.helloworld.repository.SampleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Sample service for the boilerplate resource. Orchestrates repository, cache, and search.
 *
 * <p>Pattern: one service per aggregate/resource. Inject repository + CacheService + SearchService
 * (or your own integrations). Use @Transactional for write methods, @Transactional(readOnly = true) for reads.
 */
@Service
public class SampleService {

    private final SampleRepository sampleRepository;
    private final CacheService cacheService;
    private final SearchService searchService;

    public SampleService(SampleRepository sampleRepository, CacheService cacheService, SearchService searchService) {
        this.sampleRepository = sampleRepository;
        this.cacheService = cacheService;
        this.searchService = searchService;
    }

    @Transactional(readOnly = true)
    public List<Sample> findAll() {
        return sampleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Sample> findById(Long id) {
        Sample cached = cacheService.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<Sample> sample = sampleRepository.findById(id);
        sample.ifPresent(cacheService::put);
        return sample;
    }

    @Transactional
    public Sample create(Sample sample) {
        Sample saved = sampleRepository.save(sample);
        cacheService.put(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Sample> search(String query) {
        return searchService.search(query);
    }
}
