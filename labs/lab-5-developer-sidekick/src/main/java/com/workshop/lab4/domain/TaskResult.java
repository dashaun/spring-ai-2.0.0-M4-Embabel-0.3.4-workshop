package com.workshop.lab4.domain;

import java.util.List;

/**
 * The final result of a developer task — the Goal.
 */
public record TaskResult(
        String summary,
        List<String> filesModified,
        List<String> filesCreated,
        String nextSteps
) {}
