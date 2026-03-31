package com.workshop.lab4.config;

import com.embabel.agent.api.common.AgentPlatformTypedOps;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
import com.workshop.lab4.domain.TaskRequest;
import com.workshop.lab4.domain.TaskResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lab 4 — Unified configuration and endpoints.
 *
 * <p>This wires together RAG (from Lab 2), Embabel agents (from Lab 3),
 * and MCP tools into a single developer assistant.</p>
 */
@Configuration
@RestController
public class SidekickConfig {

    private final AgentPlatform agentPlatform;
    private final ChatClient ragClient;

    public SidekickConfig(AgentPlatform agentPlatform, ChatClient.Builder builder, VectorStore vectorStore) {
        this.agentPlatform = agentPlatform;
        this.ragClient = builder
                .defaultSystem("""
                    You are a developer documentation assistant.
                    Answer using ONLY the provided context from project docs.
                    If unsure, say so and suggest where to look.
                    """)
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder()
                                        .similarityThreshold(0.7)
                                        .topK(5)
                                        .build())
                                .build()
                )
                .build();
    }

    /**
     * RAG-powered documentation chat — carries forward from Lab 2.
     */
    @Bean
    ChatClient ragClient() {
        return this.ragClient;
    }

    /**
     * Ask questions against ingested documentation.
     */
    @GetMapping("/docs")
    public String askDocs(@RequestParam String question) {
        return ragClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * Execute a developer task — the full sidekick pipeline.
     *
     * <pre>
     * curl -X POST http://localhost:8084/task \
     *   -H "Content-Type: application/json" \
     *   -d '{
     *     "description": "Add a /health endpoint that returns JVM memory stats",
     *     "projectPath": "sample-project",
     *     "type": "GENERATE_CODE"
     *   }'
     * </pre>
     */
    @PostMapping("/task")
    public TaskResult executeTask(@RequestBody TaskRequest request) {
        return new AgentPlatformTypedOps(agentPlatform)
                .transform(request, TaskResult.class, ProcessOptions.DEFAULT);
    }
}
