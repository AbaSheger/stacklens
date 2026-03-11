package com.stacklens.detector;

import com.stacklens.model.Issue;

import java.util.Optional;

/**
 * Contract for all issue detectors.
 *
 * Each detector is responsible for one specific type of error.
 * This interface makes it easy to add new detectors without touching
 * existing code (Open/Closed Principle).
 *
 * To add a new detector:
 *   1. Create a class that implements IssueDetector
 *   2. Register it in IssueClassifier
 *   3. Done — no other changes needed
 */
public interface IssueDetector {

    /**
     * Analyzes a single log line and returns an Issue if a problem is detected.
     *
     * @param line a single line from the log or stack trace
     * @return an Optional containing the detected Issue, or empty if no match
     */
    Optional<Issue> detect(String line);

    /**
     * Returns the name of the issue type this detector handles.
     * Used for display and deduplication.
     */
    String getIssueType();
}
