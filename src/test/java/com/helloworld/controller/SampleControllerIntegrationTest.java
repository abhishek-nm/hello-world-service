package com.helloworld.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SampleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createThenGetAndSearch() throws Exception {
        String createBody = "{\"name\":\"Integration Sample\",\"description\":\"For integration test\"}";

        mockMvc.perform(post("/api/v1/samples").contentType(APPLICATION_JSON).content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").value("Integration Sample"));

        mockMvc.perform(get("/api/v1/samples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));

        mockMvc.perform(get("/api/v1/samples/search").param("q", "Integration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void getById_returns404ForMissingId() throws Exception {
        mockMvc.perform(get("/api/v1/samples/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void featuresEndpoint_returnsFlags() throws Exception {
        mockMvc.perform(get("/api/v1/status/features"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.postgres").exists())
                .andExpect(jsonPath("$.data.redis").exists())
                .andExpect(jsonPath("$.data.elasticsearch").exists())
                .andExpect(jsonPath("$.data.kafka").exists())
                .andExpect(jsonPath("$.data.rabbitmq").exists());
    }
}
