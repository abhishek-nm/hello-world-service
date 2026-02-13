package com.helloworld.controller;

import com.helloworld.api.ApiPaths;
import com.helloworld.dto.SampleRequest;
import com.helloworld.entity.Sample;
import com.helloworld.service.SampleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Sample REST controller (neutral name, not domain-specific). Copy this when adding a new resource.
 *
 * <p>Boilerplate usage:
 * <ul>
 *   <li>Copy this class when adding a new resource (e.g. OrderController, UserController).</li>
 *   <li>Update {@link ApiPaths} with your resource path (e.g. /api/orders).</li>
 *   <li>Replace Sample / SampleRequest / SampleService with your entity, DTO, and service.</li>
 *   <li>Keep the same pattern: list, getById, create, and search (or equivalent) methods.</li>
 * </ul>
 */
@RestController
@RequestMapping(ApiPaths.SAMPLES)
public class SampleController {

    private final SampleService sampleService;

    public SampleController(SampleService sampleService) {
        this.sampleService = sampleService;
    }

    // ----- List all -----
    @GetMapping
    public List<Sample> list() {
        return sampleService.findAll();
    }

    // ----- Get by ID -----
    @GetMapping("/{id}")
    public ResponseEntity<Sample> getById(@PathVariable Long id) {
        return sampleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ----- Create -----
    @PostMapping
    public ResponseEntity<Sample> create(@Valid @RequestBody SampleRequest request) {
        Sample sample = new Sample(request.getName(), request.getDescription());
        Sample created = sampleService.create(sample);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ----- Search (query param) -----
    @GetMapping("/search")
    public List<Sample> search(@RequestParam(required = false) String q) {
        return sampleService.search(q);
    }
}
