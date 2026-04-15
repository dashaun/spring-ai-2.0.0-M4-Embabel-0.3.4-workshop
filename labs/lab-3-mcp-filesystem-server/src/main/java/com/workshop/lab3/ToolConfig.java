package com.workshop.lab3;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lab 3 — Wire {@link FilesystemTools} into the MCP server.
 *
 * <p>Spring AI's MCP server auto-configuration picks up any
 * {@link ToolCallbackProvider} bean and advertises its tools
 * to connecting MCP clients over HTTP/SSE.</p>
 */
@Configuration
public class ToolConfig {

    @Bean
    public ToolCallbackProvider filesystemToolCallbackProvider(FilesystemTools filesystemTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(filesystemTools)
                .build();
    }
}
