package com.stacklens.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the CLI analyze command.
 *
 * Uses picocli's CommandLine to simulate actual CLI invocations.
 */
class AnalyzeCommandTest {

    /** Captures stdout output during a CLI call. */
    private String captureOutput(String... args) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream originalOut = System.out;
        System.setOut(ps);

        try {
            CommandLine cmd = new CommandLine(new Main());
            cmd.execute(args);
        } finally {
            System.setOut(originalOut);
        }

        return baos.toString();
    }

    /** Runs the command and returns its exit code. */
    private int exitCode(String... args) {
        CommandLine cmd = new CommandLine(new Main());
        return cmd.execute(args);
    }

    // ── --text mode tests ────────────────────────────────────────────────────

    @Test
    void textMode_detectsNullPointerException() {
        String output = captureOutput("analyze", "--text",
            "java.lang.NullPointerException at OrderService.java:42", "--no-color");

        assertTrue(output.contains("NullPointerException"),
            "Output should mention NullPointerException");
    }

    @Test
    void textMode_showsExplanationAndSuggestions() {
        String output = captureOutput("analyze", "--text",
            "java.lang.NullPointerException", "--no-color");

        assertTrue(output.contains("Explanation"), "Output should have Explanation section");
        assertTrue(output.contains("Suggested fixes"), "Output should have Suggested fixes section");
    }

    @Test
    void textMode_producesJsonOutput() {
        String output = captureOutput("analyze", "--text",
            "java.lang.NullPointerException", "--output", "json");

        assertTrue(output.contains("\"issue\""), "JSON output should contain 'issue' field");
        assertTrue(output.contains("NullPointerException"), "JSON should mention the issue type");
        assertTrue(output.contains("\"suggestions\""), "JSON output should contain suggestions");
    }

    @Test
    void textMode_cleanLogReportsNoIssues() {
        String output = captureOutput("analyze", "--text",
            "INFO Application started successfully", "--no-color");

        assertTrue(output.contains("No known issues detected"),
            "Clean log should report no issues");
    }

    @Test
    void textMode_exitCode2WhenIssuesFound() {
        int code = exitCode("analyze", "--text", "java.lang.NullPointerException");
        assertEquals(2, code, "Exit code should be 2 when issues are found");
    }

    @Test
    void textMode_exitCode0WhenNoIssues() {
        int code = exitCode("analyze", "--text", "INFO Application started");
        assertEquals(0, code, "Exit code should be 0 when no issues are found");
    }

    // ── File mode tests ──────────────────────────────────────────────────────

    @Test
    void fileMode_detectsIssuesInLogFile(@TempDir Path tempDir) throws Exception {
        Path logFile = tempDir.resolve("app.log");
        Files.write(logFile, List.of(
            "INFO  Starting up",
            "ERROR java.lang.NullPointerException at OrderService.java:10"
        ));

        String output = captureOutput("analyze", logFile.toString(), "--no-color");

        assertTrue(output.contains("NullPointerException"));
    }

    @Test
    void fileMode_returnsExitCode1ForMissingFile() {
        int code = exitCode("analyze", "/tmp/nonexistent_file_xyz_12345.log");
        assertEquals(1, code, "Exit code should be 1 for file not found");
    }

    // ── Error handling tests ─────────────────────────────────────────────────

    @Test
    void noArguments_returnsExitCode1() {
        // "analyze" with no file and no --text should fail
        int code = exitCode("analyze");
        assertEquals(1, code);
    }

    @Test
    void helpFlag_printsHelpAndExits() {
        String output = captureOutput("analyze", "--help");
        assertTrue(output.contains("analyze") || output.contains("Usage"),
            "Help output should mention the command");
    }
}
