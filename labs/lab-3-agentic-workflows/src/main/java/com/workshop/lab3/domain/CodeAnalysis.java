package com.workshop.lab3.domain;

import java.util.List;
import java.util.Objects;

/**
 * Structured output from the code analysis action.
 */
public record CodeAnalysis(
        String summary,
        List<String> issues,
        List<String> suggestions,
        int qualityScore
) {
    public CodeAnalysis {
        issues = Objects.requireNonNullElse(issues, List.of());
        suggestions = Objects.requireNonNullElse(suggestions, List.of());
    }
}
