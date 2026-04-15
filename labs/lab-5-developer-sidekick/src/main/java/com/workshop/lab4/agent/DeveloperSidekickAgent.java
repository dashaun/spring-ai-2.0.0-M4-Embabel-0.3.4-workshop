package com.workshop.lab4.agent;

import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.Export;
import com.embabel.agent.api.common.Ai;
import com.workshop.lab4.domain.GeneratedCode;
import com.workshop.lab4.domain.ProjectContext;
import com.workshop.lab4.domain.TaskRequest;
import com.workshop.lab4.domain.TaskResult;
import com.workshop.lab4.tools.FileSystemTools;
import org.springframework.ai.chat.client.ChatClient;

import java.io.IOException;
import java.util.List;

/**
 * Lab 5 — The Developer Sidekick: your local AI pair programmer.
 *
 * <h2>Architecture</h2>
 * <p>This agent assembles everything from the previous labs:</p>
 * <ul>
 *   <li><strong>Lab 1</strong> — LLM interaction via Spring AI</li>
 *   <li><strong>Lab 2</strong> — RAG for documentation context</li>
 *   <li><strong>Lab 3</strong> — MCP filesystem server (tools over HTTP)</li>
 *   <li><strong>Lab 4</strong> — Embabel's GOAP planning</li>
 *   <li><strong>Lab 5</strong> — Everything together: GOAP + MCP</li>
 * </ul>
 *
 * <h2>The plan Embabel discovers</h2>
 * <pre>
 *   TaskRequest
 *       → [gatherProjectContext]  → ProjectContext
 *       → [generateCode]          → GeneratedCode
 *       → [applyAndVerify]        → TaskResult   ← GOAL
 * </pre>
 *
 * <h2>The "Aha!" Moment</h2>
 * <p>Ask the agent: "Add a REST endpoint to this Spring Boot project
 * that returns system health metrics." Watch it:</p>
 * <ol>
 *   <li>Scan your project structure via MCP filesystem tools</li>
 *   <li>Read your existing code to understand conventions</li>
 *   <li>Generate a new controller following YOUR project's patterns</li>
 *   <li>Write the file to disk</li>
 *   <li>Report what it did and suggest next steps</li>
 * </ol>
 */
@Agent(description = "Developer sidekick that can read, understand, and modify Java projects")
public class DeveloperSidekickAgent {

    private final FileSystemTools fileSystemTools;
    private final ChatClient chatClient;

    public DeveloperSidekickAgent(FileSystemTools fileSystemTools, ChatClient.Builder chatClientBuilder) {
        this.fileSystemTools = fileSystemTools;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Action 1: Gather context about the project.
     *
     * <p>Reads the project structure, build file, and key source files
     * to understand what we're working with.</p>
     */
    @Action(description = "Scan project filesystem to build understanding of structure and conventions")
    public ProjectContext gatherProjectContext(TaskRequest request, Ai ai) throws IOException {
        // Use the filesystem tool to scan the project
        String fileList = fileSystemTools.listProjectFiles(request.projectPath());

        // Try to read the build file for dependency info
        String buildFile = "";
        try {
            buildFile = fileSystemTools.readFile(request.projectPath() + "/pom.xml");
        } catch (Exception e) {
            // Might be a Gradle project
            try {
                buildFile = fileSystemTools.readFile(request.projectPath() + "/build.gradle");
            } catch (Exception ignored) {}
        }

        // Ask the LLM to synthesize project context
        return ai.withDefaultLlm().createObject("""
                Analyze this Java project and extract structured context.

                File listing:
                %s

                Build file contents:
                %s

                Task to accomplish: %s

                You MUST respond with ONLY a single-line JSON object on one line, no newlines inside the JSON.
                Example: {"projectName":"my-app","buildTool":"maven","javaVersion":"17","dependencies":["spring-boot-starter-web"],"relevantFiles":{"src/main/java/com/example/App.java":"main app"}}
                Respond with ONLY the JSON on a single line. No markdown, no explanation.
                """.formatted(fileList, buildFile, request.description()),
                ProjectContext.class);
    }

    /**
     * Action 2: Generate code based on the task and project context.
     *
     * <p>Uses Embabel's {@code createObject} for small JSON metadata (filename,
     * explanation) and Spring AI's {@code ChatClient} for the actual code content
     * as plain text. This avoids asking the LLM to embed multi-line Java source
     * inside a JSON string — something small models like qwen2.5-coder:1.5b cannot do
     * reliably.</p>
     */
    @Action(description = "Generate Java code that follows the project's existing conventions")
    public GeneratedCode generateCode(ProjectContext context, TaskRequest request, Ai ai)
            throws IOException {

        // Read existing source files to learn conventions
        StringBuilder existingCode = new StringBuilder();
        for (var entry : context.relevantFiles().entrySet()) {
            try {
                String content = fileSystemTools.readFile(
                        request.projectPath() + "/" + entry.getKey());
                existingCode.append("// --- %s ---\n%s\n\n".formatted(
                        entry.getKey(), content));
            } catch (Exception ignored) {}
        }

        // Step 1: Get metadata as JSON (small, parseable)
        record CodePlan(String filename, String explanation) {}
        CodePlan plan = ai.withDefaultLlm().createObject("""
                You are a senior Java developer planning code generation.

                Task: %s
                Project: %s (Java %s, %s)
                Package: com.example

                Respond with ONLY a single-line JSON: {"filename":"src/main/java/com/example/SomeFile.java","explanation":"what this code does"}
                """.formatted(
                request.description(),
                context.projectName(),
                context.javaVersion(),
                context.buildTool()
        ), CodePlan.class);

        // Step 2: Generate actual code as plain text via ChatClient
        // This avoids the impossible task of putting Java source inside JSON.
        String codeContent = chatClient.prompt()
                .user("""
                        You are a senior Java developer. Write ONLY the Java source file, nothing else.
                        No markdown fences, no explanation — just the raw .java file content.

                        Task: %s
                        Filename: %s
                        Project: %s (Java %s, %s)
                        Dependencies: %s

                        Existing code for reference (follow these conventions):
                        %s

                        Write the complete Java source file now:
                        """.formatted(
                        request.description(),
                        plan.filename(),
                        context.projectName(),
                        context.javaVersion(),
                        context.buildTool(),
                        String.join(", ", context.dependencies()),
                        existingCode.toString()))
                .call()
                .content();

        return new GeneratedCode(
                plan.filename(),
                codeContent,
                plan.explanation(),
                List.of()
        );
    }

    /**
     * Action 3: Write the generated code to disk and verify.
     *
     * <p>This is the <strong>"Aha!" moment</strong> — the agent actually
     * modifies your project, and you can see the file appear in your IDE.</p>
     */
    @AchievesGoal(
            description = "Complete a developer task by writing code to the project",
            export = @Export(remote = true, name = "developerTaskResult")
    )
    @Action(description = "Write generated code to the filesystem and report results")
    public TaskResult applyAndVerify(GeneratedCode code, TaskRequest request) throws IOException {
        // Strip markdown fences if the LLM wrapped the code
        String content = code.content();
        if (content.startsWith("```")) {
            content = content.replaceFirst("^```[\\w]*\\n?", "").replaceFirst("\\n?```$", "");
        }

        // Write the generated file
        fileSystemTools.writeFile(
                request.projectPath() + "/" + code.filename(),
                content
        );

        return new TaskResult(
                code.explanation(),
                List.of(),
                List.of(code.filename()),
                "File written. Run `mvn compile` to verify, then review the generated code."
        );
    }
}
