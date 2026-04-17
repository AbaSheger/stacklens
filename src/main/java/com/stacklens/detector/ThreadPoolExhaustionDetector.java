package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

public class ThreadPoolExhaustionDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "ThreadPoolExhaustion"; }

    @Override
    public Severity getSeverity() { return Severity.CRITICAL; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        String lower = line.toLowerCase();

        boolean match =
            lower.contains("rejectedexecutionexception") ||
            lower.contains("task rejected") ||
            lower.contains("thread pool") && lower.contains("full") ||
            lower.contains("unable to create new native thread") ||
            lower.contains("threadpoolexecutor") && lower.contains("rejected") ||
            lower.contains("no thread available");

        if (match) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "The thread pool is exhausted and cannot accept new tasks. All threads are busy " +
                "and the task queue is full, often caused by slow downstream services or traffic spikes.",
                List.of(
                    "Increase the thread pool size (corePoolSize, maxPoolSize) if resources allow",
                    "Increase the task queue capacity to buffer more requests",
                    "Investigate what is causing threads to be blocked (slow DB, external API)",
                    "Add timeouts to prevent threads from waiting indefinitely",
                    "Implement backpressure or rate limiting to reduce incoming load",
                    "Consider using a reactive (non-blocking) approach with WebFlux for I/O-bound work"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
