package com.helloworld.service;

import com.helloworld.entity.Sample;
import com.helloworld.repository.SampleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SampleServiceTest {

    @Mock
    private SampleRepository sampleRepository;

    @Mock
    private CacheService cacheService;

    @Mock
    private SearchService searchService;

    private SampleService sampleService;

    @BeforeEach
    void setUp() {
        sampleService = new SampleService(sampleRepository, cacheService, searchService);
    }

    @Test
    void findById_returnsFromCache_whenPresent() {
        Sample cached = new Sample("cached", "desc");
        cached.setId(1L);
        when(cacheService.get(1L)).thenReturn(cached);

        Optional<Sample> result = sampleService.findById(1L);

        assertThat(result).contains(cached);
        verify(sampleRepository, never()).findById(any());
    }

    @Test
    void findById_getsFromRepoAndPutsInCache_whenNotInCache() {
        Sample sample = new Sample("name", "desc");
        sample.setId(1L);
        when(cacheService.get(1L)).thenReturn(null);
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample));

        Optional<Sample> result = sampleService.findById(1L);

        assertThat(result).contains(sample);
        verify(cacheService).put(sample);
    }

    @Test
    void create_savesAndPutsInCache() {
        Sample toSave = new Sample("new", "desc");
        Sample saved = new Sample("new", "desc");
        saved.setId(1L);
        when(sampleRepository.save(toSave)).thenReturn(saved);

        Sample result = sampleService.create(toSave);

        assertThat(result.getId()).isEqualTo(1L);
        verify(cacheService).put(saved);
    }

    @Test
    void search_delegatesToSearchService() {
        List<Sample> samples = List.of(new Sample("a", "b"));
        when(searchService.search("q")).thenReturn(samples);

        assertThat(sampleService.search("q")).isEqualTo(samples);
    }
}
