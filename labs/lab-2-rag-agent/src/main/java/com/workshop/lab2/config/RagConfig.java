package com.workshop.lab2.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lab 2 — Wiring the RAG pipeline.
 *
 * <p>The {@link QuestionAnswerAdvisor} intercepts every user prompt,
 * queries the {@link VectorStore} for semantically similar document chunks,
 * and injects them into the prompt as context — all before the LLM sees it.</p>
 *
 * <h2>Key insight</h2>
 * <p>You don't write retrieval logic yourself. Spring AI's advisor pattern
 * composes retrieval + generation in a single, declarative pipeline.</p>
 */
@Configuration
public class RagConfig {

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
}
