# Spring AI & Agents: Building Your Local Developer Sidekick

**A hands-on workshop for Java developers**

---

## Workshop Overview

| | |
|---|---|
| **Duration** | 3–4 hours (half-day) |
| **Audience** | Java developers familiar with Spring Boot |
| **Prerequisites** | Java 25, Maven 3.9+, Docker, a code editor |
| **Takeaway** | Two functional AI agents running entirely on your laptop |

You will go from zero to a working **Developer Sidekick** — an AI agent that reads your project, understands your conventions, generates code that fits, and writes it to disk. No cloud API keys required. Everything runs locally.

### The Stack

| Layer | Technology | Role |
|-------|-----------|------|
| Application Framework | Spring Boot 4.0.5 | Foundation |
| AI Integration | Spring AI 2.0.0-M4 | LLM abstraction, RAG, tools |
| Agent Orchestration | Embabel 0.3.4 | Goal-oriented action planning |
| Tool Protocol | Spring AI MCP Client | Filesystem & external tool access |
| Runtime Skills | JavaClaw-style Markdown skills | Hot-loadable agent capabilities |
| Local LLM | Ollama + Llama 3.2 | Chat model (no API key needed) |
| Embeddings | Ollama + nomic-embed-text | Local vector embeddings |
| Vector Database | Qdrant | Document storage for RAG |

---

## Repository Structure

```
spring-ai-agent-workshop/
│
├── WORKSHOP-BLUEPRINT.md          ← You are here
├── README.md                      ← Quick-start for attendees
│
├── infra/
│   ├── docker-compose.yml         ← Ollama + Qdrant
│   └── README.md                  ← Setup instructions
│
├── labs/
│   ├── lab-1-hello-llm/           ← First contact with Spring AI
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/com/workshop/lab1/
│   │       │   ├── Lab1Application.java
│   │       │   └── ChatController.java
│   │       └── resources/application.yml
│   │
│   ├── lab-2-rag-agent/           ← Context-aware documentation agent
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/com/workshop/lab2/
│   │       │   ├── Lab2Application.java
│   │       │   ├── config/RagConfig.java
│   │       │   ├── controller/DocAgentController.java
│   │       │   └── service/DocumentIngestionService.java
│   │       └── resources/
│   │           ├── application.yml
│   │           └── docs/spring-boot-conventions.md
│   │
│   ├── lab-3-agentic-workflows/   ← Embabel goal-oriented agents
│   │   ├── pom.xml
│   │   └── src/main/
│   │       ├── java/com/workshop/lab3/
│   │       │   ├── Lab3Application.java
│   │       │   ├── agent/CodeReviewAgent.java
│   │       │   ├── config/AgentConfig.java
│   │       │   └── domain/
│   │       │       ├── CodeReviewRequest.java
│   │       │       ├── CodeAnalysis.java
│   │       │       └── ReviewReport.java
│   │       └── resources/application.yml
│   │
│   └── lab-4-developer-sidekick/  ← The grand finale
│       ├── pom.xml
│       └── src/main/
│           ├── java/com/workshop/lab4/
│           │   ├── Lab4Application.java
│           │   ├── agent/DeveloperSidekickAgent.java
│           │   ├── config/SidekickConfig.java
│           │   ├── domain/
│           │   │   ├── TaskRequest.java
│           │   │   ├── ProjectContext.java
│           │   │   ├── GeneratedCode.java
│           │   │   └── TaskResult.java
│           │   └── tools/FileSystemTools.java
│           ├── resources/
│           │   ├── application.yml
│           │   ├── docs/
│           │   └── skills/code-review/SKILL.md
│           └── workspace/sample-project/   ← Target project
│
├── solutions/                     ← Completed versions of each lab
│   ├── lab-1/
│   ├── lab-2/
│   ├── lab-3/
│   └── lab-4/
│
└── docs/                          ← Supplemental reference material
```

---

## Infrastructure Setup

All backing services run via Docker Compose. No cloud accounts needed.

```bash
cd infra
docker compose up -d
docker compose exec ollama ollama pull llama3.2
docker compose exec ollama ollama pull nomic-embed-text
```

Verify:
- Ollama API: http://localhost:11434/api/tags
- Qdrant dashboard: http://localhost:6333/dashboard

Full details in `infra/README.md`.

---

