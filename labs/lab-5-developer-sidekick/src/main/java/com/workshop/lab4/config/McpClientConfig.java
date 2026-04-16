package com.workshop.lab4.config;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Bridges the MCP client autoconfiguration (which produces a {@code List<McpSyncClient>})
 * with components that inject a single {@code McpSyncClient}.
 *
 * <p>Embabel's {@code QuiteMcpClientAutoConfiguration} creates a list of sync clients
 * from all configured SSE connections. This config extracts the first client (the
 * "filesystem" connection to Lab 3) so it can be injected directly.</p>
 */
@Configuration
public class McpClientConfig {

    @Bean
    McpSyncClient mcpSyncClient(List<McpSyncClient> mcpSyncClients) {
        if (mcpSyncClients.isEmpty()) {
            throw new IllegalStateException(
                    "No MCP clients available. Is Lab 3 running on port 8083? " +
                    "Start it with: cd labs/lab-3-mcp-filesystem-server && ./mvnw spring-boot:run");
        }
        return mcpSyncClients.getFirst();
    }
}
