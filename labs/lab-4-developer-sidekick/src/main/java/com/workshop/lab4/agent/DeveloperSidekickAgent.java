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

import java.io.IOException;
import java.util.List;

/**
 * Lab 4 — The Developer Sidekick: your local AI pair programmer.
 *
 * <h2>Architecture</h2>
 * <p>This agent combines everything from the previous labs:</p>
 * <ul>
 *   <li><strong>Lab 1</strong> — LLM interaction via Spring AI</li>
 *   <li><strong>Lab 2</strong> — RAG for documentation context</li>
 *   <li><strong>Lab 3</strong> — Embabel's GOAP planning</li>
 *   <li><strong>Lab 4</strong> — MCP tools for filesystem access</li>
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

    public DeveloperSidekickAgent(FileSystemTools fileSystemTools) {
        this.fileSystemTools = fileSystemTools;
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
        } catch (IOException e) {
            // Might be a Gradle project
            try {
                buildFile = fileSystemTools.readFile(request.projectPath() + "/build.gradle");
            } catch (IOException ignored) {}
        }

        // Ask the LLM to synthesize project context
        return ai.withDefaultLlm().createObject("""
                Analyze this Java project and extract structured context.

                File listing:
                %s

                Build file contents:
                %s

                Task to accomplish: %s

                Return a JSON object: {
                  "projectName": "...",
                  "buildTool": "maven|gradle",
                  "javaVersion": "...",
                  "dependencies": ["..."],
                  "relevantFiles": { "filename": "purpose" }
                }
                """.formatted(fileList, buildFile, request.description()),
                ProjectContext.class);
    }

    /**
     * Action 2: Generate code based on the task and project context.
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
            } catch (IOException ignored) {}
        }

        return ai.withDefaultLlm().createObject("""
                You are a senior Java developer. Generate code for this task:

                Task: %s
                Project: %s (Java %s, %s)
                Dependencies: %s

                Existing code for reference (follow these conventions):
                %s

                Generate production-quality code. Return JSON: {
                  "filename": "src/main/java/.../FileName.java",
                  "content": "full file content",
                  "explanation": "what this code does and why",
                  "newDependencies": ["groupId:artifactId if needed"]
                }
                """.formatted(
                request.description(),
                context.projectName(),
                context.javaVersion(),
                context.buildTool(),
                String.join(", ", context.dependencies()),
                existingCode.toString()
        ),
                GeneratedCode.class);
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
        // Write the generated file
        fileSystemTools.writeFile(
                request.projectPath() + "/" + code.filename(),
                code.content()
        );

        return new TaskResult(
                code.explanation(),
                List.of(),
                List.of(code.filename()),
                "File written. Run `mvn compile` to verify, then review the generated code."
        );
    }
}
