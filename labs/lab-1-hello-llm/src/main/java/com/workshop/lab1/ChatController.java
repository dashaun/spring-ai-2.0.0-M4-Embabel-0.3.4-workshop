package com.workshop.lab1;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lab 1 — Your first conversation with a local LLM.
 *
 * <h2>What you'll learn</h2>
 * <ul>
 *   <li>How Spring AI's {@link ChatClient} abstracts away the LLM provider.</li>
 *   <li>How to send a prompt and receive a response — just like calling any service.</li>
 *   <li>How system prompts shape the personality of your assistant.</li>
 * </ul>
 *
 * <h2>Exercises</h2>
 * <ol>
 *   <li>Start the app and hit {@code /chat?message=Hello}. Observe the response.</li>
 *   <li>Modify the system prompt to make the assistant respond as a pirate.</li>
 *   <li>Add a new endpoint {@code /explain} that asks the LLM to explain a Java concept.</li>
 * </ol>
 */
@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                    You are a helpful Java developer assistant.
                    You give concise, accurate answers with code examples when appropriate.
                    Always use modern Java idioms (records, pattern matching, virtual threads).
                    """)
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "What's new in Java 25?") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    // =========================================================================
    // TODO (Exercise): Implement the /explain endpoint
    // =========================================================================
    // @GetMapping("/explain")
    // public String explain(@RequestParam String concept) {
    //     // Your code here — use the chatClient to explain the given Java concept
    //     // Hint: try adding a more specific system prompt for explanations
    // }
}
