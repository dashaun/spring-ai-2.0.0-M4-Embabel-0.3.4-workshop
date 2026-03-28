package com.workshop.lab2.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lab 2 — Ask questions against your ingested documentation.
 *
 * <p>The RAG pipeline is invisible at this layer — the {@code ChatClient}
 * has a {@code QuestionAnswerAdvisor} configured that automatically
 * retrieves relevant context before every call.</p>
 *
 * <h2>Try it</h2>
 * <pre>
 *   curl "http://localhost:8082/ask?question=What are the testing guidelines?"
 * </pre>
 */
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

    // =========================================================================
    // TODO (Exercise): Add a POST /ingest endpoint that accepts a URL,
    //   fetches the content, and adds it to the vector store at runtime.
    // =========================================================================
}