## The Narrative Arc

The workshop follows a deliberate progression. Each lab builds on the last, and by the end, attendees have assembled a complete agent from parts they understand individually.

### Lab 1 — Hello, Local LLM (30 min)

**Theme:** "It's just a Spring bean."

**What attendees build:** A Spring Boot REST API that talks to a local LLM via Ollama — no API keys, no cloud, just `ChatClient` and a `@RestController`.

**Key concepts:**
- Spring AI's `ChatClient` as the universal LLM abstraction
- System prompts for shaping assistant behavior
- Ollama as a local, privacy-preserving inference engine
- How Spring Boot auto-configuration wires everything together

**The moment it clicks:** Attendees hit `/chat?message=Explain sealed classes` and get a thoughtful, code-rich response — from a model running on their own machine. The realization that LLM integration is just dependency injection is the first "aha."

**Exercises:**
1. Modify the system prompt to create a "pirate Java tutor" persona
2. Add a `/explain` endpoint with a more focused prompt template
3. Experiment with temperature settings and observe the difference

---

### Lab 2 — Context-Aware Documentation Agent (45 min)

**Theme:** "Your LLM now knows your codebase."

**What attendees build:** A RAG (Retrieval-Augmented Generation) pipeline that ingests project documentation into Qdrant, then answers questions grounded in that context.

**Key concepts:**
- Document ingestion: reading, chunking, and embedding with `TikaDocumentReader` and `TokenTextSplitter`
- Vector storage with Qdrant via Spring AI's `VectorStore` abstraction
- The `QuestionAnswerAdvisor` pattern — RAG as a composable middleware
- Embedding models running locally via Ollama (`nomic-embed-text`)

**The moment it clicks:** The attendee asks "What's our team's convention for error handling?" and the agent responds with specific details from their own documentation — not hallucinated generalities. The LLM is grounded.

**Exercises:**
1. Add your own Markdown docs to the `docs/` folder and re-ingest
2. Open the Qdrant dashboard and inspect the vector embeddings
3. Tune similarity threshold and top-K to see how retrieval quality changes

---

### Lab 3 — Agentic Workflows with Embabel (60 min)

**Theme:** "Stop scripting workflows. Start declaring goals."

**What attendees build:** A code review agent using Embabel's annotation model. Instead of hardcoding "step 1, step 2, step 3," you define typed actions and a goal — the GOAP (Goal-Oriented Action Planning) engine discovers the path automatically.

**Key concepts:**
- Embabel's `@Agent`, `@Action`, and `@AchievesGoal` annotations
- Strongly-typed domain models (Java records) as the glue between actions
- How the GOAP planner connects actions by their input/output types
- Testing agents with Embabel's `FakePromptRunner`

**The moment it clicks:** Attendees add a `SecurityScan` action to the agent. Without changing any workflow code, the planner automatically incorporates it into the execution plan. The flow goes from `analyzeCode → writeReport` to `analyzeCode → scanForVulnerabilities → writeReport` — and they didn't touch a single orchestration line. That is the power of goal-oriented planning.

**Exercises:**
1. Observe the execution plan in the console logs
2. Add a `SecurityScan` action and watch the planner adapt
3. Write a unit test using `FakePromptRunner` to verify the review logic without calling the LLM

---

### Lab 4 — Developer Sidekick: The Grand Finale (60 min)

**Theme:** "Your agent just wrote code to your project. In your IDE. Right now."

**What attendees build:** A full-stack developer assistant that combines RAG knowledge, Embabel's planning, and MCP filesystem tools. It reads your project, understands your patterns, generates convention-following code, and writes it to disk.

**Key concepts:**
- Spring AI's MCP Client for connecting to tool servers (filesystem access)
- Spring AI `@Tool` annotations for native Java function calling
- JavaClaw-style Markdown skills for hot-loadable agent capabilities
- Combining RAG + Agents + Tools in a single coherent application
- Sandboxed workspace access (security by default)

**The "Aha!" Moment — the demo that sells the workshop:**

The facilitator runs this curl command in front of everyone:

```bash
curl -X POST http://localhost:8084/task \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Add a /health endpoint that returns JVM memory usage, active thread count, and uptime as a JSON response",
    "projectPath": "sample-project",
    "type": "GENERATE_CODE"
  }'
```

