package com.example.demoai.service;

import com.example.demoai.DogA;
import com.example.demoai.util.MessageCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatClient chatClient;
    private final MessageCleaner messageCleaner;
    private final DogA dogA;

    public ChatService(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory, MessageCleaner messageCleaner, DogA dogA) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.messageCleaner = messageCleaner;
        this.dogA = dogA;
        logger.info("🤖 ChatService initialized with Ollama ChatClient, MessageCleaner, and DogA");
    }

    public String sendMessage(String message) {
        logger.info("🚀 Sending message to Ollama LLM");
        logger.debug("📝 Message to LLM: '{}'", message);

        try {
            long startTime = System.currentTimeMillis();

            logger.debug("⏳ Calling Ollama API...");
            String response = chatClient.prompt()
                    .user(message)
                    .tools(dogA)
                    .call()
                    .content();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            logger.info("🎯 Received response from Ollama in {}ms", duration);

            if (response != null) {
                logger.debug("📋 Raw response length: {} characters", response.length());

                // Clean the response to remove thinking content
                String cleanedResponse = messageCleaner.cleanCompleteMessage(response);

                logger.debug("🧹 Cleaned response length: {} characters", cleanedResponse.length());
                logger.debug("🔍 Response preview: '{}'",
                        cleanedResponse.length() > 200 ? cleanedResponse.substring(0, 200) + "..." : cleanedResponse);

                response = cleanedResponse;
            } else {
                logger.warn("⚠️ Received null response from Ollama");
                response = ""; // Set empty string as fallback
            }

            if (duration > 5000) {
                logger.warn("⚠️ Slow response from Ollama: {}ms (consider optimizing)", duration);
            }

            return response;

        } catch (Exception e) {
            logger.error("💥 Failed to communicate with Ollama LLM: {}", e.getMessage());
            logger.debug("🔧 Full error details:", e);
            throw new RuntimeException("LLM communication failed: " + e.getMessage(), e);
        }
    }

    public Flux<String> streamMessage(String message) {
        logger.info("🚀 Starting streaming message to Ollama LLM");
        logger.debug("📝 Message to stream: '{}'", message);

        try {
            long startTime = System.currentTimeMillis();
            logger.debug("⏳ Initiating streaming call to Ollama API...");

            return chatClient.prompt()
                    .user(message)
                    .stream()
                    .content()
                    .scan(new MessageCleaner.StreamingState(), (state, chunk) -> {
                        // Process each chunk through the message cleaner
                        MessageCleaner.StreamingState newState = messageCleaner.processStreamingChunk(chunk, state);
                        logger.debug("📦 Processing chunk - Input: '{}', Output: '{}'",
                                chunk.length() > 20 ? chunk.substring(0, 20) + "..." : chunk,
                                newState.getOutput().length() > 20 ? newState.getOutput().substring(0, 20) + "..." : newState.getOutput());
                        return newState;
                    })
                    .map(MessageCleaner.StreamingState::getOutput)
                    .filter(output -> !output.isEmpty()) // Only emit non-empty cleaned content
                    .doOnSubscribe(subscription ->
                            logger.info("🔄 Client subscribed to cleaned LLM stream"))
                    .doOnComplete(() -> {
                        long endTime = System.currentTimeMillis();
                        logger.info("✅ Cleaned stream completed in {}ms", (endTime - startTime));
                    })
                    .doOnError(error ->
                            logger.error("💥 Stream error: {}", error.getMessage(), error))
                    .onErrorMap(e -> new RuntimeException("LLM streaming failed: " + e.getMessage(), e));

        } catch (Exception e) {
            logger.error("💥 Failed to initialize streaming with Ollama LLM: {}", e.getMessage());
            logger.debug("🔧 Full error details:", e);
            return Flux.error(new RuntimeException("LLM streaming initialization failed: " + e.getMessage(), e));
        }
    }
}
