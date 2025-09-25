package com.example.demoai.dto;

public record ChatResponse(String response, boolean success, String error) {

    public static ChatResponse success(String response) {
        return new ChatResponse(response, true, null);
    }

    public static ChatResponse error(String error) {
        return new ChatResponse(null, false, error);
    }
}
