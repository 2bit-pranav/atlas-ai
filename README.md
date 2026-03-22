# atlas-ai
A fully local browser AI agent to automate browser workflows. 

# 📂 Root Structure:

```text
project-atlas/
├── atlas-ui/              # React (Vite) Frontend
├── atlas-manager/         # Java (Spring Boot) Backend
├── atlas-worker/          # Python (FastAPI) Backend
├── .gitignore
└── README.md              # Team documentation & setup instructions
```

# 💻 Frontend Structure (ReactJS)

```text
atlas-ui/
├── public/
├── src/
│   ├── assets/             # Icons, logos, global CSS
│   ├── components/
│   │   ├── chat/           # ChatWindow.jsx, MessageBubble.jsx
│   │   ├── terminal/       # LiveLogViewer.jsx, StatusIndicator.jsx
│   │   └── shared/         # Buttons, Modals, Inputs
│   ├── hooks/              # Custom React hooks
│   │   └── useWebSocket.js # Manages STOMP connection & auto-reconnect
│   ├── services/           # External communication
│   │   ├── apiClient.js    # Standard Axios HTTP calls (REST)
│   │   └── socketClient.js # STOMP over WebSocket setup
│   ├── App.jsx             # Main layout (Sidebar + Chat Area)
│   └── main.jsx
├── index.html
├── package.json
└── vite.config.js
```

# ⚙️ Manager Structure (Java)
```text
atlas-manager/
├── pom.xml                 # Dependencies (Spring Web, WebSocket, LangChain4j)
└── src/
    └── main/
        ├── java/dev/atlas/
        │   ├── AtlasApplication.java
        │   ├── api/                  # REST Controllers & STOMP MessageMappings
        │   │   ├── ChatController.java     # Handles incoming user prompts
        │   │   └── WebhookController.java  # Receives logs from Python via HTTP
        │   ├── config/               # System configurations
        │   │   ├── WebSocketConfig.java    # STOMP /topic/chat and /topic/logs
        │   │   └── AiConfig.java           # LangChain4j Ollama bean setup
        │   ├── ai/                   # AI Pre-processing
        │   │   ├── QueryExpander.java      # LC4j Interface
        │   │   └── TaskDecomposer.java     # LC4j Interface
        │   ├── service/              # Business Logic & HTTP Clients
        │   │   ├── PythonBridgeClient.java # HTTP client to send POST to port 8000
        │   │   └── MemoryService.java      # Handles Vector Search & SQLite
        │   └── model/                # Data Transfer Objects (The API Contracts)
        │       ├── WorkflowState.java
        │       ├── TaskRequest.java
        │       └── UserFact.java
        └── resources/
            ├── application.yml       # Port 8080, DB URLs, Ollama URL
            └── data/                 # Local storage directory (ignored in Git)
                ├── atlas_prefs.db
                └── vector_vault.json
```

# 🤖 Worker Structure (Python)
```text
atlas-worker/
├── requirements.txt          # fastapi, uvicorn, langchain, playwright
├── main.py                   # FastAPI app init & HTTP routes
└── app/
    ├── api/                  # FastAPI Endpoints
    │   └── routes.py         # POST /execute (Receives TaskRequest from Java)
    ├── agents/               # The LangChain execution loops
    │   ├── form_filler.py
    │   ├── job_researcher.py
    │   └── yt_extractor.py
    ├── core/                 # Shared logic & infrastructure
    │   ├── browser.py        # Playwright context & page management
    │   ├── dom_distiller.py  # HTML compression script
    │   └── tools/            # @tool annotated functions
    │       ├── navigation.py # click, type, scroll
    │       ├── extraction.py # read_text, get_links
    │       └── lifeline.py   # consult_java_memory (HTTP GET to port 8080)
    └── schemas/              # Pydantic Models (Input validation)
        ├── request_models.py # Validates incoming JSON from Java
        └── response_models.py# Formats the outgoing JSON back to Java
```


