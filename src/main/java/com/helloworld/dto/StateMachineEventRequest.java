package com.helloworld.dto;

import jakarta.validation.constraints.NotNull;

import com.helloworld.statemachine.DemoEvent;

public class StateMachineEventRequest {

    @NotNull(message = "event is required (SUBMIT, START_REVIEW, APPROVE, REJECT)")
    private DemoEvent event;

    public DemoEvent getEvent() {
        return event;
    }

    public void setEvent(DemoEvent event) {
        this.event = event;
    }
}
