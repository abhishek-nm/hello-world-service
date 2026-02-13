package com.helloworld.statemachine;

/**
 * Example events that trigger transitions. Replace with your domain events.
 */
public enum DemoEvent {
    SUBMIT,       // DRAFT -> SUBMITTED
    START_REVIEW, // SUBMITTED -> IN_REVIEW
    APPROVE,      // IN_REVIEW -> APPROVED
    REJECT        // IN_REVIEW -> REJECTED
}
