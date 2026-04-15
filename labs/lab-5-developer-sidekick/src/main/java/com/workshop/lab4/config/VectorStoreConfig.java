package com.workshop.lab4.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

/**
 * Explicit Redis vector store wiring — same pattern as Lab 2.
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    RedisVectorStore vectorStore(EmbeddingModel embeddingModel,
                                 @Value("${spring.data.redis.host:localhost}") String host,
                                 @Value("${spring.data.redis.port:6379}") int port,
                                 @Value("${spring.ai.vectorstore.redis.index-name:sidekick-knowledge}") String indexName,
                                 @Value("${spring.ai.vectorstore.redis.initialize-schema:true}") boolean initializeSchema) {

        var jedis = new JedisPooled(host, port);

        return RedisVectorStore.builder(jedis, embeddingModel)
                .indexName(indexName)
                .initializeSchema(initializeSchema)
                .build();
    }
}
