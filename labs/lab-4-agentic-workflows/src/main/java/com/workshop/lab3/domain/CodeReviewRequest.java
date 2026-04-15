package com.workshop.lab3.domain;

/**
 * Lab 3 — Domain model: a request to review a piece of code.
 *
 * <p>Embabel encourages a rich, typed domain model. These records flow
 * between actions — the planner uses their types to connect actions
 * into a coherent plan (like type-safe pipes).</p>
 */
public record CodeReviewRequest(
        String sourceCode,
        String language,
        String context
) {}
