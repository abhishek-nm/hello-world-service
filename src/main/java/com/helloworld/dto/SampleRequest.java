package com.helloworld.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating/updating the sample resource. Add one DTO per write operation
 * (e.g. SampleCreateRequest, SampleUpdateRequest) as your API grows.
 */
public class SampleRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
