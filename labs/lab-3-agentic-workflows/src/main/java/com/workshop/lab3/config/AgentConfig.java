package com.workshop.lab3.config;

import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.workshop.lab3.domain.CodeReviewRequest;
import com.workshop.lab3.domain.ReviewReport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lab 3 — Invoking the agent through the Embabel platform.
 *
 * <p>The {@link AgentPlatform} is the entry point for running agents.
 * You provide input and a goal type — the platform finds agents that
 * can achieve it, plans the actions, and executes them.</p>
 */
@RestController
public class AgentConfig {

    private final AgentPlatform agentPlatform;

    public AgentConfig(AgentPlatform agentPlatform) {
        this.agentPlatform = agentPlatform;
    }

    /**
     * Submit code for review. The Embabel planner will:
     * 1. Discover that CodeReviewAgent can produce a ReviewReport.
     * 2. Plan the action sequence: analyzeCode → writeReport.
     * 3. Execute each action, passing typed outputs between them.
     */
    @PostMapping("/review")
    public ReviewReport reviewCode(@RequestBody CodeReviewRequest request) {
        return agentPlatform.runFor(
                new UserInput(request.sourceCode()),
                ReviewReport.class
        );
    }
}
