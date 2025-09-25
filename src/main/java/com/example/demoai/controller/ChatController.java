package com.example.demoai.controller;

import com.example.demoai.dto.ChatRequest;
import com.example.demoai.dto.ChatResponse;
import com.example.demoai.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        logger.info("🔥 Received chat request: '{}'", request.message());
        logger.debug("📨 Request details - Message length: {} characters", request.message().length());

        try {
            long startTime = System.currentTimeMillis();
            String response = chatService.sendMessage(request.message());
            long endTime = System.currentTimeMillis();

            logger.info("✅ Successfully processed chat request in {}ms", (endTime - startTime));
            logger.debug("📤 Response preview: '{}'",
                    response.length() > 100 ? response.substring(0, 100) + "..." : response);

            return ResponseEntity.ok(ChatResponse.success(response));
        } catch (Exception e) {
            logger.error("❌ Error processing chat request: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ChatResponse.error("Error processing request: " + e.getMessage()));
        }
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamChat(@RequestParam String message) {
        logger.info("🔥 Received streaming chat request: '{}'", message);
        logger.debug("📨 Request details - Message length: {} characters", message.length());

        return chatService.streamMessage(message)
                .doOnSubscribe(subscription ->
                        logger.info("🚀 Starting stream for client"))
                .doOnComplete(() ->
                        logger.info("✅ Stream completed successfully"))
                .doOnError(error ->
                        logger.error("❌ Stream error: {}", error.getMessage(), error))
                .onErrorResume(error -> {
                    logger.error("💥 Fallback: Stream failed with error: {}", error.getMessage());
                    return Flux.just("Error: " + error.getMessage());
                });
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> streamChatPost(@RequestBody ChatRequest request) {
        logger.info("🔥 Received streaming chat POST request: '{}'", request.message());
        logger.debug("📨 Request details - Message length: {} characters", request.message().length());

        return chatService.streamMessage(request.message())
                .doOnSubscribe(subscription ->
                        logger.info("🚀 Starting stream for client"))
                .doOnComplete(() ->
                        logger.info("✅ Stream completed successfully"))
                .doOnError(error ->
                        logger.error("❌ Stream error: {}", error.getMessage(), error))
                .onErrorResume(error -> {
                    logger.error("💥 Fallback: Stream failed with error: {}", error.getMessage());
                    return Flux.just("Error: " + error.getMessage());
                });
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        logger.info("🏥 Health check requested");
        return ResponseEntity.ok("Chat service is running");
    }
}
