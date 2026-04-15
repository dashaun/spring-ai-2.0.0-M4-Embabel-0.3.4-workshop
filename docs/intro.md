<!-- .slide: data-background-color="#6db33f" -->

# Spring AI & Agents

### Building Your Local Developer Sidekick

DaShaun Carter | Spring Developer Advocate

Notes:
- Welcome everyone. Today we're building AI agents that run entirely on your laptop — no cloud API keys required.
- 3-4 hour hands-on workshop with four progressive labs.
- Encourage attendees to struggle during labs — that's where learning happens.

---

## What You'll Build Today

A fully local **AI-powered Developer Sidekick** that:

- Talks to an LLM running on your laptop
- Answers questions grounded in your documentation
- Plans and executes multi-step workflows
- Reads your project, generates code, and writes it to disk

**No cloud. No API keys. Everything runs locally.**

---

## The Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 4.0.5 |
| AI Integration | Spring AI 2.0.0-M4 |
| Agent Orchestration | Embabel 0.3.4 |
| Local LLM | Ollama + qwen2.5-coder:1.5b |
| Embeddings | nomic-embed-text |
| Vector DB | Redis Stack |

---

## What is Spring AI?

Spring AI is the **portable LLM abstraction** for Java developers.

- Write once, swap models freely (Ollama, OpenAI, Anthropic, Bedrock, Azure...)
- Auto-configured by Spring Boot — it's just a dependency and a bean
- **Advisors API** — compose RAG, memory, and guardrails as middleware
- **@Tool** — expose any Java method as a callable LLM function
- **Structured Output** — parse LLM responses directly into Java records

> If you already know Spring, you already know how to use Spring AI.

---

## What is Embabel?

Embabel is a **type-safe agent orchestration framework** for Java — built by Rod Johnson, the creator of the Spring Framework.

- Declare **what your agent can do** (`@Action`) and **what it should achieve** (`@AchievesGoal`)
- The GOAP planner **discovers the execution path** at runtime — no orchestration code
- Actions connect by **matching Java types** — strongly typed in, strongly typed out
- **Deterministic by design**: the agent can only execute what you've explicitly defined

> "Stop scripting workflows. Start declaring goals."

---

## Workshop Progression

```text
Lab 1: Hello LLM          → "It's just a Spring bean"
Lab 2: RAG Agent           → "Your LLM knows your codebase"
Lab 3: MCP Server          → "Give it tools — in pure Java"
Lab 4: Agentic Workflows   → "Declare goals, not steps"
Lab 5: Developer Sidekick  → "It just wrote code to your project"
```

Each lab builds on the last.

Notes:
- Timing: 4-5 hours total (with 4 breaks)
- Lab 1: 30 min | Lab 2: 45 min | Lab 3: 45 min | Lab 4: 60 min | Lab 5: 60 min
- Breaks: 10 min each after Labs 1, 2, 3, and 4
- Solutions directory is sealed until debriefs

---

## Prerequisites

- Java 25
- Docker Compose v2
- 8 GB RAM (16 GB recommended)
- A code editor / IDE
- `npx` on PATH

Notes:
- Pre-pull Docker images during welcome: `docker compose pull`
- Ollama model pulls take several minutes — start this early
- All labs are self-contained — no cloud API keys needed
- After setup, workshop works offline

---

## Clone & Setup

```bash
git clone https://github.com/dashaun/spring-ai-2.0.0-M4-Embabel-0.3.4-workshop
cd spring-ai-2.0.0-M4-Embabel-0.3.4-workshop
```

---

## Infrastructure Setup

```bash
cd infra
docker compose up -d
```

Pull the models (this takes a few minutes):

```bash
docker compose exec ollama ollama pull qwen2.5-coder:1.5b
docker compose exec ollama ollama pull nomic-embed-text
```

Notes:
- Start this early — model pulls take several minutes
- Wi-Fi-free fallback: pre-pull to a USB drive before the workshop

---

## Verify Infrastructure

```bash
# Ollama API — should list pulled models
curl http://localhost:11434/api/tags

# Redis — should return PONG
redis-cli ping
```

RedisInsight UI: http://localhost:8001

Notes:
- Common issue: Ollama slow on first call — model loads into memory, wait ~30s and retry
- Common issue: Redis connection refused — run `docker compose ps` and check container health
