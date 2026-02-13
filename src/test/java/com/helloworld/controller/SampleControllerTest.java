package com.helloworld.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helloworld.contract.UniversalContractExceptionHandler;
import com.helloworld.contract.UniversalResponseAdvice;
import com.helloworld.entity.Sample;
import com.helloworld.service.SampleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SampleController.class)
@Import({ UniversalResponseAdvice.class, UniversalContractExceptionHandler.class })
class SampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SampleService sampleService;

    @Test
    void list_returnsSamples() throws Exception {
        Sample sample = new Sample("sample1", "desc1");
        sample.setId(1L);
        when(sampleService.findAll()).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/samples"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("sample1"))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getById_returns200WhenFound() throws Exception {
        Sample sample = new Sample("sample1", "desc1");
        sample.setId(1L);
        when(sampleService.findById(1L)).thenReturn(Optional.of(sample));

        mockMvc.perform(get("/api/v1/samples/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("sample1"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(sampleService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/samples/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returns201AndBody() throws Exception {
        Sample created = new Sample("new", "new desc");
        created.setId(1L);
        when(sampleService.create(any(Sample.class))).thenReturn(created);

        String body = "{\"name\":\"new\",\"description\":\"new desc\"}";

        mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("new"));
    }

    @Test
    void create_returns400WhenNameBlank() throws Exception {
        mockMvc.perform(post("/api/v1/samples")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.errors[0].errorCode").value("INVALID_REQUEST"));
    }

    @Test
    void search_returnsResults() throws Exception {
        Sample sample = new Sample("match", "desc");
        sample.setId(1L);
        when(sampleService.search("match")).thenReturn(List.of(sample));

        mockMvc.perform(get("/api/v1/samples/search").param("q", "match"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("match"));
    }
}