The agent:
1. Scans `sample-project/` via MCP filesystem tools
2. Reads `pom.xml` to understand dependencies
3. Reads `GreetingController.java` to learn the project's coding style
4. Generates a `HealthController.java` that follows the same record-based response pattern
5. Writes the file to `sample-project/src/main/java/com/example/HealthController.java`
6. Returns a structured report of what it did

The attendee opens their IDE, sees the new file, and it compiles. It follows their project's conventions. It works. The agent just pair-programmed with them.

**Exercises:**
1. Ask the agent to add a test for the new endpoint
2. Drop a custom skill Markdown file into `skills/` and use it
3. Connect an additional MCP server (e.g., a Git MCP server)

---

## Facilitation Notes

### Timing Guide

| Segment | Duration | Cumulative |
|---------|----------|------------|
| Welcome + infra setup | 15 min | 0:15 |
| Lab 1: Hello LLM | 30 min | 0:45 |
| Break | 10 min | 0:55 |
| Lab 2: RAG Agent | 45 min | 1:40 |
| Break | 10 min | 1:50 |
| Lab 3: Embabel Agents | 60 min | 2:50 |
| Break | 10 min | 3:00 |
| Lab 4: Developer Sidekick | 60 min | 4:00 |

### Tips for Facilitators

- **Pre-pull Docker images.** Have attendees run `docker compose pull` during the welcome segment. Ollama model pulls are the biggest time sink.
- **Provide a Wi-Fi-free fallback.** Since everything runs locally, the workshop works without internet after initial setup. This is a selling point.
- **Pair slower attendees.** Labs 3 and 4 are conceptually dense. Pairing helps.
- **Keep the solutions directory sealed** until each lab's debrief. Encourage struggle — that's where learning happens.
- **The Lab 4 demo is your closer.** Save energy for it. Run the curl command on a projector. Let the room watch the file appear in the IDE in real time.

### Common Issues

| Problem | Fix |
|---------|-----|
| Ollama slow on first call | Model is loading into memory. Wait 30s, retry. |
| Qdrant connection refused | Check `docker compose ps`. Ensure port 6334 (gRPC) is mapped. |
| Embabel planner timeout | Increase `embabel.agent.planning.timeout-seconds` or simplify the domain model. |
| MCP server not found | Ensure `npx` is on PATH. Run `npm install -g @modelcontextprotocol/server-filesystem` as fallback. |

---

## Technology Deep Dives

### Why Embabel?

Created by Rod Johnson (yes, the Spring Framework creator), Embabel brings the same philosophy to AI agents: strong typing, testability, and convention over configuration. Instead of writing procedural orchestration code, you declare what the agent can do (`@Action`) and what it should achieve (`@AchievesGoal`). The GOAP planner handles the rest. This means adding a new capability doesn't require rewriting the workflow — the planner adapts automatically.

### Why JavaClaw-style Skills?

JavaClaw pioneered the idea of runtime-extensible agent capabilities via Markdown files. A skill is a folder containing a `SKILL.md` with YAML frontmatter (name, description) and natural-language instructions. Drop it into the skills directory, and the agent picks it up without recompilation. This pattern is powerful for teams: domain experts can extend agent behavior without writing Java code.

### Why Local LLMs?

Running models locally via Ollama provides: no API costs during development, no data leaving the machine (critical for enterprise code), deterministic behavior across workshop attendees, and the ability to run the workshop without internet. The `llama3.2` 3B model is remarkably capable for code tasks and runs comfortably on 16 GB RAM.

---

## What Attendees Take Home

1. A working GitHub repository they can extend
2. Understanding of the Spring AI `ChatClient` abstraction
3. A complete RAG pipeline they can point at their own docs
4. Hands-on experience with Embabel's goal-oriented agent model
5. Knowledge of MCP tool integration for filesystem and API access
6. Confidence that AI-assisted development is a Java-first experience — not a Python-only club

---

## Next Steps for Attendees

After the workshop, attendees can:
- Swap Ollama for a cloud provider (OpenAI, Anthropic) by changing one line of config
- Connect additional MCP servers (Git, database, Kubernetes)
- Build custom Embabel agents for their team's specific workflows
- Explore the A2A (Agent-to-Agent) protocol for multi-agent systems
- Deploy the sidekick as a team-shared service behind Spring Security
