package com.helloworld.controller;

import com.helloworld.api.ApiPaths;
import com.helloworld.dto.StateMachineEventRequest;
import com.helloworld.statemachine.DemoState;
import com.helloworld.statemachine.DemoEvent;
import com.helloworld.statemachine.StateMachineDemoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Example state machine API. Active when app.features.statemachine.enabled=true.
 * Create a workflow instance, then send events to move through DRAFT -> SUBMITTED -> IN_REVIEW -> APPROVED|REJECTED.
 */
@RestController
@RequestMapping(ApiPaths.STATEMACHINE)
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "app.features.statemachine.enabled", havingValue = "true")
public class StateMachineController {

    private final StateMachineDemoService demoService;

    public StateMachineController(StateMachineDemoService demoService) {
        this.demoService = demoService;
    }

    /** Create new workflow instance (starts in DRAFT). Returns instance id. */
    @PostMapping("/demo")
    public ResponseEntity<Map<String, Object>> create() {
        long id = demoService.create();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "state", DemoState.DRAFT.name()));
    }

    /** Get current state of an instance. */
    @GetMapping("/demo/{id}")
    public ResponseEntity<Map<String, String>> getState(@PathVariable long id) {
        return demoService.getState(id)
                .map(s -> ResponseEntity.ok(Map.of("id", String.valueOf(id), "state", s.name())))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Send an event to transition state. Body: { "event": "SUBMIT" } (SUBMIT, START_REVIEW, APPROVE, REJECT). */
    @PostMapping("/demo/{id}/event")
    public ResponseEntity<Map<String, Object>> sendEvent(@PathVariable long id, @Valid @RequestBody StateMachineEventRequest request) {
        Optional<DemoState> next = demoService.sendEvent(id, request.getEvent());
        if (next.isEmpty()) {
            Optional<DemoState> current = demoService.getState(id);
            if (current.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.unprocessableEntity()
                    .body(Map.of(
                            "message", "Transition not allowed",
                            "currentState", current.get().name(),
                            "event", request.getEvent().name()));
        }
        return ResponseEntity.ok(Map.of("id", id, "state", next.get().name()));
    }

    /** List all demo instances (for testing). */
    @GetMapping("/demo")
    public Map<Long, String> list() {
        Map<Long, DemoState> all = demoService.listAll();
        return all.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> e.getValue().name()));
    }
}
