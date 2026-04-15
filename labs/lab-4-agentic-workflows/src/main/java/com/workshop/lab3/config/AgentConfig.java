package com.workshop.lab3.config;

import com.embabel.agent.api.common.AgentPlatformTypedOps;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.core.ProcessOptions;
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
     *
     * <p>Pass the {@link CodeReviewRequest} directly so the planner can see it
     * on the blackboard and connect it to the {@code analyzeCode} action.
     * Wrapping it in a {@code UserInput} would leave the planner with no path
     * from UserInput → CodeReviewRequest → CodeAnalysis → ReviewReport (STUCK).</p>
     */
    @PostMapping("/review")
    public ReviewReport reviewCode(@RequestBody CodeReviewRequest request) {
        return new AgentPlatformTypedOps(agentPlatform)
                .transform(request, ReviewReport.class, ProcessOptions.DEFAULT);
    }
}
