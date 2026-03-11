package com.stacklens.detector;

import com.stacklens.model.Issue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NullPointerDetectorTest {

    private NullPointerDetector detector;

    @BeforeEach
    void setUp() {
        detector = new NullPointerDetector();
    }

    @Test
    void detectsFullyQualifiedNullPointerException() {
        String line = "java.lang.NullPointerException at com.example.OrderService.processOrder(OrderService.java:42)";
        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("NullPointerException", result.get().getType());
    }

    @Test
    void detectsShortNullPointerException() {
        String line = "Caused by: NullPointerException";
        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
    }

    @Test
    void doesNotMatchUnrelatedLine() {
        String line = "2024-01-15 10:30:00 INFO  Application started successfully";
        Optional<Issue> result = detector.detect(line);

        assertFalse(result.isPresent());
    }

    @Test
    void doesNotMatchNullInput() {
        Optional<Issue> result = detector.detect(null);
        assertFalse(result.isPresent());
    }

    @Test
    void matchedLineIsPreservedInIssue() {
        String line = "  java.lang.NullPointerException  ";
        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        // The matched line should be trimmed
        assertEquals("java.lang.NullPointerException", result.get().getMatchedLine());
    }

    @Test
    void issueContainsSuggestions() {
        String line = "java.lang.NullPointerException";
        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertFalse(result.get().getSuggestions().isEmpty());
    }

    @Test
    void issueTypeMatchesDetectorType() {
        String line = "java.lang.NullPointerException";
        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals(detector.getIssueType(), result.get().getType());
    }
}
