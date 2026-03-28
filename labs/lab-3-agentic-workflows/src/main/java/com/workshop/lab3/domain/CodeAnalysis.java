package com.workshop.lab3.domain;

import java.util.List;

/**
 * Structured output from the code analysis action.
 */
public record CodeAnalysis(
        String summary,
        List<String> issues,
        List<String> suggestions,
        int qualityScore
) {}
