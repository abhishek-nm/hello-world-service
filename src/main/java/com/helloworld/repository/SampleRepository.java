package com.helloworld.repository;

import com.helloworld.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Sample Spring Data JPA repository. For a new resource, add an interface extending JpaRepository
 * and add custom query methods here (or in a separate interface + impl if needed).
 */
public interface SampleRepository extends JpaRepository<Sample, Long> {

    List<Sample> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
}
