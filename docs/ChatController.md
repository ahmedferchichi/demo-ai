# ChatController

## Overview

`ChatController` is the main **REST controller** of the Demo AI application.  
It is the entry point for all HTTP requests related to the AI chat feature.

The class lives at:

```
src/main/java/com/example/demoai/controller/ChatController.java
```

It is annotated with:

| Annotation | Purpose |
|---|---|
| `@RestController` | Marks the class as a Spring MVC controller where every method returns a JSON/text response body (no view rendering). |
| `@RequestMapping("/api/chat")` | All endpoints in this controller are prefixed with `/api/chat`. |
| `@CrossOrigin(origins = "*")` | Enables CORS for every origin, allowing the Angular front-end (or any other client) to call the API from a different domain/port. |

---

## Dependencies

`ChatController` has a single dependency injected via constructor:

| Field | Type | Description |
|---|---|---|
| `chatService` | `ChatService` | The service layer that communicates with the Ollama AI model and applies response cleaning. |

A `Logger` (SLF4J) is used throughout the class for structured, emoji-annotated log messages that make it easy to trace requests in the application logs.

---

## Endpoints

### 1. `POST /api/chat` — Standard Chat

```
POST /api/chat
Content-Type: application/json

{
  "message": "Hello, how are you?"
}
```

**What it does**

1. Logs the incoming message.
2. Delegates to `ChatService.sendMessage()` to call the AI model and get a cleaned response.
3. Measures and logs the processing time.
4. Returns a `200 OK` with a `ChatResponse` body on success.
5. Returns a `400 Bad Request` with an error body if an exception is thrown.

**Response (success)**

```json
{
  "response": "I'm doing great, thanks for asking!",
  "success": true,
  "error": null
}
```

**Response (error)**

```json
{
  "response": null,
  "success": false,
  "error": "Error processing request: <reason>"
}
```

---

### 2. `GET /api/chat/stream` — Streaming Chat (GET)

```
GET /api/chat/stream?message=Hello
Accept: text/plain
```

**What it does**

1. Reads the `message` query parameter.
2. Delegates to `ChatService.streamMessage()`, which returns a reactive `Flux<String>`.
3. Attaches lifecycle hooks:
   - **`doOnSubscribe`** — logs when a client connects to the stream.
   - **`doOnComplete`** — logs successful stream completion.
   - **`doOnError`** — logs any error during streaming.
   - **`onErrorResume`** — falls back to emitting a single `"Error: <reason>"` string instead of crashing the stream.
4. The response is a plain-text stream (Server-Sent Events compatible).

---

### 3. `POST /api/chat/stream` — Streaming Chat (POST)

```
POST /api/chat/stream
Content-Type: application/json
Accept: text/plain

{
  "message": "Tell me a story"
}
```

Identical behavior to the GET variant above, but accepts the message as a JSON body (`ChatRequest`) instead of a query parameter. Useful when the message is long or contains special characters.

---

### 4. `GET /api/chat/health` — Health Check

```
GET /api/chat/health
```

Returns a plain `200 OK` with the body:

```
Chat service is running
```

Useful for readiness/liveness probes (e.g. Docker, Kubernetes, or a load balancer).

---

## DTOs

### `ChatRequest`

A Java `record` that wraps the incoming message:

```java
public record ChatRequest(String message) { ... }
```

The compact constructor validates that `message` is not null or blank, throwing an `IllegalArgumentException` early if the constraint is violated.

### `ChatResponse`

A Java `record` that wraps the outgoing response:

```java
public record ChatResponse(String response, boolean success, String error) { ... }
```

Two static factory methods are provided:
- `ChatResponse.success(String response)` — used on the happy path.
- `ChatResponse.error(String error)` — used when an exception is caught.

---

## Flow Diagram

```
HTTP Client
    │
    │  POST /api/chat  (or /api/chat/stream)
    ▼
ChatController
    │
    │  delegates to
    ▼
ChatService
    │
    │  builds prompt and calls
    ▼
Spring AI ChatClient  ──►  Ollama (DeepSeek R1 1.5B)
    │
    │  raw AI response
    ▼
MessageCleaner  (strips <think>…</think> tags)
    │
    │  cleaned response
    ▼
ChatController
    │
    │  wraps in ChatResponse / Flux<String>
    ▼
HTTP Client
```

---

## Logging

All log messages use emojis as visual markers to make scanning log output quick:

| Emoji | Meaning |
|---|---|
| 🔥 | Incoming request received |
| 📨 | Request detail (debug) |
| ✅ | Success |
| ❌ | Error |
| 🚀 | Stream starting |
| 💥 | Fatal/fallback error |
| 🏥 | Health check |

---

## Related Classes

| Class | Role |
|---|---|
| [`ChatService`](../src/main/java/com/example/demoai/service/ChatService.java) | Business logic — communicates with the AI model and cleans responses |
| [`ChatRequest`](../src/main/java/com/example/demoai/dto/ChatRequest.java) | DTO for incoming chat messages |
| [`ChatResponse`](../src/main/java/com/example/demoai/dto/ChatResponse.java) | DTO for outgoing chat responses |
| [`MessageCleaner`](../src/main/java/com/example/demoai/util/MessageCleaner.java) | Utility that removes `<think>` tags from AI output |
