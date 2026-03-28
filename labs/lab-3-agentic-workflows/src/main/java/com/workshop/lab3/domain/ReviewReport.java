package com.workshop.lab3.domain;

/**
 * The final, human-readable review report — this is the Goal.
 */
public record ReviewReport(
        String title,
        String body,
        String verdict
) {}
