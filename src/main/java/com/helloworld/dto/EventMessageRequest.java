package com.helloworld.dto;

import jakarta.validation.constraints.NotBlank;

public class EventMessageRequest {

    @NotBlank(message = "message is required")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
