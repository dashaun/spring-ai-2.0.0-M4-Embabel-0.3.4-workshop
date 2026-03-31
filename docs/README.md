# Spring AI & Agents

### Building Your Local Developer Sidekick

[A hands-on workshop for Java developers](https://github.com/dashaun/spring-ai-2.0.0-M4-Embabel-0.3.4-workshop)

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

## Workshop Progression

```text
Lab 1: Hello LLM         → "It's just a Spring bean"
Lab 2: RAG Agent          → "Your LLM knows your codebase"
Lab 3: Agentic Workflows  → "Declare goals, not steps"
Lab 4: Developer Sidekick → "It just wrote code to your project"
```

Each lab builds on the last.

Notes:
- Timing: 3-4 hours total (with 3 breaks)
- Lab 1: 30 min | Lab 2: 45 min | Lab 3: 60 min | Lab 4: 60 min
- Breaks: 10 min each after Labs 1, 2, and 3
- Encourage attendees to struggle during labs — that's where learning happens
- Solutions directory is sealed until debriefs
```

---

## Prerequisites

- Java 25, Maven 3.9+
- Docker Compose v2
- 8GB RAM (16 GB recommended)
- A code editor / IDE
- npx

Notes:
- Pre-pull Docker images during welcome: `docker compose pull`
- Ollama model pulls take several minutes — start this early
- All labs are self-contained — no cloud API keys needed
- After setup, workshop works offline (selling point!)
- Pair slower attendees for Labs 3 and 4 (conceptually dense)
```

---

```bash
git clone https://github.com/dashaun/spring-ai-2.0.0-M4-Embabel-0.3.4-workshop
cd spring-ai-2.0.0-M4-Embabel-0.3.4-workshop
```


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

---

## Verify Infrastructure

```bash
# Ollama API — should list pulled models
curl http://localhost:11434/api/tags

# Redis — should return PONG
redis-cli ping
```

RedisInsight UI: http://localhost:8001

---

<!-- .slide: data-background="#1a73e8" data-background-transition="zoom" -->

# Lab 1

## Hello, Local LLM

*"It's just a Spring bean."*

Notes:
- **Timing**: 30 minutes
- **Key message**: "It's just a Spring bean" — LLM integration is dependency injection
- **Common issue**: Ollama slow on first call (wait 30s, retry)
- **Demo script**: `curl "http://localhost:8080/chat?message=Explain sealed classes"`
- **Exercise tip**: Ask attendees to modify system prompt to create "pirate Java tutor"
- **Aha moment**: Realization that LLM integration feels like any other Spring service

---

## Lab 1 — What You'll Build

A Spring Boot REST API that talks to a local LLM

- One dependency, one bean, one endpoint
- No API keys, no cloud — just Ollama on your laptop
- Spring AI's `ChatClient` abstracts the LLM provider

---

## Key Concept: ChatClient

Spring AI's universal LLM abstraction

```java [25: 1-7,14-15]
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
      this.chatClient = chatClientBuilder
              .defaultSystem("""
                      You are a helpful Java developer assistant.
                      You give concise, accurate answers with code examples when appropriate.
                      Always use modern Java idioms (records, pattern matching, virtual threads).
                      """)
              .build();
    }
}
```

Just like any other Spring bean — injected via the constructor.

---

## Key Concept: System Prompts

System prompts shape the assistant's personality:

```java [32:]
          .defaultSystem("""
              You are a helpful Java developer assistant.
              You give concise, accurate answers with code examples when appropriate.
              Always use modern Java idioms (records, pattern matching, virtual threads).
              """)
```

The system prompt is sent with every request — the user never sees it.

---

## Key Concept: Calling the LLM

```java [40:]
    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "Hello!") String message) {
      return chatClient.prompt()
              .user(message)
              .call()
              .content();
    }
```

- `.prompt()` — start building a request
- `.user(message)` — the user's message
- `.call()` — send to the LLM
- `.content()` — extract the text response

---

## Lab 1 — Configuration

```yaml [4:]
spring:
  application:
    name: lab-1-hello-llm
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: qwen2.5-coder:1.5b
        options:
          temperature: 0.7
          num-predict: 512
```

Spring Boot auto-configuration wires Ollama as the AI provider.

---

## Lab 1 — Hands-On

```bash
cd labs/lab-1-hello-llm
./mvnw spring-boot:run
```

Test it:

```bash
curl "http://localhost:8080/chat?message=Explain%20sealed%20classes"

http :8080/chat message=="Explain sealed classes"
```

---

## Lab 1 — Exercise 1

**Modify the system prompt** to make the assistant respond as a pirate.

Hint: Change line 36 in `ChatController.java`:

```java [32:]
                .defaultSystem("""
                    You are a helpful Java developer assistant.
                    You give concise, accurate answers with code examples when appropriate.
                    Always use modern Java idioms (records, pattern matching, virtual threads).
                    Respond in the style of a pirate!
                    """)
```

Restart the Lab 1 Spring Boot app.

```bash
curl "http://localhost:8080/chat"
#or
http :8080/chat
```




---

## Lab 1 — Exercise 2

**Add an `/explain` endpoint** with a focused prompt template:

```java
@GetMapping("/explain")
public String explain(
        @RequestParam(defaultValue = "HashMap") String concept) {
    return chatClient.prompt()
            .system("""
                You are a helpful Spring Developer Advocate.
                You give concise, accurate answers.
                Respond as if teaching a 100-level Java course.
                """)
            .user(u -> u.text("Explain the Java concept {concept}")
                        .param("concept", concept))
            .call()
            .content();
}
```
Note:`.system()` on the prompt **overrides** the default system prompt.

Restart Lab 1

```bash
http :8080/explain
http :8080/chat message=="What is new in Java 26"
http :8080/chat message=="What is your data cutoff date?"
```



Notes:
- **Exercise 1**: Modify system prompt to create a pirate persona
- **Exercise 2**: Add `/explain` endpoint with focused template
- **Exercise 3**: Experiment with temperature settings
- **Validate**: Run `./mvnw test` to verify integration
- **Key takeaways**: 
  - ChatClient.Builder is auto-configured
  - System prompts control personality without code changes
  - Ollama runs everything locally
```

---

## Lab 1 — Validate

Run the integration test:

```bash
./mvnw test
```

The test verifies:
- The `/chat` endpoint returns a non-empty response
- The application context loads with Ollama configured

---

## Lab 1 — Key Takeaways

- Spring AI makes LLM integration feel like any Spring service
- `ChatClient.Builder` is auto-configured by Spring Boot
- System prompts control personality without changing code
- Ollama runs everything locally — no API keys needed
- Models hava a cutoff date

---

<!-- .slide: data-background="#34a853" data-background-transition="zoom" -->

# Lab 2

## Context-Aware Documentation Agent

*"Your LLM now knows your codebase."*

Notes:
- **Timing**: 45 minutes
- **Key message**: "Your LLM now knows your codebase"
- **Common issues**:
  - Vector store not populated (wait for ApplicationReadyEvent)
  - Redis connection refused (check `docker compose ps`)
- **Demo script**: "What's our team's convention for error handling?"
- **Demo tip**: Show RedisInsight UI at http://localhost:8001
- **Exercise tip**: Add your own Markdown docs to docs/ folder
- **Aha moment**: LLM responds with specific detail from YOUR docs, not hallucinated generalities

---

## The Problem with Vanilla LLMs

Ask an LLM: *"What's our team's convention for error handling?"*

It will **hallucinate** — it doesn't know your codebase.

**RAG** (Retrieval-Augmented Generation) fixes this by grounding the LLM in your actual documentation.

---

## Lab 2 — What You'll Build

A RAG pipeline that:

1. **Ingests** project docs into Redis as vector embeddings
2. **Retrieves** relevant chunks when you ask a question
3. **Augments** the LLM prompt with that context
4. **Generates** a grounded, accurate answer

---

## Key Concept: The RAG Pipeline

```text
Documents → Chunking → Embedding → Vector Store
                                        ↓
User Query → Embed Query → Similarity Search
                                        ↓
                            Relevant Chunks + Query → LLM → Answer
```

---

## Key Concept: Document Ingestion

```java
@EventListener(ApplicationReadyEvent.class)
public void ingestDocuments() throws IOException {
    Resource[] resources = resolver.getResources(docsPath + "/**/*");

    List<Document> allDocuments = new ArrayList<>();
    for (Resource resource : resources) {
        if (resource.isReadable()) {
            allDocuments.addAll(
                new TikaDocumentReader(resource).get());
        }
    }

    List<Document> chunks = buildSplitter().apply(allDocuments);
    vectorStore.add(chunks);
}
```

Tika reads any format. The splitter creates overlapping chunks.

---

## Key Concept: Token Text Splitter

```java [78:]
    private TokenTextSplitter buildSplitter() {
      return TokenTextSplitter.builder()
              .withChunkSize(800)
              .withMinChunkSizeChars(200)
              .withMinChunkLengthToEmbed(5)
              .withMaxNumChunks(10000)
              .withKeepSeparator(true)
              .build();
    }
```

- **Chunk size**: tokens per chunk (not characters)
- **Overlap**: keeps context across chunk boundaries
- Too small = lost context. Too large = diluted relevance.

---

## Key Concept: Vector Store (Redis)

```java [24:]
    @Bean
    ChatClient ragChatClient(ChatClient.Builder builder, VectorStore vectorStore) {
      return builder
              .defaultSystem("""
                      You are a documentation assistant for a Java development team.
                      Answer questions using ONLY the provided context from our internal docs.
                      If the context does not contain the answer, say so clearly.
                      Always cite which document the information came from.
                      """)
              .defaultAdvisors(
                      QuestionAnswerAdvisor.builder(vectorStore)
                              .searchRequest(SearchRequest.builder()
                                      .similarityThreshold(0.7)
                                      .topK(5)
                                      .build())
                              .build())
              .build();
    }
```

Redis Stack includes RediSearch with native vector similarity.

---

## Key Concept: QuestionAnswerAdvisor

The magic — RAG as composable middleware:

```java [24:]
    @Bean
    ChatClient ragChatClient(ChatClient.Builder builder, VectorStore vectorStore) {
    return builder
          .defaultSystem("""
                  You are a documentation assistant for a Java development team.
                  Answer questions using ONLY the provided context from our internal docs.
                  If the context does not contain the answer, say so clearly.
                  Always cite which document the information came from.
                  """)
          .defaultAdvisors(
                  QuestionAnswerAdvisor.builder(vectorStore)
                          .searchRequest(SearchRequest.builder()
                                  .similarityThreshold(0.7)
                                  .topK(5)
                                  .build())
                          .build())
          .build();
    }
```

The advisor **intercepts** every prompt, queries the vector store, and injects context — automatically.

---

## Lab 2 — The Controller

```java [20:]
@RestController
public class DocAgentController {

    private final ChatClient ragChatClient;

    public DocAgentController(ChatClient ragChatClient) {
        this.ragChatClient = ragChatClient;
    }

    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        return ragChatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
```

Looks identical to Lab 1 — the RAG is invisible at this layer.

---

## Lab 2 — Hands-On

```bash
cd labs/lab-2-rag-agent
./mvnw spring-boot:run
```

Test it:

```bash
curl "http://localhost:8082/ask?question=What+are+the+testing+guidelines"

http :8082/ask question=="What are the testing guidelines?"
```

The response cites your actual documentation — not hallucinated generalities.

- Add another document to the classpath and restart the app.
- Ask questions about the new document

---

## Lab 2 — Exercise: Add Runtime Ingestion

Add a `POST /ingest` endpoint to `DocAgentController`:

```java
@PostMapping("/ingest")
public ResponseEntity<String> ingest(@RequestParam String url) {
    try {
        int chunks = ingestionService.ingestUrl(url);
        return ResponseEntity.ok(
            "Ingested '%s' → %d chunks stored."
            .formatted(url, chunks));
    } catch (MalformedURLException e) {
        return ResponseEntity.badRequest()
            .body("Invalid URL: " + e.getMessage());
    }
}
```

---

## Lab 2 — Exercise: Add Runtime Ingestion

Add a ingestUrl method to `DocAgentController`:

```java
    public int ingestUrl(String url) throws MalformedURLException {
        log.info("Ingesting URL: {}", url);

        var resource = new UrlResource(url);
        List<Document> documents = new TikaDocumentReader(resource).get();

        if (documents.isEmpty()) {
            log.warn("No content extracted from URL: {}", url);
            return 0;
        }

        List<Document> chunks = ingestAndStore(documents);
        log.info("Stored {} chunks from URL: {}", chunks.size(), url);
        return chunks.size();
    }
```
Then ingest a live URL and ask questions about it.

```bash
http POST :8082/ingest url=="http://spring.io"

http :8082/ask question=="What can Spring do?"
```

---

## Lab 2 — Validate

Run the integration test:

```bash
./mvnw test
```

The test verifies:
- The application context loads with Redis and Ollama
- The `/ask` endpoint returns a non-empty, grounded response

---

## Lab 2 — Key Takeaways

- RAG grounds LLMs in your actual data — no hallucination
- `QuestionAnswerAdvisor` makes RAG a single line of config
- Redis Stack provides vector similarity out of the box
- The controller code is identical to Lab 1 — RAG is invisible

---

<!-- .slide: data-background="#ea4335" data-background-transition="zoom" -->

# Lab 3

## Agentic Workflows with Embabel

*"Stop scripting workflows. Start declaring goals."*

Notes:
- **Timing**: 60 minutes
- **Key message**: "Stop scripting workflows. Start declaring goals."
- **Common issues**:
  - Planner timeout (increase `embabel.agent.planning.timeout-seconds`)
  - Ensure Embabel beans are properly configured
- **Demo script**: Watch console logs for GOAP planner discovering plan
- **Exercise tip**: Add SecurityScan action and watch planner adapt automatically
- **Aha moment**: Planner automatically incorporates new action without workflow code changes

---

## The Problem with Hardcoded Workflows

Traditional approach:

```java
// Step 1: analyze
CodeAnalysis analysis = analyzeCode(request);
// Step 2: write report
ReviewReport report = writeReport(analysis);
// Step 3: if you add a new step... rewrite everything
```

Adding a step means rewriting the orchestration.

---

## Lab 3 — What You'll Build

A **code review agent** using Embabel's GOAP planner.

You define **what the agent can do** and **what it should achieve**.

The planner discovers the path automatically.

---

## Key Concept: Embabel's Annotation Model

```text
@Agent    → "I am an agent"
@Action   → "I can do this"
@AchievesGoal → "This is the finish line"
```

Domain objects (Java records) are the glue — the planner connects actions by matching output types to input types.

---

## Key Concept: GOAP Planning

**Goal-Oriented Action Planning**:

```text
CodeReviewRequest
    → [analyzeCode]  → CodeAnalysis
    → [writeReport]  → ReviewReport  ← GOAL
```

The planner sees:
- `analyzeCode` takes `CodeReviewRequest`, returns `CodeAnalysis`
- `writeReport` takes `CodeAnalysis`, returns `ReviewReport`
- Goal is `ReviewReport`

It connects them automatically. No orchestration code.

---

## The Domain Model

Strongly-typed records flow between actions:

```java
public record CodeReviewRequest(
        String sourceCode,
        String language,
        String context
) {}
```
```java
public record CodeAnalysis(
        String summary,
        List<String> issues,
        List<String> suggestions,
        int qualityScore
) {
  public CodeAnalysis {
    issues = Objects.requireNonNullElse(issues, List.of());
    suggestions = Objects.requireNonNullElse(suggestions, List.of());
  }
}
```
```java
public record ReviewReport(
        String title,
        String body,
        CodeAnalysisVerdict verdict
) {}
```
```java
public enum CodeAnalysisVerdict {
    APPROVE,
    REQUEST_CHANGES,
    NEEDS_DISCUSSION
}
```

Types are the API contract between actions.

---

## The Agent

```java
@Agent(description = "Reviews Java source code and produces a structured report")
public class CodeReviewAgent {
    
    /* ... */
    
}
```
---

## Actions

```java []
    @AchievesGoal(description = "Produce a human-readable code review report")
    @Action(description = "Compile analysis into a developer-friendly review report")
    public ReviewReport writeReport(CodeAnalysis analysis, Ai ai) {
      return ai.withDefaultLlm().createObject("""
              You are a senior Java developer writing a code review.
              Based on the following analysis, write a clear, actionable report.
              
              Quality Score: %d/100
              Summary: %s
              Issues found: %s
              Suggestions: %s
              
              """.formatted(
              analysis.qualityScore(),
              analysis.summary(),
              String.join(", ", analysis.issues()),
              String.join(", ", analysis.suggestions())
      ), ReviewReport.class);
    }
```

The `writeReport` `Action` also `AchievesGoal` (GOAP)

---

## Invoking the Agent

```java
@RestController
public class AgentConfig {

    private final AgentPlatform agentPlatform;

    /* ... */
    
    @PostMapping("/review")
    public ReviewReport reviewCode(@RequestBody CodeReviewRequest request) {
      return new AgentPlatformTypedOps(agentPlatform)
              .transform(request, ReviewReport.class, ProcessOptions.DEFAULT);
    }
}
```

Give input + goal type. The platform handles everything.

---

## Lab 3 — Hands-On

```bash
cd labs/lab-3-agentic-workflows
./mvnw spring-boot:run
```

```bash
curl -X POST http://localhost:8083/review \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCode": "public class UserService {\n  public User findById(String id) {\n    for (User u : users) {\n      if (u.getId() == id) return u;\n    }\n    return null;\n  }\n}",
    "language": "Java",
    "context": "Service layer in a Spring Boot REST API"
  }'
```
The response should be a valid ReviewReport as JSON!

Look at the Lab 3 logs that were generated.

---

## Lab 3 — What the Agent Finds

The buggy code has two issues:

1. **String identity comparison** (`==` instead of `.equals()`)
2. **Returning `null`** instead of `Optional<User>`

Watch the console logs — you'll see the GOAP planner discover and execute the action sequence.

---

## Lab 3 — Exercise: Add a SecurityScan Action

Add a new action — the planner adapts automatically.

**Step 1:** Create `SecurityReport.java` in the `domain` package:

```java
public record SecurityReport(
        String summary,
        List<String> vulnerabilities,
        SecurityRiskLevel riskLevel
) {
    public SecurityReport {
        if (vulnerabilities == null)
            vulnerabilities = List.of();
    }
}
```

---

## Lab 3 — Exercise: SecurityScan (cont.)

**Step 2:** Add a `scanForVulnerabilities` action in `CodeReviewAgent`:

```java
@Action(description = "Scan code for security vulnerabilities")
public SecurityReport scanForVulnerabilities(CodeReviewRequest request, Ai ai) {
    return ai.withDefaultLlm().createObject("""
        Scan this %s code for security vulnerabilities.
        Look for: injection, auth issues, data exposure,
        unsafe deserialization, hardcoded secrets.
        Context: %s
        Code:
        ```
        %s
        ```
        """.formatted(request.language(), request.context(),
            request.sourceCode()),
        SecurityReport.class);
}
```

---

## Lab 3 — Exercise: SecurityScan (cont.)

**Step 3:** Update `writeReport` to accept `SecurityReport`:

```java
@AchievesGoal(description = "Produce a review report")
@Action(description = "Compile analysis into a report")
public ReviewReport writeReport(
        CodeAnalysis analysis, SecurityReport security,
        Ai ai) {
    return ai.withDefaultLlm().createObject("""
            You are a senior Java developer writing a code review.
            Based on the following analysis, write a clear, actionable report combining these findings:
            
            Quality Score: %d/100
            Summary: %s
            Issues found: %s
            Suggestions: %s
            Security risk: %s — %s
            Vulnerabilities: %s

            Respond with ONLY the JSON object on a single line. No markdown, no explanation, no newlines inside the JSON.
            
            {"title":"...","body":"...","verdict":"APPROVE|REQUEST_CHANGES|NEEDS_DISCUSSION"}
            """.formatted(analysis.qualityScore(),
            analysis.summary(),
            String.join(", ", analysis.issues()),
            String.join(", ", analysis.suggestions()),
            security.riskLevel(), security.summary(),
            String.join(", ", security.vulnerabilities())),
    ReviewReport.class);
}
```

---

## Lab 3 — Exercise: The New Flow

The planner now discovers a **three-step** plan automatically:

```text
CodeReviewRequest
    → [analyzeCode]             → CodeAnalysis
    → [scanForVulnerabilities]  → SecurityReport
    → [writeReport]             → ReviewReport  ← GOAL
```

`writeReport` needs both `CodeAnalysis` **and** `SecurityReport`, so the planner runs both scans before the report.

**No orchestration code changed. Just new types and actions.**

---

## Lab 3 — Validate

Run the integration test:

```bash
./mvnw test
```

The test:
- POSTs buggy code to `/review`
- Verifies the response has `title`, `body`, `verdict`
- Confirms the verdict is NOT "APPROVE" (the bugs should be caught)

---

## Lab 3 — Key Takeaways

- Embabel's GOAP planner connects actions by type signatures
- Adding capabilities doesn't require rewriting workflows
- Each action is independently testable with `FakeOperationContext`
- Domain objects (records) are the contract between actions
- Created by Rod Johnson (yes, the Spring Framework creator)

---

<!-- .slide: data-background="#9c27b0" data-background-transition="zoom" -->

# Lab 4

## Developer Sidekick: The Grand Finale

*"Your agent just wrote code to your project."*

Notes:
- **Timing**: 60 minutes
- **Key message**: "Your agent just wrote code to your project. In your IDE. Right now."
- **The Grand Finale Demo** (critical!):
  ```
  curl -X POST http://localhost:8084/task \
    -H "Content-Type: application/json" \
    -d '{"description":"Add /health endpoint...", "projectPath":"sample-project", "type":"GENERATE_CODE"}'
  ```
- **Demo script**: Watch agent scan project, read pom.xml, generate HealthController, write to disk
- **Save energy for this demo** — it's your closer
- **Aha moment**: File appears in attendee's IDE, compiles, follows their conventions
- **Exercise tip**: Ask agent to add tests for new endpoint, connect additional MCP servers
- **Common issues**: Ensure npx is on PATH for MCP servers

---

## Lab 4 — What You'll Build

Everything comes together:

- **Lab 1** — LLM interaction via Spring AI
- **Lab 2** — RAG for documentation context
- **Lab 3** — Embabel's GOAP planning
- **Lab 4** — MCP tools for filesystem access

A full-stack developer assistant.

Notes:
- This combines everything from previous labs
- No new infrastructure needed
- Each piece is something you built and understand from prior labs
- The agent reads your project, understands conventions, generates code, writes to disk
- The demo in this section (the curl command) is the "showstopper" moment
- File appears in IDE → compiles → works → "Whoa!"

---

## Key Concept: MCP (Model Context Protocol)

MCP lets LLMs call external tools via a standard protocol:

```yaml
spring:
  ai:
    mcp:
      client:
        stdio:
          servers:
            filesystem:
              command: npx
              args:
                - "-y"
                - "@modelcontextprotocol/server-filesystem"
                - "./workspace"
```

The filesystem MCP server gives the agent read/write access — sandboxed to `./workspace`.

Spring AI is also a great choice for creating your own MCP Servers

---

## Key Concept: Spring AI @Tool

Native Java function calling:

```java
@Component
public class FileSystemTools {

    @Tool(description = "List all files in a project directory")
    public String listProjectFiles(String relativePath)
            throws IOException {
        Path dir = Path.of(workspacePath)
                       .resolve(relativePath).normalize();
        validatePath(dir);  // Security: stay in sandbox
        try (var stream = Files.walk(dir, 5)) {
            return stream.filter(Files::isRegularFile)
                .map(p -> dir.relativize(p).toString())
                .collect(Collectors.joining("\n"));
        }
    }
}
```

All paths validated to stay within the workspace.

---

## The Sidekick's Plan

Embabel discovers this automatically:

```text
TaskRequest
    → [gatherProjectContext]  → ProjectContext
    → [generateCode]          → GeneratedCode
    → [applyAndVerify]        → TaskResult  ← GOAL
```

Three actions, three domain objects, zero orchestration.

---

## Action 1: Gather Project Context

```java
@Action(description = "Scan project filesystem")
public ProjectContext gatherProjectContext(
        TaskRequest request, Ai ai) throws IOException {
    String fileList = fileSystemTools
        .listProjectFiles(request.projectPath());
    String buildFile = fileSystemTools
        .readFile(request.projectPath() + "/pom.xml");

    return ai.withDefaultLlm().createObject("""
        Analyze this Java project and extract context.
        File listing: %s
        Build file: %s
        """.formatted(fileList, buildFile),
        ProjectContext.class);
}
```

---

## Action 2: Generate Code

```java
@Action(description = "Generate Java code following conventions")
public GeneratedCode generateCode(
        ProjectContext context, TaskRequest request, Ai ai)
        throws IOException {

    // Read existing source files to learn conventions
    StringBuilder existingCode = new StringBuilder();
    for (var entry : context.relevantFiles().entrySet()) {
        String content = fileSystemTools.readFile(
            request.projectPath() + "/" + entry.getKey());
        existingCode.append(content);
    }

    return ai.withDefaultLlm().createObject("""
        Generate code for: %s
        Following these conventions: %s
        """.formatted(request.description(), existingCode),
        GeneratedCode.class);
}
```

---

## Action 3: Apply and Verify

```java
@AchievesGoal(description = "Complete a developer task")
@Action(description = "Write generated code to filesystem")
public TaskResult applyAndVerify(
        GeneratedCode code, TaskRequest request)
        throws IOException {

    fileSystemTools.writeFile(
        request.projectPath() + "/" + code.filename(),
        code.content());

    return new TaskResult(
        code.explanation(),
        List.of(),
        List.of(code.filename()),
        "File written. Run mvn compile to verify."
    );
}
```

**The file appears in your IDE.**

---

## Lab 4 — Hands-On

```bash
cd labs/lab-4-developer-sidekick
./mvnw spring-boot:run
```

---

## Lab 4 — The Demo

```bash
curl -X POST http://localhost:8084/task \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Add a /health endpoint that returns JVM memory usage, active thread count, and uptime as a JSON response",
    "projectPath": "sample-project",
    "type": "GENERATE_CODE"
  }'
