package com.workshop.lab2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the Lab 2 RAG documentation agent.
 *
 * <p>Requires Ollama running locally at http://localhost:11434 with both
 * {@code qwen2.5-coder:1.5b} and {@code nomic-embed-text} models pulled, and
 * Redis Stack running on port 6379.</p>
 *
 * <p>Document ingestion is triggered on ApplicationReadyEvent, so by the
 * time this test runs the vector store should be populated.</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class DocAgentIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void askEndpoint_returnsGroundedResponse() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/ask").param("question", "What are the testing guidelines?"))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).isNotBlank();

        System.out.println("\n=== Lab 2 RAG Response ===");
        System.out.println(body);
    }
}
