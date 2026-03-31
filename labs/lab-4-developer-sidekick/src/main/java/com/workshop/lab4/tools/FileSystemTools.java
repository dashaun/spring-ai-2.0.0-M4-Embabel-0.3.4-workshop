package com.workshop.lab4.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Lab 4 — Spring AI Tool functions for local filesystem access.
 *
 * <p>These are <strong>Spring AI native tools</strong>, registered via
 * {@code @Tool}. The LLM can call them through function calling.
 * They complement the MCP filesystem server configured in
 * {@code application.yml} — giving you two approaches to tool use.</p>
 *
 * <p><strong>Security note:</strong> All operations are sandboxed to
 * the configured workspace path.</p>
 */
@Component
public class FileSystemTools {

    @Value("${workshop.workspace.path:./workspace}")
    private String workspacePath;

    @Tool(description = "List all files in a project directory, returning their relative paths")
    public String listProjectFiles(
            @ToolParam(description = "Relative path within the workspace") String relativePath
    ) throws IOException {
        Path dir = Path.of(workspacePath).resolve(relativePath).normalize();
        validatePath(dir);

        try (var stream = Files.walk(dir, 10)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> dir.relativize(p).toString())
                    .collect(Collectors.joining("\n"));
        }
    }

    @Tool(description = "Read the contents of a source file in the project")
    public String readFile(
            @ToolParam(description = "Relative file path within the workspace") String relativePath
    ) throws IOException {
        Path file = Path.of(workspacePath).resolve(relativePath).normalize();
        validatePath(file);
        return Files.readString(file);
    }

    @Tool(description = "Write content to a file in the project workspace")
    public String writeFile(
            @ToolParam(description = "Relative file path") String relativePath,
            @ToolParam(description = "File content to write") String content
    ) throws IOException {
        Path file = Path.of(workspacePath).resolve(relativePath).normalize();
        validatePath(file);
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
        return "Written %d bytes to %s".formatted(content.length(), relativePath);
    }

    private void validatePath(Path path) {
        Path workspace = Path.of(workspacePath).toAbsolutePath().normalize();
        if (!path.toAbsolutePath().normalize().startsWith(workspace)) {
            throw new SecurityException("Access denied: path escapes workspace sandbox");
        }
    }
}
