<!-- .slide: data-background-color="#191e1e" -->

# Lab 5

## Developer Sidekick: The Grand Finale

*"Your agent just wrote code to your project."*

Notes:
- Timing: 60 minutes
- Key message: "Your agent just wrote code to your project. In your IDE. Right now."
- Save energy for this demo — it's your closer
- Aha moment: File appears in attendee's IDE, compiles, follows their conventions
- Prerequisite: Lab 3 must be running on port 8083

---

## Validate First

Confirm the lab is ready before you start:

```bash
# Lab 3 must be running first
cd labs/lab-3-mcp-filesystem-server && ./mvnw spring-boot:run &

cd labs/lab-5-developer-sidekick
./mvnw test
```

The test verifies:
- The `FileSystemTools` can call Lab 3's MCP server
- The application context loads with all components wired
- Redis and Ollama are reachable

**All tests green? You're ready.**

Notes:
- If tests fail, confirm Lab 3 is running: curl http://localhost:8083/sse
- Check Redis: redis-cli ping

---

## What You'll Build

Everything comes together:

- **Lab 1** — LLM interaction via Spring AI
- **Lab 2** — RAG for documentation context
- **Lab 3** — MCP filesystem server (tools over HTTP)
- **Lab 4** — Embabel's GOAP planning
- **Lab 5** — The full stack: GOAP + MCP client + RAG

A developer assistant that reads your project, understands your conventions, and writes code to disk — using the MCP server you built in Lab 3.

Notes:
- No new infrastructure needed — Lab 3's server is already running
- Each piece is something you built and understand from prior labs

---

## Key Concept: Connecting to Your MCP Server

Lab 5 connects to the Java MCP server you built in Lab 3 — over HTTP:

```yaml
spring:
  ai:
    mcp:
      client:
        sse:
          connections:
            filesystem:
              url: http://localhost:8083
```

Spring AI's MCP client auto-configures a `McpSyncClient` bean for each connection.
No `npx`. No Node.js. **Pure Java, all the way down.**

---

## Key Concept: FileSystemTools as MCP Adapter

`FileSystemTools` is now a thin adapter over `McpSyncClient`:

```java
@Component
public class FileSystemTools {

    private final McpSyncClient mcpSyncClient;

    public String listProjectFiles(String path) {
        return callTool("list_files", Map.of("path", path));
    }

    private String callTool(String toolName, Map<String, Object> args) {
        McpSchema.CallToolResult result =
            mcpSyncClient.callTool(new McpSchema.CallToolRequest(toolName, args));
        return result.content().stream()
            .filter(c -> c instanceof McpSchema.TextContent)
            .map(c -> ((McpSchema.TextContent) c).text())
            .collect(Collectors.joining("\n"));
    }
}
```

The agent code is unchanged. The tools are now remote services.

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
public ProjectContext gatherProjectContext(TaskRequest request, Ai ai) throws IOException {
    String fileList = fileSystemTools.listProjectFiles(request.projectPath());
    String buildFile = fileSystemTools.readFile(request.projectPath() + "/pom.xml");

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
public GeneratedCode generateCode(ProjectContext context, TaskRequest request, Ai ai)
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
public TaskResult applyAndVerify(GeneratedCode code, TaskRequest request) throws IOException {

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

## Hands-On

```bash
cd labs/lab-5-developer-sidekick
./mvnw spring-boot:run
```

---

## The Demo

```bash
curl -X POST http://localhost:8085/task \
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

Notes:
- This is the showstopper moment
- File appears in IDE → compiles → works → "Whoa!"
- Exercise tip: Ask agent to add tests for the new endpoint

---

## Key Takeaways

- MCP provides a standard protocol for LLM tool access
- `@Tool` annotations make any Java method callable by the LLM
- Path sandboxing is critical — always validate filesystem access
- The full agent combines RAG + GOAP + Tools seamlessly
- Each piece is something you built and understand from prior labs

Notes:
- Connect more MCP servers (Git, database, Kubernetes) as a next step
