package com.helloworld.statemachine;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * Defines allowed (state, event) -> next state. Add more transitions or replace with your domain rules.
 * For persistence, move this to DB or config; for Spring State Machine use its transition DSL.
 */
@Component
@ConditionalOnProperty(name = "app.features.statemachine.enabled", havingValue = "true")
public class TransitionConfig {

    private final Map<DemoState, Map<DemoEvent, DemoState>> transitions = new EnumMap<>(DemoState.class);

    public TransitionConfig() {
        add(DemoState.DRAFT, DemoEvent.SUBMIT, DemoState.SUBMITTED);
        add(DemoState.SUBMITTED, DemoEvent.START_REVIEW, DemoState.IN_REVIEW);
        add(DemoState.IN_REVIEW, DemoEvent.APPROVE, DemoState.APPROVED);
        add(DemoState.IN_REVIEW, DemoEvent.REJECT, DemoState.REJECTED);
    }

    private void add(DemoState from, DemoEvent event, DemoState to) {
        transitions.computeIfAbsent(from, k -> new EnumMap<>(DemoEvent.class)).put(event, to);
    }

    public DemoState next(DemoState current, DemoEvent event) {
        Map<DemoEvent, DemoState> fromMap = transitions.get(current);
        return fromMap == null ? null : fromMap.get(event);
    }
}
