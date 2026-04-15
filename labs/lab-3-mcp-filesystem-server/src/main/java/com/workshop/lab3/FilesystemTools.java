package com.workshop.lab3;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Lab 3 — Filesystem tools exposed as MCP tools.
 *
 * <p>Each {@code @Tool}-annotated method becomes a named tool in the MCP server.
 * Any MCP client (including Lab 5) can discover and call these tools over HTTP.</p>
 *
 * <p><strong>Security:</strong> All paths are validated against the workspace root.
 * No path traversal is possible — attempts to escape raise a {@link SecurityException}.</p>
 */
@Component
public class FilesystemTools {

    @Value("${workshop.workspace.path:./workspace}")
    private String workspacePath;

    @Tool(description = "List all files in the project workspace, returning relative paths")
    public String list_files(
            @ToolParam(description = "Relative path to list (use '.' for the workspace root)") String path
    ) {
        Path dir = resolve(path);
        try (var stream = Files.walk(dir, 10)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(p -> dir.relativize(p).toString())
                    .collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Tool(description = "Read the full contents of a file in the workspace")
    public String read_file(
            @ToolParam(description = "Relative file path within the workspace") String path
    ) {
        Path file = resolve(path);
        try {
            return Files.readString(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Tool(description = "Write content to a file in the workspace, creating directories as needed")
    public String write_file(
            @ToolParam(description = "Relative file path within the workspace") String path,
            @ToolParam(description = "Content to write to the file") String content
    ) {
        Path file = resolve(path);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, content);
            return "Written %d bytes to %s".formatted(content.length(), path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path resolve(String relativePath) {
        Path workspace = Path.of(workspacePath).toAbsolutePath().normalize();
        Path resolved = workspace.resolve(relativePath).normalize();
        if (!resolved.startsWith(workspace)) {
            throw new SecurityException("Access denied: path escapes workspace sandbox");
        }
        return resolved;
    }
}
