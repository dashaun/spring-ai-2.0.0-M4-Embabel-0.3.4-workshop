# Spring AI & Agents: Building Your Local Developer Sidekick

A hands-on workshop where Java developers build AI agents that run entirely on their laptop — no cloud API keys required.

## What You'll Build

1. **Lab 1** — A Spring Boot app that chats with a local LLM via Ollama
2. **Lab 2** — A RAG-powered documentation agent backed by Redis
3. **Lab 3** — A code review agent using Embabel's goal-oriented planning
4. **Lab 4** — A full developer sidekick that reads your project and writes code to disk

## Quick Start

```bash
# 1. Start infrastructure
cd infra && docker compose up -d

# 2. Pull models
docker compose exec ollama ollama pull qwen2.5-coder:1.5b
docker compose exec ollama ollama pull nomic-embed-text

# 3. Run Lab 1
cd ../labs/lab-1-hello-llm
mvn spring-boot:run

# 4. Test it
curl "http://localhost:8080/chat?message=Hello"
```

## Prerequisites

- Java 25
- Maven 3.9+
- Docker with Compose v2
- 16 GB RAM recommended (8 GB minimum)
- No internet required after initial setup

## Workshop Blueprint

See [WORKSHOP-BLUEPRINT.md](WORKSHOP-BLUEPRINT.md) for the full facilitator guide, narrative arc, and timing.

## The Stack

| Component | Technology |
|-----------|-----------|
| Framework | Spring Boot 4.0.5 |
| AI Layer | Spring AI 2.0.0-M4 |
| Agents | Embabel 0.3.4 |
| Tools | Spring AI MCP Client |
| LLM | Ollama + qwen2.5-coder:1.5b |
| Embeddings | nomic-embed-text |
| Vector DB | Redis Stack (RediSearch) |
