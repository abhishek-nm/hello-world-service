package com.helloworld.service;

import com.helloworld.entity.Sample;
import com.helloworld.repository.SampleRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(name = "app.features.elasticsearch.enabled", havingValue = "false")
public class DbSearchService implements SearchService {

    private final SampleRepository sampleRepository;

    public DbSearchService(SampleRepository sampleRepository) {
        this.sampleRepository = sampleRepository;
    }

    @Override
    public List<Sample> search(String query) {
        if (query == null || query.isBlank()) {
            return sampleRepository.findAll();
        }
        String term = query.trim();
        return sampleRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(term, term);
    }
}
