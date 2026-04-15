<!-- .slide: data-background-color="#6db33f" -->

# Lab 4

## Agentic Workflows with Embabel

*"Stop scripting workflows. Start declaring goals."*

Notes:
- Timing: 60 minutes
- Key message: "Stop scripting workflows. Start declaring goals."
- Common issues: Planner timeout (increase embabel.planning.timeout-seconds)
- Demo script: Watch console logs for GOAP planner discovering plan
- Aha moment: Planner automatically incorporates new action without workflow code changes

---

## Validate First

Confirm the lab is ready before you start:

```bash
cd labs/lab-4-agentic-workflows
./mvnw test
```

The test:
- POSTs buggy code to `/review`
- Verifies the response has `title`, `body`, `verdict`
- Confirms the verdict is NOT "APPROVE" (the bugs should be caught)

**All tests green? You're ready.**

Notes:
- If tests fail, check Ollama is running and qwen2.5-coder:1.5b is pulled
- Embabel planner timeout: increase embabel.planning.timeout-seconds in application.yml

---

## The Problem with Hardcoded Workflows

Traditional approach:

```java
// Step 1: analyze
CodeAnalysis analysis = analyzeCode(request);
// Step 2: write report
ReviewReport report = writeReport(analysis);
// Step 3: if you add a new step... rewrite everything
```

Adding a step means rewriting the orchestration.

---

## What You'll Build

A **code review agent** using Embabel's GOAP planner.

You define **what the agent can do** and **what it should achieve**.

The planner discovers the path automatically.

---

## Key Concept: Embabel's Annotation Model

```text
@Agent        → "I am an agent"
@Action       → "I can do this"
@AchievesGoal → "This is the finish line"
```

Domain objects (Java records) are the glue — the planner connects actions by matching output types to input types.

---

## Key Concept: GOAP Planning

**Goal-Oriented Action Planning**:

```text
CodeReviewRequest
    → [analyzeCode]  → CodeAnalysis
    → [writeReport]  → ReviewReport  ← GOAL
```

The planner sees:
- `analyzeCode` takes `CodeReviewRequest`, returns `CodeAnalysis`
- `writeReport` takes `CodeAnalysis`, returns `ReviewReport`
- Goal is `ReviewReport`

It connects them automatically. No orchestration code.

---

## Built-in Safeguard: Types as Boundaries

*"Can I ask the code reviewer for an apple pie recipe?"*

No — and not because of a prompt filter.

```text
POST /review
  input:  CodeReviewRequest
  goal:   ReviewReport

The planner only knows how to produce ReviewReport.
There are no actions for Recipe, Podcast, or anything else.
```

The agent **cannot** go off-script because:
1. Every action is declared with explicit input and output types
2. The planner only discovers paths that lead to the declared goal type
3. If a type doesn't exist in the graph, no path can reach it

> This is why I teach Embabel. Determinism isn't a system prompt. It's the type system.

---

## The Domain Model

Strongly-typed records flow between actions:

```java
public record CodeReviewRequest(String sourceCode, String language, String context) {}

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

public record ReviewReport(String title, String body, CodeAnalysisVerdict verdict) {}

public enum CodeAnalysisVerdict { APPROVE, REQUEST_CHANGES, NEEDS_DISCUSSION }
```

Types are the API contract between actions.

---

## The Agent and Actions

```java
@Agent(description = "Reviews Java source code and produces a structured report")
public class CodeReviewAgent {

    @Action(description = "Analyze Java source code for quality, bugs, and style")
    public CodeAnalysis analyzeCode(CodeReviewRequest request, Ai ai) {
        return ai.withDefaultLlm().createObject("""
                Analyze the following %s code. Identify:
                1. Potential bugs or logic errors
                2. Style and convention violations
                3. Performance concerns
                4. Security issues
                Context: %s
                Source code: ```%s```
                """.formatted(request.language(), request.context(), request.sourceCode()),
                CodeAnalysis.class);
    }

    @AchievesGoal(description = "Produce a human-readable code review report")
    @Action(description = "Compile analysis into a developer-friendly review report")
    public ReviewReport writeReport(CodeAnalysis analysis, Ai ai) { /* ... */ }
}
```

---

## Invoking the Agent

```java
@PostMapping("/review")
public ReviewReport reviewCode(@RequestBody CodeReviewRequest request) {
  return new AgentPlatformTypedOps(agentPlatform)
          .transform(request, ReviewReport.class, ProcessOptions.DEFAULT);
}
```

Give input + goal type. The platform handles everything.

---

## Hands-On

```bash
cd labs/lab-4-agentic-workflows
./mvnw spring-boot:run
```

```bash
curl -X POST http://localhost:8084/review \
  -H "Content-Type: application/json" \
  -d '{
    "sourceCode": "public class UserService {\n  public User findById(String id) {\n    for (User u : users) {\n      if (u.getId() == id) return u;\n    }\n    return null;\n  }\n}",
    "language": "Java",
    "context": "Service layer in a Spring Boot REST API"
  }'
```

Watch the console logs — you'll see the GOAP planner discover and execute the action sequence.

---

## What the Agent Finds

The buggy code has two issues:

1. **String identity comparison** (`==` instead of `.equals()`)
2. **Returning `null`** instead of `Optional<User>`

The verdict should be `REQUEST_CHANGES`.

---

## Exercise: Add a SecurityScan Action

Add a new action — the planner adapts **automatically**.

**Step 1:** Create `SecurityReport.java` in the `domain` package:

```java
public record SecurityReport(
        String summary,
        List<String> vulnerabilities,
        SecurityRiskLevel riskLevel
) {
    public SecurityReport {
        if (vulnerabilities == null) vulnerabilities = List.of();
    }
}
```

---

## Exercise: SecurityScan (cont.)

**Step 2:** Add a `scanForVulnerabilities` action in `CodeReviewAgent`:

```java
@Action(description = "Scan code for security vulnerabilities")
public SecurityReport scanForVulnerabilities(CodeReviewRequest request, Ai ai) {
    return ai.withDefaultLlm().createObject("""
        Scan this %s code for security vulnerabilities.
        Look for: injection, auth issues, data exposure,
        unsafe deserialization, hardcoded secrets.
        Context: %s
        Code: ```%s```
        """.formatted(request.language(), request.context(), request.sourceCode()),
        SecurityReport.class);
}
```

**Step 3:** Update `writeReport` to also accept `SecurityReport` as a parameter.

---

## Exercise: The New Flow

The planner now discovers a **three-step** plan automatically:

```text
CodeReviewRequest
    → [analyzeCode]             → CodeAnalysis
    → [scanForVulnerabilities]  → SecurityReport
    → [writeReport]             → ReviewReport  ← GOAL
```

`writeReport` needs both `CodeAnalysis` **and** `SecurityReport`, so the planner runs both scans before the report.

**No orchestration code changed. Just new types and actions.**

---

## Key Takeaways

- Embabel's GOAP planner connects actions by type signatures
- **Types enforce boundaries** — the agent can only do what you've declared
- Adding capabilities doesn't require rewriting workflows
- Each action is independently testable with `FakeOperationContext`
- Domain objects (records) are the contract between actions
- Created by Rod Johnson (yes, the Spring Framework creator)

Notes:
- Pair slower attendees for this lab — GOAP is conceptually dense
- The key insight: types are the wiring, not code
