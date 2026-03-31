package com.workshop.lab1;

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
 * Integration test for the Lab 1 chat endpoint.
 *
 * <p>Requires Ollama running locally at <a href="http://localhost:11434">http://localhost:11434</a> with the
 * {@code qwen2.5-coder:1.5b} model pulled ({@code ollama pull qwen2.5-coder:1.5b}).</p>
 */
@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void chatEndpoint_returnsNonEmptyResponse() throws Exception {
        MvcResult result = mockMvc.perform(
                        get("/chat").param("message", "What is a Java record? Answer in one sentence."))
                .andExpect(status().isOk())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).isNotBlank();

        System.out.println("\n=== Lab 1 Chat Response ===");
        System.out.println(body);
    }
}