```

Watch the agent:
1. Scan `sample-project/` via filesystem tools
2. Read `pom.xml` and `GreetingController.java`
3. Generate a `HealthController.java` matching your conventions
4. Write it to disk

---

## Lab 4 — Validate

Run the integration test:

```bash
./mvnw test
```

The test verifies:
- The `FileSystemTools` can list, read, and write files
- Path sandboxing prevents escaping the workspace
- The application context loads with all components wired

---

## Lab 4 — Key Takeaways

- MCP provides a standard protocol for LLM tool access
- `@Tool` annotations make any Java method callable by the LLM
- Path sandboxing is critical — always validate filesystem access
- The full agent combines RAG + GOAP + Tools seamlessly
- Each piece is something you built and understand from prior labs

---

<!-- .slide: data-background="#333" data-background-transition="zoom" -->

# Recap

## What You Built Today

Notes:
- **Timing**: This section wraps up the workshop (~10 minutes)
- Review the journey: ChatClient → RAG → GOAP → Tools
- The demo that sold the workshop: Lab 4's curl command that writes code to the project
- Attendees take home: repo, understanding, RAG pipeline, GOAP experience
- Next steps: Connect more MCP servers, build custom agents, explore A2A protocol

---

## The Journey

| Lab | Concept | Superpower                  |
|-----|---------|-----------------------------|
| 1 | ChatClient | Talk to an LLM              |
| 2 | RAG | Ground it in your data      |
| 3 | GOAP Agents | Plan multi-step workflows   |
| 4 | Tools + MCP | Read and write your project |

Each lab was a building block. Lab 4 assembled them all.

Notes:
- Lab 1: Hello LLM (30 min)
- Lab 2: RAG Agent (45 min)
- Lab 3: Embabel Agents (60 min)
- Lab 4: Developer Sidekick (60 min)
- Total: 3-4 hours with breaks
- Each lab built on the last — Lab 4 assembled everything
- Encourage attendees to continue experimenting after the workshop

---

## Common Issues

| Problem | Fix |
|-----|---|
| Ollama slow on first call | Model loading, wait 30s, retry |
| Redis connection refused | Check `docker compose ps` |
| Embabel planner timeout | Increase timeout setting |
| MCP server not found | Ensure `npx` is on PATH |

Notes:
- Common issues participants might encounter:
  - **Ollama slow on first call**: Model loads into memory, wait 30s, retry
  - **Redis connection refused**: Check `docker compose ps`, ensure container healthy
  - **Embabel planner timeout**: Increase `embabel.agent.planning.timeout-seconds` in config
  - **MCP server not found**: Ensure `npx` on PATH, or install `@modelcontextprotocol/server-filesystem` globally
- These are shown in slides or provided as handouts
- Encourage attendees to troubleshoot — it's part of learning

---

## What You Take Home

1. A working GitHub repo you can extend
2. Understanding of Spring AI's `ChatClient` abstraction
3. A complete RAG pipeline for your own docs
4. Hands-on experience with Embabel's GOAP model
5. Knowledge of MCP tool integration

**AI-assisted development is a Java-first experience.**

Notes:
- What attendees actually take home:
  1. Working GitHub repo (labs/) they can extend
  2. Understanding of Spring AI's ChatClient abstraction
  3. Complete RAG pipeline for their own docs
  4. Hands-on experience with Embabel's GOAP model
  5. Knowledge of MCP tool integration (filesystem, external APIs)
- The "java-first" message is important — Java developers are first-class in AI now
- This is not a Python-only club — Java has first-class LLM support via Spring AI

---

## Next Steps

- Try other models, on your own infrastructure
- Connect more MCP servers (Git, database, Kubernetes)
- Build custom Embabel agents for your team's workflows
- Explore the A2A protocol for multi-agent systems
- Deploy the sidekick as a team service behind Spring Security

Notes:
- **Technology deep dive notes for facilitators**:
  - **Why Embabel?**: Rod Johnson (Spring Framework creator) built it with strong typing, testability, concurrency over config. Adding capabilities doesn't require rewriting workflows — planner adapts automatically.
  - **Why JavaClaw-style Skills?**: Runtime-extensible via Markdown. Domain experts can extend agent behavior without Java code.
  - **Why Local LLMs?**: No API costs, no data leakage, deterministic across attendees, works offline. qwen2.5-coder:1.5b runs on 16GB RAM.
- **Facilitator timing**:
  - Welcome + infra: 15 min
  - Lab 1: 30 min
  - Break: 10 min
  - Lab 2: 45 min
  - Break: 10 min
  - Lab 3: 60 min
  - Break: 10 min
  - Lab 4: 60 min (the demo!)
- **Tips**: Pre-pull Docker images, Wi-Fi-free fallback, pair attendees, keep solutions sealed, save energy for Lab 4 demo
- **Common issues**: Ollama slow (30s wait), Redis connection (check compose ps), planner timeout (increase config), MCP not found (npx on PATH)

---

<!-- .slide: data-background="#34a853" data-background-transition="zoom" -->

# Thank You!

### Questions?
