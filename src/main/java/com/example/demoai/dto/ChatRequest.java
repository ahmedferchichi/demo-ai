package com.example.demoai.dto;

public record ChatRequest(String message) {

    public ChatRequest {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
    }
}
