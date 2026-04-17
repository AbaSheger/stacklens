package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for the five detectors added in v1.1.0.
 */
class NewDetectorsTest {

    private static final String CLEAN_LINE = "2024-01-15 10:30:00 INFO  Application started successfully";

    // ── SpringBeanDetector ────────────────────────────────────────────────────

    @Test
    void springBean_detectsNoSuchBeanDefinitionException() {
        IssueDetector detector = new SpringBeanDetector();
        String line = "org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.example.PaymentService'";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("SpringBeanFailure", result.get().getType());
        assertEquals(Severity.CRITICAL, result.get().getSeverity());
    }

    @Test
    void springBean_detectsBeanCreationException() {
        IssueDetector detector = new SpringBeanDetector();
        String line = "org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'orderService'";

        assertTrue(detector.detect(line).isPresent());
    }

    @Test
    void springBean_detectsCircularDependency() {
        IssueDetector detector = new SpringBeanDetector();
        String line = "The dependencies of some of the beans in the application context form a cycle";

        assertTrue(detector.detect(line).isPresent());
    }

    @Test
    void springBean_doesNotMatchCleanLine() {
        assertFalse(new SpringBeanDetector().detect(CLEAN_LINE).isPresent());
    }

    // ── LazyInitializationDetector ────────────────────────────────────────────

    @Test
    void lazyInit_detectsLazyInitializationException() {
        IssueDetector detector = new LazyInitializationDetector();
        String line = "org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: com.example.Order.items";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("LazyInitializationException", result.get().getType());
        assertEquals(Severity.ERROR, result.get().getSeverity());
    }

    @Test
    void lazyInit_detectsCouldNotInitializeProxy() {
        IssueDetector detector = new LazyInitializationDetector();
        String line = "org.hibernate.LazyInitializationException: could not initialize proxy - no Session";

        assertTrue(detector.detect(line).isPresent());
    }

    @Test
    void lazyInit_doesNotMatchCleanLine() {
        assertFalse(new LazyInitializationDetector().detect(CLEAN_LINE).isPresent());
    }

    // ── ClassCastDetector ─────────────────────────────────────────────────────

    @Test
    void classCast_detectsClassCastException() {
        IssueDetector detector = new ClassCastDetector();
        String line = "java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Integer";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("ClassCastException", result.get().getType());
        assertEquals(Severity.ERROR, result.get().getSeverity());
    }

    @Test
    void classCast_detectsCannotBeCastTo() {
        IssueDetector detector = new ClassCastDetector();
        String line = "com.example.UserDto cannot be cast to com.example.AdminDto";

        assertTrue(detector.detect(line).isPresent());
    }

    @Test
    void classCast_doesNotMatchCleanLine() {
        assertFalse(new ClassCastDetector().detect(CLEAN_LINE).isPresent());
    }

    // ── StackOverflowDetector ─────────────────────────────────────────────────

    @Test
    void stackOverflow_detectsStackOverflowError() {
        IssueDetector detector = new StackOverflowDetector();
        String line = "java.lang.StackOverflowError";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("StackOverflowError", result.get().getType());
        assertEquals(Severity.CRITICAL, result.get().getSeverity());
    }

    @Test
    void stackOverflow_doesNotMatchCleanLine() {
        assertFalse(new StackOverflowDetector().detect(CLEAN_LINE).isPresent());
    }

    // ── ConcurrentModificationDetector ────────────────────────────────────────

    @Test
    void concurrentModification_detectsException() {
        IssueDetector detector = new ConcurrentModificationDetector();
        String line = "java.util.ConcurrentModificationException";

        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("ConcurrentModificationException", result.get().getType());
        assertEquals(Severity.ERROR, result.get().getSeverity());
    }

    @Test
    void concurrentModification_doesNotMatchCleanLine() {
        assertFalse(new ConcurrentModificationDetector().detect(CLEAN_LINE).isPresent());
    }
}
