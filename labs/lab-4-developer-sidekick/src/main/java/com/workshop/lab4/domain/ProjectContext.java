package com.workshop.lab4.domain;

import java.util.List;
import java.util.Map;

/**
 * Rich context about the project the agent is working in.
 * Gathered by scanning the filesystem and build files.
 */
public record ProjectContext(
        String projectName,
        String buildTool,
        String javaVersion,
        List<String> dependencies,
        Map<String, String> relevantFiles
) {}
