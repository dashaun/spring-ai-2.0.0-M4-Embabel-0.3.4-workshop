package com.workshop.lab4.domain;

/**
 * A developer task the sidekick should perform.
 */
public record TaskRequest(
        String description,
        String projectPath,
        TaskType type
) {
    public enum TaskType {
        GENERATE_CODE,
        REFACTOR,
        ADD_TEST,
        FIX_BUG,
        EXPLAIN
    }
}
