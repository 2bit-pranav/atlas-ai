# atlas-ai
A fully local browser AI agent to automate browser workflows. 

# 📂 Project Structure: atlas-ai

```text
atlas-ai/ (GitHub Root)
├── .git/                       # Master tracking folder
├── .gitignore                  # Global ignores (target, .idea, etc.)
├── README.md                   # Project landing page
├── docs/                       # ALL non-code assets
└── atlas/                      # THE JAVA PROJECT (IntelliJ Module)
    ├── pom.xml                 # Maven dependencies
    └── src/
        ├── main/
        │   ├── java/dev/project/atlas/
        │   │   ├── agent/      # Task Decomposition logic
        │   │   ├── browser/    # Playwright automation
        │   │   ├── storage/    # RAG & Local DB
        │   │   └── ui/         # User Interface
        │   └── resources/      # Prompt templates & Configs
        └── test/               # Unit tests
