<!-- .slide: data-background-color="#6db33f" -->

# Recap

## What You Built Today

Notes:
- Timing: ~10 minutes
- Review the journey: ChatClient → RAG → GOAP → Tools
- The demo that sold the workshop: Lab 4's curl command that writes code to the project

---

## The Journey

| Lab | Concept | Superpower |
|-----|---------|------------|
| 1 | ChatClient | Talk to an LLM |
| 2 | RAG | Ground it in your data |
| 3 | MCP Server | Give it tools — in pure Java |
| 4 | GOAP Agents | Plan multi-step workflows |
| 5 | Full Stack | Read and write your project |

Each lab was a building block. Lab 5 assembled them all.

---

## What You Take Home

1. A working GitHub repo you can extend
2. Understanding of Spring AI's `ChatClient` abstraction
3. A complete RAG pipeline for your own docs
4. Hands-on experience with Embabel's GOAP model
5. Knowledge of MCP tool integration

**AI-assisted development is a Java-first experience.**

Notes:
- The "java-first" message is important — Java developers are first-class in AI now
- This is not a Python-only club — Java has first-class LLM support via Spring AI

---

## Common Issues

| Problem | Fix |
|---------|-----|
| Ollama slow on first call | Model loading — wait ~30s, retry |
| Redis connection refused | `docker compose ps` — check container health |
| Embabel planner timeout | Increase `embabel.planning.timeout-seconds` in `application.yml` |
| Lab 5 can't connect to MCP | Ensure Lab 3 is running on port 8083: `curl -N http://localhost:8083/sse` |

---

## Next Steps

- Try other models on your own infrastructure
- Connect more MCP servers (Git, database, Kubernetes)
- Build custom Embabel agents for your team's workflows
- Explore the A2A protocol for multi-agent systems
- Deploy the sidekick as a team service behind Spring Security

Notes:
- Why Embabel? Rod Johnson (Spring Framework creator) built it with strong typing, testability, concurrency over config
- Why Local LLMs? No API costs, no data leakage, deterministic across attendees, works offline

---

<!-- .slide: data-background-color="#6db33f" -->

# Thank You!

### Questions?

DaShaun Carter | @dashaun
