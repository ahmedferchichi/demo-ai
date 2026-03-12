# Agent UI

An Angular 19 frontend for the Demo AI Chat Application. It provides a chat interface that streams AI responses from the Spring Boot backend in real time, rendering them as Markdown.

## 🚀 Features

- **Real-time Streaming Chat**: Connects to the backend streaming endpoint and displays tokens as they arrive via HTTP progress events
- **Markdown Rendering**: AI responses are rendered as formatted Markdown using [ngx-markdown](https://github.com/jfcere/ngx-markdown)
- **Bootstrap 5 Styling**: Clean, responsive UI built with Bootstrap 5 and Bootstrap Icons
- **Loading Indicator**: Spinner shown while the AI response is being streamed
- **Standalone Components**: Uses Angular 19 standalone component architecture (no NgModules)

## 🛠️ Technology Stack

| Technology | Version | Purpose |
|---|---|---|
| Angular | ^19.2.0 | Frontend framework |
| Angular CLI | ^19.2.7 | Build tooling & scaffolding |
| ngx-markdown | ^19.1.1 | Markdown rendering for AI responses |
| Bootstrap | ^5.3.7 | CSS framework & components |
| Bootstrap Icons | ^1.13.1 | Icon library |
| TypeScript | ~5.7.2 | Language |
| Karma / Jasmine | ~6.4 / ~5.1 | Unit testing |

## 📋 Prerequisites

- **Node.js** 18+ and **npm**
- **Angular CLI** ^19.2.7:
  ```bash
  npm install -g @angular/cli
  ```
- The [Demo AI Spring Boot backend](../README.md) running on `http://localhost:8080`

## 🏃‍♂️ Quick Start

### 1. Install Dependencies
```bash
cd agent-ui
npm install
```

### 2. Start the Development Server
```bash
ng serve
```

Open your browser at `http://localhost:4200/`. The application automatically reloads when source files change.

> **Note**: Ensure the Spring Boot backend is running at `http://localhost:8080` before using the chat interface.

## 🏗️ Project Structure

```
agent-ui/
├── src/
│   ├── app/
│   │   ├── app.component.ts          # Root component (hosts ChatComponent)
│   │   ├── app.component.html        # Root template
│   │   ├── app.config.ts             # Application providers (HttpClient, Router, Markdown)
│   │   ├── app.routes.ts             # Route definitions
│   │   └── chat/
│   │       ├── chat.component.ts     # Chat logic: query input & streaming HTTP
│   │       ├── chat.component.html   # Chat template: input, send button, spinner, Markdown output
│   │       └── chat.component.css    # Component-scoped styles
│   ├── styles.css                    # Global styles (Bootstrap & Bootstrap Icons imports)
│   ├── main.ts                       # Application bootstrap
│   └── index.html                    # HTML entry point
├── public/
│   └── favicon.ico
├── angular.json                      # Angular CLI workspace configuration
├── tsconfig.json                     # TypeScript base configuration
├── tsconfig.app.json                 # TypeScript app configuration
├── tsconfig.spec.json                # TypeScript test configuration
└── package.json                      # npm dependencies & scripts
```

## 🔌 Backend Integration

The `ChatComponent` sends user questions to the backend streaming endpoint:

```
GET http://localhost:8080/api/chat/stream?message=<encoded-message>
```

Responses are consumed via Angular's `HttpClient` with `reportProgress: true`, updating the UI token-by-token as the AI generates output. The backend URL is currently hardcoded in `chat.component.ts`.

## 📦 Building

### Development Build (with source maps)
```bash
ng build --configuration development
```

### Production Build
```bash
ng build
```

Build artifacts are written to the `dist/agent-ui/` directory.

## 🧪 Running Unit Tests

```bash
ng test
```

Tests run with the [Karma](https://karma-runner.github.io) test runner and Jasmine. Coverage reports are generated in `coverage/`.

## ⚙️ Configuration

To point the UI at a different backend, update the base URL in `src/app/chat/chat.component.ts`:

```typescript
this.http.get(`http://localhost:8080/api/chat/stream?message=${encodeURIComponent(this.query)}`, ...)
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 🔗 Related

- [Backend README](../README.md) – Spring Boot AI backend documentation
- [Angular CLI Overview](https://angular.dev/tools/cli) – CLI command reference
