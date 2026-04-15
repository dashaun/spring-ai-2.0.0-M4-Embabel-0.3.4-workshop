package com.workshop.lab4.tools;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lab 5 — Filesystem access via the Lab 3 MCP server.
 *
 * <p>In Lab 4, this class performed local {@code Files.*} operations.
 * Now it is a thin adapter over the MCP client — every call goes over
 * HTTP to the filesystem server built in Lab 3. The agent code is
 * unchanged; the tools are now networked services.</p>
 *
 * <p><strong>Prerequisite:</strong> Lab 3 must be running on port 8083
 * before starting Lab 5.</p>
 */
@Component
public class FileSystemTools {

    private final McpSyncClient mcpSyncClient;

    public FileSystemTools(McpSyncClient mcpSyncClient) {
        this.mcpSyncClient = mcpSyncClient;
    }

    /**
     * List all files under the given relative path in the Lab 3 workspace.
     */
    public String listProjectFiles(String relativePath) {
        return callTool("list_files", Map.of("path", relativePath));
    }

    /**
     * Read a file from the Lab 3 workspace.
     */
    public String readFile(String relativePath) {
        return callTool("read_file", Map.of("path", relativePath));
    }

    /**
     * Write content to a file in the Lab 3 workspace.
     */
    public String writeFile(String relativePath, String content) {
        return callTool("write_file", Map.of("path", relativePath, "content", content));
    }

    private String callTool(String toolName, Map<String, Object> arguments) {
        McpSchema.CallToolResult result = mcpSyncClient.callTool(
                new McpSchema.CallToolRequest(toolName, arguments));

        return result.content().stream()
                .filter(c -> c instanceof McpSchema.TextContent)
                .map(c -> ((McpSchema.TextContent) c).text())
                .collect(Collectors.joining("\n"));
    }
}
