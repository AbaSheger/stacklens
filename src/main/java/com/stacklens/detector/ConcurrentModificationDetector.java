package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

/**
 * Detects ConcurrentModificationException in log lines.
 *
 * Matches patterns like:
 *   java.util.ConcurrentModificationException
 *   ConcurrentModificationException
 */
public class ConcurrentModificationDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "ConcurrentModificationException"; }

    @Override
    public Severity getSeverity() { return Severity.ERROR; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        if (line.contains("ConcurrentModificationException")) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "A collection was modified while it was being iterated. This is a fail-fast " +
                "safety mechanism — the iterator detected structural modification mid-iteration.",
                List.of(
                    "Never add or remove elements from a collection inside a for-each loop over it",
                    "Use Iterator.remove() instead of Collection.remove() when removing during iteration",
                    "Collect items to remove first, then remove them after the loop",
                    "Use CopyOnWriteArrayList or ConcurrentHashMap for concurrent access scenarios",
                    "Use removeIf() for bulk removal: list.removeIf(item -> condition)",
                    "Use stream().filter().collect() to produce a new filtered collection instead of mutating"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
