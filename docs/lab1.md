<!-- .slide: data-background-color="#6db33f" -->

# Lab 1

## Hello, Local LLM

*"It's just a Spring bean."*

Notes:
- Timing: 30 minutes
- Key message: "It's just a Spring bean" — LLM integration is dependency injection
- Common issue: Ollama slow on first call (wait 30s, retry)
- Aha moment: LLM integration feels like any other Spring service

---

## Validate First

Confirm the lab is ready before you start:

```bash
cd labs/lab-1-hello-llm
./mvnw test
```

The test verifies:
- The application context loads with Ollama configured
- The `/chat` endpoint returns a non-empty response

**All tests green? You're ready.**

Notes:
- If tests fail, check that Ollama is running and the model is pulled
- curl http://localhost:11434/api/tags should list qwen2.5-coder:1.5b

---

## What You'll Build

A Spring Boot REST API that talks to a local LLM

- One dependency, one bean, one endpoint
- No API keys, no cloud — just Ollama on your laptop
- Spring AI's `ChatClient` abstracts the LLM provider

---

## Key Concept: ChatClient

Spring AI's universal LLM abstraction

```java
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

```java
.defaultSystem("""
    You are a helpful Java developer assistant.
    You give concise, accurate answers with code examples when appropriate.
    Always use modern Java idioms (records, pattern matching, virtual threads).
    """)
```

The system prompt is sent with every request — the user never sees it.

---

## Key Concept: Calling the LLM

```java
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

## Configuration

```yaml
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

## Hands-On

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

## Exercise 1: Modify the System Prompt

**Make the assistant respond as a pirate.**

Change the `defaultSystem` in `ChatController.java`:

```java
.defaultSystem("""
    You are a helpful Java developer assistant.
    You give concise, accurate answers with code examples when appropriate.
    Always use modern Java idioms (records, pattern matching, virtual threads).
    Respond in the style of a pirate!
    """)
```

Restart, then test:

```bash
curl "http://localhost:8080/chat"
```

---

## Exercise 2: Add an `/explain` Endpoint

Add a focused prompt template to `ChatController`:

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

Note: `.system()` on the prompt **overrides** the default system prompt.

---

## Exercise 2: Test It

```bash
http :8080/explain
http :8080/chat message=="What is new in Java 26"
http :8080/chat message=="What is your data cutoff date?"
```

Note how the model doesn't know recent events — it has a cutoff date.

---

## Key Takeaways

- Spring AI makes LLM integration feel like any Spring service
- `ChatClient.Builder` is auto-configured by Spring Boot
- System prompts control personality without changing code
- Ollama runs everything locally — no API keys needed
- Models have a cutoff date — they don't know recent events

Notes:
- ChatClient.Builder is auto-configured
- System prompts control personality without code changes
- Ollama runs everything locally
