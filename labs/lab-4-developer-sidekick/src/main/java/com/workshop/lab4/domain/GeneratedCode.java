package com.workshop.lab4.domain;

import java.util.List;

/**
 * Output from the code generation action — ready to be written to disk.
 */
public record GeneratedCode(
        String filename,
        String content,
        String explanation,
        List<String> newDependencies
) {
    public GeneratedCode {
        if (filename == null) filename = "GeneratedFile.java";
        if (content == null) content = "// No content generated";
        if (explanation == null) explanation = "";
        if (newDependencies == null) newDependencies = List.of();
    }
}
