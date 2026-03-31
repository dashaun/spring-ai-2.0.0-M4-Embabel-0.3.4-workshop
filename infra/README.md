# Infrastructure Setup

This directory contains everything you need to run the workshop's backing services locally.

## Prerequisites

| Tool | Minimum Version | Check |
|------|----------------|-------|
| Java | 25 | `java -version` |
| Maven | 3.9+ | `mvn -version` |
| Docker & Compose | 27+ / v2 | `docker compose version` |
| (Optional) NVIDIA GPU | CUDA 12+ | `nvidia-smi` |

> **No GPU? No problem.** Ollama runs on CPU just fine — responses will simply be slower. The `qwen2.5-coder:1.5b` model is perfectly usable on a modern laptop with 16 GB RAM.

## Quick Start

```bash
# 1. Start the infrastructure
cd infra
docker compose up -d

# 2. Pull the models we'll use in the workshop
docker compose exec ollama ollama pull qwen2.5-coder:1.5b   # Chat/model code model (~1.5 GB)
docker compose exec ollama ollama pull nomic-embed-text     # Embedding model (~274 MB)

# 3. Verify everything is healthy
docker compose ps          # All services should show "healthy"
curl http://localhost:11434/api/tags   # Should list pulled models
redis-cli ping                         # Should return "PONG"
```

## Services at a Glance

| Service | URL | Purpose |
|---------|-----|---------|
| Ollama API | http://localhost:11434 | Local LLM inference |
| Redis | localhost:6379 | Vector storage for RAG |
| RedisInsight UI | http://localhost:8001 | Visual data browser |

## Troubleshooting

**Ollama is slow on first run:** The first inference request after pulling a model loads it into memory. Subsequent calls are fast.

**Port conflicts:** If `11434` or `6379` are occupied, adjust the port mappings in `docker-compose.yml` and the corresponding `application.yml` in each lab.

**GPU not detected:** Remove the `deploy.resources` block from the `ollama` service definition. Ollama will fall back to CPU automatically.

## Tearing Down

```bash
docker compose down            # Stop services, keep data
docker compose down -v         # Stop services AND delete model cache + vector data
```
