# Demo AI Chat Application

A Spring Boot application that provides an AI-powered chat interface using Spring AI with Ollama integration.

## 🚀 Features

- **AI Chat Interface**: RESTful API endpoints for chat functionality
- **Ollama Integration**: Uses DeepSeek R1 1.5B model via Ollama
- **Real-time Streaming**: Supports both regular and streaming chat responses
- **Message Cleaning**: Automatic removal of thinking tags from AI responses
- **API Documentation**: Integrated Swagger/OpenAPI documentation
- **Cross-Origin Support**: CORS enabled for frontend integration

## 🛠️ Technology Stack

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring AI 1.0.2**
- **Spring WebFlux** (for reactive streaming)
- **Maven** (build tool)
- **Ollama** (AI model serving)
- **SpringDoc OpenAPI** (API documentation)

## 📋 Prerequisites

Before running this application, ensure you have:

1. **Java 21** or higher installed
2. **Maven 3.6+** installed
3. **Ollama** installed and running on `http://localhost:11434`
4. **DeepSeek R1 1.5B model** pulled in Ollama:
   ```bash
   ollama pull deepseek-r1:1.5b
   ```

## 🏃‍♂️ Quick Start

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/demo-ai.git
cd demo-ai
```

### 2. Install Dependencies
```bash
mvn clean install
```

### 3. Start Ollama (if not already running)
```bash
ollama serve
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

Once the application is running, you can access:

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## 🔌 API Endpoints

### Chat Endpoint
- **POST** `/api/chat`
  - Send a chat message and receive AI response
  - Request body: `{"message": "your message here"}`
  - Response: `{"response": "AI response"}`

### Streaming Chat Endpoint
- **POST** `/api/chat/stream`
  - Send a chat message and receive streaming AI response
  - Request body: `{"message": "your message here"}`
  - Response: Server-Sent Events (SSE) stream

## ⚙️ Configuration

Key configuration properties in `application.properties`:

```properties
# AI Model Configuration
spring.ai.ollama.chat.model=deepseek-r1:1.5b
spring.ai.ollama.base-url=http://localhost:11434

# Message Cleaning
app.message-cleaner.enabled=true
app.message-cleaner.remove-thinking-tags=true

# Logging
logging.level.com.example.demoai=DEBUG
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/com/example/demoai/
│   │   ├── DemoAiApplication.java          # Main application class
│   │   ├── controller/
│   │   │   └── ChatController.java         # REST controller
│   │   ├── dto/
│   │   │   ├── ChatRequest.java           # Request DTO
│   │   │   └── ChatResponse.java          # Response DTO
│   │   ├── model/
│   │   │   └── ConversationMessage.java   # Message model
│   │   ├── service/
│   │   │   └── ChatService.java           # Business logic
│   │   └── util/
│   │       └── MessageCleaner.java        # Message processing utility
│   └── resources/
│       └── application.properties         # Configuration
└── test/
    └── java/
        └── com/example/demoai/
            └── DemoAiApplicationTests.java # Tests
```

## 🧪 Testing

Run the tests using Maven:

```bash
mvn test
```

## 🐳 Docker Support

Create a `Dockerfile` in the project root:

```dockerfile
FROM openjdk:21-jdk-slim
VOLUME /tmp
COPY target/demo-ai-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run with Docker:

```bash
mvn clean package
docker build -t demo-ai .
docker run -p 8080:8080 demo-ai
```

## 🔧 Development

### Running in Development Mode

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Building for Production

```bash
mvn clean package -Pprod
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙋‍♂️ Support

If you have any questions or issues, please open an issue on GitHub or contact the maintainers.

## 📝 Changelog

### v0.0.1-SNAPSHOT
- Initial release
- Basic chat functionality with Ollama integration
- Streaming support
- Message cleaning utilities
- API documentation with Swagger

---

**Happy Coding! 🎉**
