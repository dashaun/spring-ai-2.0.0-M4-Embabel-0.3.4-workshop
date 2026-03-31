package com.workshop.lab2.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lab 2 — Document ingestion pipeline.
 *
 * <p>On startup, reads every document from {@code classpath:docs/},
 * splits them into chunks, generates embeddings via Ollama's
 * {@code nomic-embed-text}, and stores them in Redis Stack.</p>
 *
 * <h2>Exercises</h2>
 * <ol>
 *   <li>Add your own Markdown files to {@code src/main/resources/docs/}.</li>
 *   <li>Experiment with different chunk sizes and overlap settings.</li>
 *   <li>Open RedisInsight (<a href="http://localhost:8001">http://localhost:8001</a>) and inspect the stored vectors.</li>
 * </ol>
 */
@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final VectorStore vectorStore;

    @Value("${workshop.docs.path}")
    private String docsPath;

    public DocumentIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ingestDocuments() throws IOException {
        log.info("Scanning for documents at: {}", docsPath);

        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(docsPath + "/**/*");

        List<Document> allDocuments = new ArrayList<>();
        for (Resource resource : resources) {
            if (resource.isReadable()) {
                log.info("  Reading: {}", resource.getFilename());
                allDocuments.addAll(new TikaDocumentReader(resource).get());
            }
        }

        if (allDocuments.isEmpty()) {
            log.warn("No documents found. Add files to src/main/resources/docs/");
            return;
        }

        List<Document> results = ingestAndStore(allDocuments);
        log.info("Ingestion complete with {} documents", results.size());
    }

    private List<Document> ingestAndStore(List<Document> documents) {
        List<Document> chunks = buildSplitter().apply(documents);
        vectorStore.add(chunks);
        return chunks;
    }

    private TokenTextSplitter buildSplitter() {
        return TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(200)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();
    }
}
