package com.example.demoai.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class MessageCleaner {

    private static final Logger logger = LoggerFactory.getLogger(MessageCleaner.class);

    // Common patterns for thinking models
    private static final Pattern THINK_PATTERN = Pattern.compile("<think>.*?</think>", Pattern.DOTALL);
    private static final Pattern THINKING_PATTERN = Pattern.compile("<thinking>.*?</thinking>", Pattern.DOTALL);
    private static final Pattern INTERNAL_THOUGHT_PATTERN = Pattern.compile("\\[THINKING].*?\\[/THINKING]", Pattern.DOTALL);
    private static final Pattern REASONING_PATTERN = Pattern.compile("\\*\\*Reasoning:\\*\\*.*?(?=\\n\\n|$)", Pattern.DOTALL);

    /**
     * Cleans the complete message by removing thinking sections and extracting final answer
     */
    public String cleanCompleteMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "";
        }

        logger.debug("🧹 Cleaning complete message - Original length: {}", message.length());

        String cleaned = message;

        // Remove thinking tags
        cleaned = THINK_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = THINKING_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = INTERNAL_THOUGHT_PATTERN.matcher(cleaned).replaceAll("");
        cleaned = REASONING_PATTERN.matcher(cleaned).replaceAll("");

        // Clean up extra whitespace
        cleaned = cleaned.replaceAll("\\n{3,}", "\n\n").trim();

        logger.debug("🧹 Message cleaned - New length: {}", cleaned.length());

        if (cleaned.length() < message.length()) {
            logger.info("🔧 Removed thinking content: {} chars -> {} chars", message.length(), cleaned.length());
        }

        return cleaned;
    }

    /**
     * Filters streaming chunks to only emit content outside thinking sections
     */
    public StreamingState processStreamingChunk(String chunk, StreamingState state) {
        if (chunk == null) {
            return state;
        }

        String buffer = state.getBuffer() + chunk;
        StringBuilder output = new StringBuilder();
        boolean insideThinking = state.isInsideThinking();

        String content = buffer;
        int i = 0;

        while (i < content.length()) {
            if (!insideThinking) {
                // Check for start of thinking sections
                if (content.startsWith("<think>", i) || content.startsWith("<thinking>", i) ||
                    content.startsWith("[THINKING]", i)) {
                    insideThinking = true;
                    // Skip to end of opening tag
                    if (content.startsWith("<think>", i)) {
                        i += 7;
                    } else if (content.startsWith("<thinking>", i)) {
                        i += 10;
                    } else if (content.startsWith("[THINKING]", i)) {
                        i += 10;
                    }
                    continue;
                }
                // Not inside thinking, add character to output
                output.append(content.charAt(i));
            } else {
                // Inside thinking, check for end tags
                if (content.startsWith("</think>", i)) {
                    insideThinking = false;
                    i += 8;
                    continue;
                } else if (content.startsWith("</thinking>", i)) {
                    insideThinking = false;
                    i += 11;
                    continue;
                } else if (content.startsWith("[/THINKING]", i)) {
                    insideThinking = false;
                    i += 11;
                    continue;
                }
                // Inside thinking, skip character
            }
            i++;
        }

        // Keep some buffer for incomplete tags
        String finalOutput = output.toString();
        String remainingBuffer = "";

        // If we're at the end and have potential incomplete tags, keep them in buffer
        if (content.endsWith("<think") || content.endsWith("<thinking") ||
            content.endsWith("[THINKING") || content.endsWith("</think") ||
            content.endsWith("</thinking") || content.endsWith("[/THINKING")) {
            remainingBuffer = content.substring(Math.max(0, content.length() - 15));
            finalOutput = output.substring(0, Math.max(0, output.length() - 15));
        }

        return new StreamingState(finalOutput, remainingBuffer, insideThinking);
    }

    /**
     * State holder for streaming processing
     */
    public static class StreamingState {
        private final String output;
        private final String buffer;
        private final boolean insideThinking;

        public StreamingState() {
            this("", "", false);
        }

        public StreamingState(String output, String buffer, boolean insideThinking) {
            this.output = output;
            this.buffer = buffer;
            this.insideThinking = insideThinking;
        }

        public String getOutput() { return output; }
        public String getBuffer() { return buffer; }
        public boolean isInsideThinking() { return insideThinking; }
    }
}
