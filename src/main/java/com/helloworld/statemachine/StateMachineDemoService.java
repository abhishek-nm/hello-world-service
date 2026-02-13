package com.helloworld.statemachine;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Example state machine service: in-memory store + transition rules.
 * When app.features.statemachine.enabled=true, use this as the pattern; for production persist state (e.g. JPA entity with state column).
 * Can be replaced by Spring State Machine (spring-statemachine) for complex workflows.
 */
@Service
@ConditionalOnProperty(name = "app.features.statemachine.enabled", havingValue = "true")
public class StateMachineDemoService {

    private final TransitionConfig transitionConfig;
    private final Map<Long, DemoState> stateStore = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public StateMachineDemoService(TransitionConfig transitionConfig) {
        this.transitionConfig = transitionConfig;
    }

    /** Create a new workflow instance in DRAFT. */
    public long create() {
        long id = idGenerator.getAndIncrement();
        stateStore.put(id, DemoState.DRAFT);
        return id;
    }

    public Optional<DemoState> getState(long id) {
        return Optional.ofNullable(stateStore.get(id));
    }

    /**
     * Apply event; returns new state if transition is allowed, empty otherwise.
     */
    public Optional<DemoState> sendEvent(long id, DemoEvent event) {
        DemoState current = stateStore.get(id);
        if (current == null) return Optional.empty();
        DemoState next = transitionConfig.next(current, event);
        if (next == null) return Optional.empty();
        stateStore.put(id, next);
        return Optional.of(next);
    }

    public Map<Long, DemoState> listAll() {
        return Map.copyOf(stateStore);
    }
}
