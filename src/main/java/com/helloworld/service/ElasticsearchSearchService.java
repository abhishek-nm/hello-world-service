package com.helloworld.service;

import com.helloworld.entity.Sample;
import com.helloworld.repository.SampleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Placeholder for Elasticsearch-backed search when app.features.elasticsearch.enabled=true.
 * Replace with real ElasticsearchTemplate/ElasticsearchOperations query once index and sync are in place.
 */
@Service
@ConditionalOnProperty(name = "app.features.elasticsearch.enabled", havingValue = "true")
public class ElasticsearchSearchService implements SearchService {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchSearchService.class);

    private final SampleRepository sampleRepository;

    public ElasticsearchSearchService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @Override
    public List<Sample> search(String query) {
        log.debug("Elasticsearch enabled: using DB fallback until ES index is configured");
        if (query == null || query.isBlank()) {
            return sampleRepository.findAll();
        }
        String term = query.trim();
        return sampleRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(term, term);
    }
}
