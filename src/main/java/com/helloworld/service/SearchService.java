package com.helloworld.service;

import com.helloworld.entity.Sample;

import java.util.List;

/**
 * Search abstraction: use Elasticsearch when enabled, DB (LIKE) fallback otherwise.
 * Currently used for the sample resource.
 */
public interface SearchService {

    List<Sample> search(String query);
}
