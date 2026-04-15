package com.workshop.lab3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilesystemToolsTest {

    @TempDir
    Path tempWorkspace;

    private FilesystemTools tools;

    @BeforeEach
    void setUp() {
        tools = new FilesystemTools();
        ReflectionTestUtils.setField(tools, "workspacePath", tempWorkspace.toString());
    }

    @Test
    void listFiles_returnsFileNames() throws IOException {
        Files.writeString(tempWorkspace.resolve("hello.java"), "class Hello {}");
        Files.createDirectories(tempWorkspace.resolve("sub"));
        Files.writeString(tempWorkspace.resolve("sub/World.java"), "class World {}");

        String result = tools.list_files(".");

        assertThat(result).contains("hello.java");
        assertThat(result).contains("sub/World.java");
    }

    @Test
    void readFile_returnsContents() throws IOException {
        Files.writeString(tempWorkspace.resolve("Readme.md"), "# Hello Workshop");

        String content = tools.read_file("Readme.md");

        assertThat(content).isEqualTo("# Hello Workshop");
    }

    @Test
    void writeFile_createsFile() {
        String result = tools.write_file("output/New.java", "class New {}");

        assertThat(result).contains("New.java");
        assertThat(tempWorkspace.resolve("output/New.java")).exists();
    }

    @Test
    void pathTraversal_isBlocked() {
        assertThatThrownBy(() -> tools.read_file("../../etc/passwd"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("sandbox");
    }
}
