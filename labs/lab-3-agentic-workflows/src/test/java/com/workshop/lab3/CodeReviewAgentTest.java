package com.workshop.lab3;

import com.embabel.agent.test.unit.FakeOperationContext;
import com.workshop.lab3.agent.CodeReviewAgent;
import com.workshop.lab3.domain.CodeAnalysis;
import com.workshop.lab3.domain.CodeAnalysisVerdict;
import com.workshop.lab3.domain.CodeReviewRequest;
import com.workshop.lab3.domain.ReviewReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for each {@link CodeReviewAgent} action using Embabel's
 * {@link com.embabel.agent.test.unit.FakePromptRunner}.
 *
 * <p>No LLM, no Spring context, no network — these run in milliseconds.</p>
 */
class CodeReviewAgentTest {

    CodeReviewAgent agent;
    FakeOperationContext context;

    @BeforeEach
    void setUp() {
        agent = new CodeReviewAgent();
        context = FakeOperationContext.create();
    }

    @Test
    void analyzeCode_returnsStructuredAnalysis() {
        var expected = new CodeAnalysis(
                "Uses == for String comparison and returns null instead of Optional",
                List.of("String identity comparison with ==", "Returns null instead of Optional<User>"),
                List.of("Use .equals() for String comparison", "Return Optional<User> instead of null"),
                35
        );
        context.expectResponse(expected);

        var request = new CodeReviewRequest(
                "public class UserService { public User findById(String id) { if (u.getId() == id) return u; } }",
                "Java",
                "Service layer in a Spring Boot REST API"
        );

        CodeAnalysis result = agent.analyzeCode(request, context.ai());

        assertThat(result).isEqualTo(expected);
        assertThat(result.summary()).contains("==");
        assertThat(result.issues()).hasSize(2);
        assertThat(result.suggestions()).hasSize(2);
        assertThat(result.qualityScore()).isBetween(0, 100);
        assertThat(context.getLlmInvocations()).hasSize(1);
    }

    @Test
    void writeReport_producesReportFromAnalysis() {
        var expected = new ReviewReport(
                "String Comparison Bug Found",
                "Using == instead of equals() for String comparison in findById. Replace with .equals() to compare by value.",
                CodeAnalysisVerdict.REQUEST_CHANGES
        );
        context.expectResponse(expected);

        var analysis = new CodeAnalysis(
                "Code has a string comparison bug",
                List.of("== used instead of .equals()"),
                List.of("Use .equals() for String comparison"),
                40
        );

        ReviewReport result = agent.writeReport(analysis, context.ai());

        assertThat(result).isEqualTo(expected);
        assertThat(result.title()).isNotBlank();
        assertThat(result.body()).contains("equals");
        assertThat(result.verdict()).isIn(CodeAnalysisVerdict.values());
        assertThat(context.getLlmInvocations()).hasSize(1);
    }

    @Test
    void writeReport_approvesCleanCode() {
        var expected = new ReviewReport(
                "Clean Code Review",
                "Code follows Java best practices. No issues found.",
                CodeAnalysisVerdict.APPROVE
        );
        context.expectResponse(expected);

        var cleanAnalysis = new CodeAnalysis(
                "Well-written code following best practices",
                List.of(),
                List.of(),
                95
        );

        ReviewReport result = agent.writeReport(cleanAnalysis, context.ai());

        assertThat(result.verdict()).isEqualTo(CodeAnalysisVerdict.APPROVE);
        assertThat(context.getLlmInvocations()).hasSize(1);
    }
}
