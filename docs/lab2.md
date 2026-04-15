<!-- .slide: data-background-color="#191e1e" -->

# Lab 2

## Context-Aware Documentation Agent

*"Your LLM now knows your codebase."*

Notes:
- Timing: 45 minutes
- Key message: "Your LLM now knows your codebase"
- Common issues: Vector store not populated (wait for ApplicationReadyEvent), Redis connection refused
- Demo tip: Show RedisInsight UI at http://localhost:8001
- Aha moment: LLM responds with specific detail from YOUR docs, not hallucinated generalities

---

## Validate First

Confirm the lab is ready before you start:

```bash
cd labs/lab-2-rag-agent
./mvnw test
```

The test verifies:
- The application context loads with Redis and Ollama
- The `/ask` endpoint returns a non-empty, grounded response

**All tests green? You're ready.**

Notes:
- If tests fail, check that Redis is running: redis-cli ping should return PONG
- Check that Ollama has both models: qwen2.5-coder:1.5b and nomic-embed-text

---

## The Problem with Vanilla LLMs

Ask an LLM: *"What's our team's convention for error handling?"*

It will **hallucinate** — it doesn't know your codebase.

**RAG** (Retrieval-Augmented Generation) fixes this by grounding the LLM in your actual documentation.

---

## What You'll Build

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

    List<Document> allDocuments = Arrays.stream(resources)
            .filter(Resource::isReadable)
            .flatMap(resource -> new TikaDocumentReader(resource).get().stream())
            .toList();

    List<Document> chunks = buildSplitter().apply(allDocuments);
    vectorStore.add(chunks);
}
```

Tika reads any format. The splitter creates overlapping chunks.

---

## Key Concept: Token Text Splitter

```java
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
- Too small = lost context. Too large = diluted relevance.

---

## Key Concept: QuestionAnswerAdvisor

RAG as composable middleware — a single line of config:

```java
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

## The Controller

```java
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

## Hands-On

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

- Add another document to `src/main/resources/docs/` and restart.
- Ask questions about the new document.

---

## Exercise: Add Runtime Ingestion

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

## Exercise: Add `ingestUrl` to the Service

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

Then ingest a live URL and ask questions about it:

```bash
http POST :8082/ingest url=="http://spring.io"
http :8082/ask question=="What can Spring do?"
```

---

## Key Takeaways

- RAG grounds LLMs in your actual data — no hallucination
- `QuestionAnswerAdvisor` makes RAG a single line of config
- Redis Stack provides vector similarity out of the box
- The controller code is identical to Lab 1 — RAG is invisible

Notes:
- Show RedisInsight UI at http://localhost:8001 to visualize stored embeddings
