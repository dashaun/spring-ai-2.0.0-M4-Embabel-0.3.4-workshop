package com.workshop.lab2.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

/**
 * Lab 2 — Explicit Redis vector store wiring.
 *
 * <p>Creates a {@link RedisVectorStore} backed by a {@link JedisPooled}
 * connection and the Ollama embedding model. Making this explicit (rather
 * than relying on autoconfiguration) lets you see exactly how Spring AI
 * talks to Redis Stack's vector search engine.</p>
 *
 * <h2>Key insight</h2>
 * <p>Redis Stack bundles RediSearch, which supports vector similarity
 * indexes natively. The {@code initializeSchema(true)} call creates the
 * index on first startup — open <a href="http://localhost:8001">RedisInsight</a>
 * to inspect it.</p>
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    RedisVectorStore vectorStore(EmbeddingModel embeddingModel,
                                 @Value("${spring.data.redis.host:localhost}") String host,
                                 @Value("${spring.data.redis.port:6379}") int port,
                                 @Value("${spring.ai.vectorstore.redis.index-name:workshop-docs}") String indexName,
                                 @Value("${spring.ai.vectorstore.redis.initialize-schema:true}") boolean initializeSchema) {

        var jedis = new JedisPooled(host, port);

        return RedisVectorStore.builder(jedis, embeddingModel)
                .indexName(indexName)
                .initializeSchema(initializeSchema)
                .build();
    }
}
