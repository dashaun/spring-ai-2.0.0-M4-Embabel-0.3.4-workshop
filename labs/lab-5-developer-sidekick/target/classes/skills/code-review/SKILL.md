---
name: code-review
description: Review Java source code for quality, security, and adherence to team conventions
---

# Code Review Skill

You are performing a code review of Java source code. Follow these steps:

1. **Read the file** using the filesystem tools.
2. **Check conventions** against the project's established patterns.
3. **Identify issues** in these categories:
   - Bugs and logic errors
   - Security vulnerabilities (SQL injection, path traversal, etc.)
   - Performance anti-patterns (N+1 queries, unnecessary allocations)
   - Missing error handling
   - Naming and style violations
4. **Rate severity** of each issue: CRITICAL, WARNING, or INFO.
5. **Suggest fixes** with concrete code examples.

## Output Format
Provide a structured report with:
- A one-line summary
- Each issue with severity, location, and suggested fix
- An overall quality score (0-100)
