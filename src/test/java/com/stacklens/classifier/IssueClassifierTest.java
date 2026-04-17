package com.stacklens.classifier;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for IssueClassifier's enrichment behaviour:
 * occurrence counting, stack trace context, and location extraction.
 */
class IssueClassifierTest {

    private IssueClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new IssueClassifier();
    }

    @Test
    void countsOccurrencesWhenSameIssueAppearsMultipleTimes() {
        List<String> lines = List.of(
            "java.lang.NullPointerException: first time",
            "2024-01-15 INFO  something normal",
            "java.lang.NullPointerException: second time",
            "2024-01-15 INFO  something normal",
            "java.lang.NullPointerException: third time"
        );

        List<Issue> issues = classifier.classify(lines);

        assertEquals(1, issues.size(), "Same issue type should be reported once");
        assertEquals(3, issues.get(0).getOccurrenceCount());
    }

    @Test
    void collectsStackContextAfterMatchedLine() {
        List<String> lines = List.of(
            "java.lang.NullPointerException: Cannot invoke method getEmail()",
            "\tat com.example.OrderService.createOrder(OrderService.java:42)",
            "\tat com.example.OrderController.submit(OrderController.java:28)",
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)"
        );

        List<Issue> issues = classifier.classify(lines);

        assertFalse(issues.get(0).getStackContext().isEmpty(), "Stack context should be collected");
        assertTrue(issues.get(0).getStackContext().size() >= 2);
    }

    @Test
    void extractsAppLocationSkippingJdkFrames() {
        List<String> lines = List.of(
            "java.lang.NullPointerException: Cannot invoke getEmail()",
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)",
            "\tat java.lang.reflect.Method.invoke(Method.java:498)",
            "\tat com.example.OrderService.createOrder(OrderService.java:42)"
        );

        List<Issue> issues = classifier.classify(lines);

        String location = issues.get(0).getLocation();
        assertNotNull(location, "App location should be extracted");
        assertTrue(location.contains("OrderService"), "Should skip JDK frames and find app frame");
        assertTrue(location.contains("42"), "Should include line number");
    }

    @Test
    void returnsNullLocationWhenNoAppFrameFound() {
        // Only JDK internal frames — no app code visible
        List<String> lines = List.of(
            "java.lang.NullPointerException",
            "\tat java.lang.reflect.Method.invoke(Method.java:498)",
            "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)"
        );

        List<Issue> issues = classifier.classify(lines);

        assertNull(issues.get(0).getLocation());
    }

    @Test
    void issueWithNoFollowingStackHasEmptyContext() {
        List<String> lines = List.of(
            "ERROR 500 Internal Server Error for /api/orders",
            "2024-01-15 INFO  Next unrelated log line"
        );

        List<Issue> issues = classifier.classify(lines);

        assertTrue(issues.get(0).getStackContext().isEmpty());
    }

    @Test
    void severityIsSetCorrectlyOnEnrichedIssue() {
        List<String> issues = List.of(
            "java.lang.OutOfMemoryError: Java heap space"
        );
        List<Issue> result = classifier.classify(issues);

        assertEquals(Severity.CRITICAL, result.get(0).getSeverity());
    }

    @Test
    void multipleDistinctIssuesAreAllReported() {
        List<String> lines = List.of(
            "java.lang.NullPointerException: null",
            "java.lang.OutOfMemoryError: Java heap space",
            "java.net.SocketTimeoutException: Read timed out"
        );

        List<Issue> issues = classifier.classify(lines);

        assertEquals(3, issues.size());
    }

    @Test
    void occurrenceCountIsOneForSingleOccurrence() {
        List<Issue> issues = classifier.classify(List.of("java.lang.NullPointerException"));
        assertEquals(1, issues.get(0).getOccurrenceCount());
    }
}
