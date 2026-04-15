<!-- .slide: data-background-color="#191e1e" -->

# Lab 3

## Build a Java MCP Server

*"Give the LLM tools to act — in pure Java."*

Notes:
- Timing: 45 minutes
- Key message: "No Node.js, no npx — just Spring Boot and @Tool"
- Common issues: Port conflict on 8083, workspace path not found
- Aha moment: The MCP server you build here is consumed by Lab 5

---

## Validate First

Confirm the lab is ready before you start:

```bash
cd labs/lab-3-mcp-filesystem-server
./mvnw test
```

The tests verify:
- `FilesystemTools` can list, read, and write files
- Path sandboxing blocks traversal attempts

**All tests green? You're ready.**

Notes:
- Tests are unit tests — no Ollama or Redis required
- If tests fail, check that the workspace directory exists

---

## The Story So Far

```text
Lab 1: ChatClient    → Talk to an LLM
Lab 2: RAG           → Ground it in your data
Lab 3: MCP Server    → Give it tools to act   ← You are here
```

The LLM can reason. It can retrieve. Now we give it **hands**.

---

## What is MCP?

**Model Context Protocol** is an open standard for connecting AI models to external tools and resources.

```text
MCP Client (Lab 5)  ←──HTTP/SSE──→  MCP Server (Lab 3)
     │                                      │
     │  "call list_files(.)"                │  Files.walk(workspace)
     └──────────────────────────────────────┘
```

- Any LLM framework can be an MCP client
- Any backend can be an MCP server
- Standard JSON protocol over HTTP

> In Lab 5, your Embabel agent will call the server you build today.

---

## Why Build It in Java?

The typical MCP filesystem server is installed via `npx`:

```bash
npx -y @modelcontextprotocol/server-filesystem ./workspace
```

This works — but it adds Node.js and npm as prerequisites.

**Spring AI 2.0.0-M4 includes a full MCP server starter:**

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

One dependency. Pure Java. No external runtimes.

---

## The Architecture

```text
Spring Boot App (port 8083)
    │
    ├── FilesystemTools.java  (@Tool methods)
    │       ├── list_files(path)
    │       ├── read_file(path)
    │       └── write_file(path, content)
    │
    └── ToolConfig.java  (ToolCallbackProvider → MCP)
            │
            └── SSE endpoint at /sse
                    ← Lab 5 connects here
```

The MCP server auto-configuration picks up any `ToolCallbackProvider` bean and exposes its tools over HTTP/SSE.

---

## The Tool Methods

```java
@Component
public class FilesystemTools {

    @Value("${workshop.workspace.path:./workspace}")
    private String workspacePath;

    @Tool(description = "List all files in the workspace")
    public String list_files(
            @ToolParam(description = "Relative path (use '.' for root)") String path) {
        Path dir = resolve(path);
        try (var stream = Files.walk(dir, 10)) {
            return stream.filter(Files::isRegularFile)
                    .map(p -> dir.relativize(p).toString())
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    // read_file, write_file follow the same pattern
}
```

The method name becomes the MCP tool name. The `@ToolParam` description is the argument schema.

---

## Wiring Tools to MCP

```java
@Configuration
public class ToolConfig {

    @Bean
    public ToolCallbackProvider filesystemToolCallbackProvider(FilesystemTools tools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(tools)
                .build();
    }
}
```

Spring AI's MCP server auto-configuration detects this bean and advertises the tools to connecting clients.

---

## Configuration

```yaml
spring:
  ai:
    mcp:
      server:
        name: filesystem-server
        version: 1.0.0

workshop:
  workspace:
    path: ./workspace

server:
  port: 8083
```

The workspace path is sandboxed — all `FilesystemTools` methods validate that resolved paths stay within it.

---

## Hands-On

```bash
cd labs/lab-3-mcp-filesystem-server
./mvnw spring-boot:run
```

Verify the server is advertising its tools:

```bash
# SSE endpoint — should return the MCP handshake
curl -N http://localhost:8083/sse
```

You should see the server stream event data including tool definitions for `list_files`, `read_file`, and `write_file`.

---

## Exercise: Add a `create_directory` Tool

Extend `FilesystemTools` with a new tool:

```java
@Tool(description = "Create a directory (and parent directories) in the workspace")
public String create_directory(
        @ToolParam(description = "Relative directory path to create") String path) {
    Path dir = resolve(path);
    try {
        Files.createDirectories(dir);
        return "Created directory: " + path;
    } catch (IOException e) { throw new UncheckedIOException(e); }
}
```

Restart the server and verify the new tool appears in the SSE stream.

**No ToolConfig changes needed** — `MethodToolCallbackProvider` picks it up automatically.

---

## Key Takeaways

- Spring AI's MCP server starter turns a Spring Boot app into an MCP server
- `@Tool`-annotated methods become named MCP tools automatically
- `ToolCallbackProvider` is the bridge between Java methods and the MCP protocol
- Path sandboxing is your security layer — always validate before I/O
- Lab 5 will connect to this server as an MCP client — no npx required

Notes:
- Keep this server running through Lab 5
- The workspace directory (./workspace/sample-project) is pre-populated
