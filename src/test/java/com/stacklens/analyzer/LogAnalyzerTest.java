package com.stacklens.analyzer;

import com.stacklens.model.AnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogAnalyzerTest {

    private LogAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new LogAnalyzer();
    }

    // ── Text analysis tests ──────────────────────────────────────────────────

    @Test
    void analyzeText_detectsNullPointerException() {
        String text = "java.lang.NullPointerException at com.example.OrderService.process(OrderService.java:42)";

        AnalysisResult result = analyzer.analyzeText(text);

        assertTrue(result.hasIssues());
        assertEquals(1, result.getIssues().size());
        assertEquals("NullPointerException", result.getIssues().get(0).getType());
    }

    @Test
    void analyzeText_returnsCleanResultForHealthyLog() {
        String text = "2024-01-15 10:30:00 INFO  Application started\n" +
                      "2024-01-15 10:30:01 INFO  Connected to database\n" +
                      "2024-01-15 10:30:02 INFO  Server listening on port 8080";

        AnalysisResult result = analyzer.analyzeText(text);

        assertFalse(result.hasIssues());
        assertEquals(0, result.getIssues().size());
    }

    @Test
    void analyzeText_detectsMultipleIssues() {
        String text = "java.lang.NullPointerException at OrderService.java:10\n" +
                      "java.net.SocketTimeoutException: Read timed out\n" +
                      "2024-01-15 INFO  Processing request";

        AnalysisResult result = analyzer.analyzeText(text);

        assertTrue(result.hasIssues());
        assertTrue(result.getIssues().size() >= 2,
            "Should detect both NullPointerException and TimeoutError");
    }

    @Test
    void analyzeText_deduplicatesSameIssueType() {
        // The same NPE appearing 3 times should only be reported once
        String text = "NullPointerException at line 1\n" +
                      "NullPointerException at line 2\n" +
                      "NullPointerException at line 3";

        AnalysisResult result = analyzer.analyzeText(text);

        assertEquals(1, result.getIssues().size(),
            "Duplicate issue types should be reported only once");
    }

    @Test
    void analyzeText_sourceIsLabeledAsInlineText() {
        AnalysisResult result = analyzer.analyzeText("NullPointerException");
        assertEquals("inline text", result.getSource());
    }

    @Test
    void analyzeText_handlesWindowsLineEndings() {
        String text = "java.lang.NullPointerException\r\nsome other line\r\n";

        AnalysisResult result = analyzer.analyzeText(text);

        assertTrue(result.hasIssues());
    }

    @Test
    void analyzeText_handlesEmptyString() {
        AnalysisResult result = analyzer.analyzeText("");

        assertFalse(result.hasIssues());
    }

    // ── File analysis tests ──────────────────────────────────────────────────

    @Test
    void analyzeFile_detectsIssuesInLogFile(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("app.log");
        Files.write(logFile, List.of(
            "2024-01-15 10:30:00 INFO  Starting application",
            "2024-01-15 10:30:01 ERROR java.lang.NullPointerException at OrderService.java:42",
            "2024-01-15 10:30:01 ERROR     at com.example.Main.run(Main.java:10)"
        ));

        AnalysisResult result = analyzer.analyzeFile(logFile);

        assertTrue(result.hasIssues());
        assertEquals("NullPointerException", result.getIssues().get(0).getType());
    }

    @Test
    void analyzeFile_sourceIsFilePath(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("server.log");
        Files.write(logFile, List.of("INFO Application started"));

        AnalysisResult result = analyzer.analyzeFile(logFile);

        assertEquals(logFile.toString(), result.getSource());
    }

    @Test
    void analyzeFile_throwsIfFileNotFound() {
        Path missing = Path.of("/tmp/does_not_exist_12345.log");
        assertThrows(IOException.class, () -> analyzer.analyzeFile(missing));
    }

    @Test
    void analyzeFile_returnsCleanResultForHealthyLogFile(@TempDir Path tempDir) throws IOException {
        Path logFile = tempDir.resolve("healthy.log");
        Files.write(logFile, List.of(
            "INFO  Application started",
            "INFO  Database connection established",
            "INFO  Health check passed"
        ));

        AnalysisResult result = analyzer.analyzeFile(logFile);

        assertFalse(result.hasIssues());
    }
}
