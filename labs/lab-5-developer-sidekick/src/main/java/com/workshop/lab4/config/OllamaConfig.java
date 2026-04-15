package com.workshop.lab4.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes Spring AI {@link ChatModel} and {@link EmbeddingModel} beans
 * backed by Ollama.
 *
 * <p>Embabel's Ollama starter registers models through its own
 * {@code ModelProvider} abstraction but does not create Spring AI beans.
 * This config bridges the gap so that {@code ChatClient.Builder} and
 * {@code VectorStore} auto-configuration can find the models they need.</p>
 */
@Configuration
public class OllamaConfig {

    @Bean
    OllamaApi ollamaApi(@Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl) {
        return OllamaApi.builder().baseUrl(baseUrl).build();
    }

    @Bean
    ChatModel chatModel(OllamaApi ollamaApi,
                          @Value("${spring.ai.ollama.chat.model:qwen2.5-coder:1.5b}") String model) {
        return OllamaChatModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaChatOptions.builder().model(model).build())
                .toolCallingManager(null)
                .build();
    }

    @Bean
    EmbeddingModel embeddingModel(OllamaApi ollamaApi,
                                   @Value("${spring.ai.ollama.embedding.model:nomic-embed-text}") String model) {
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaEmbeddingOptions.builder().model(model).build())
                .build();
    }
}
