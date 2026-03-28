package com.workshop.lab3.agent;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.Ai;
import com.workshop.lab3.domain.CodeAnalysis;
import com.workshop.lab3.domain.CodeReviewRequest;
import com.workshop.lab3.domain.ReviewReport;

/**
 * Lab 3 — A code review agent built with Embabel's annotation model.
 *
 * <h2>How it works</h2>
 * <p>Embabel uses <strong>Goal-Oriented Action Planning (GOAP)</strong>.
 * You define <em>Actions</em> (what the agent can do) and <em>Goals</em>
 * (what it should achieve). The planner automatically discovers a path
 * from the current state to the goal — you don't hardcode the workflow.</p>
 *
 * <h2>The flow Embabel will discover</h2>
 * <pre>
 *   CodeReviewRequest
 *       → [analyzeCode]  → CodeAnalysis
 *       → [writeReport]  → ReviewReport   ← GOAL
 * </pre>
 *
 * <h2>Why this matters</h2>
 * <p>Each action is independently testable, reusable, and composable.
 * Add a new action that produces {@code CodeAnalysis} from a Git diff,
 * and the planner will incorporate it automatically.</p>
 *
 * <h2>Exercises</h2>
 * <ol>
 *   <li>Run the agent and observe the plan it creates in the logs.</li>
 *   <li>Add a {@code SecurityScan} action between analysis and report.</li>
 *   <li>Write a unit test using Embabel's {@code FakePromptRunner}.</li>
 * </ol>
 */
@Agent(description = "Reviews Java source code and produces a structured report")
public class CodeReviewAgent {

    /**
     * Action 1: Analyze the source code.
     *
     * <p>Takes raw code and produces a structured analysis.
     * The {@link Ai} parameter is injected by Embabel and provides
     * access to the configured LLM.</p>
     */
    @Action(description = "Analyze Java source code for quality, bugs, and style")
    public CodeAnalysis analyzeCode(CodeReviewRequest request, Ai ai) {
        return ai.withDefaultLlm().createObject("""
                Analyze the following %s code. Identify:
                1. Potential bugs or logic errors
                2. Style and convention violations
                3. Performance concerns
                4. Security issues

                Context: %s

                Source code:
                ```
                %s
                ```

                Respond with a JSON object matching this structure:
                { "summary": "...", "issues": [...], "suggestions": [...], "qualityScore": 0-100 }
                """.formatted(request.language(), request.context(), request.sourceCode()),
                CodeAnalysis.class);
    }

    /**
     * Action 2: Write the final review report.
     *
     * <p>The {@code @AchievesGoal} annotation tells the planner that
     * reaching this action satisfies the agent's objective.</p>
     */
    @AchievesGoal(
            description = "Produce a human-readable code review report",
            export = @Export(remote = true, name = "codeReviewReport")
    )
    @Action(description = "Compile analysis into a developer-friendly review report")
    public ReviewReport writeReport(CodeAnalysis analysis, Ai ai) {
        return ai.withDefaultLlm().createObject("""
                You are a senior Java developer writing a code review.
                Based on the following analysis, write a clear, actionable report.

                Quality Score: %d/100
                Summary: %s
                Issues found: %s
                Suggestions: %s

                Write the report with:
                - A short title
                - A body with specific, actionable feedback (use code examples)
                - A verdict: APPROVE, REQUEST_CHANGES, or NEEDS_DISCUSSION

                Respond as JSON: { "title": "...", "body": "...", "verdict": "..." }
                """.formatted(
                analysis.qualityScore(),
                analysis.summary(),
                String.join(", ", analysis.issues()),
                String.join(", ", analysis.suggestions())
        ),
                ReviewReport.class);
    }

    // =========================================================================
    // TODO (Exercise): Add a SecurityScan action
    // =========================================================================
    // Hint: Create a SecurityReport record, then an @Action that takes
    // CodeReviewRequest and returns SecurityReport. The planner will
    // automatically incorporate it if you update writeReport to also
    // accept SecurityReport as a parameter.
}
