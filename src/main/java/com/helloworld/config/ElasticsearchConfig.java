package com.helloworld.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * When app.features.elasticsearch.enabled=true, ElasticsearchSearchService is active (DB fallback).
 * Add ElasticsearchDataAutoConfiguration and real ES client here when index is ready.
 */
@Configuration
@ConditionalOnProperty(name = "app.features.elasticsearch.enabled", havingValue = "true")
public class ElasticsearchConfig {
}
