package com.stacklens.detector;

import com.stacklens.model.Issue;

import java.util.List;
import java.util.Optional;

/**
 * Detects OutOfMemoryError in log lines.
 *
 * Matches patterns like:
 *   java.lang.OutOfMemoryError
 *   OutOfMemoryError: Java heap space
 *   OutOfMemoryError: GC overhead limit exceeded
 *   OutOfMemoryError: Metaspace
 */
public class OutOfMemoryDetector implements IssueDetector {

    @Override
    public String getIssueType() {
        return "OutOfMemoryError";
    }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) {
            return Optional.empty();
        }

        String lower = line.toLowerCase();

        boolean isOOM =
            lower.contains("outofmemoryerror") ||
            lower.contains("java heap space") ||
            lower.contains("gc overhead limit exceeded") ||
            lower.contains("metaspace") && lower.contains("error") ||
            lower.contains("permgen space");

        if (isOOM) {
            Issue issue = new Issue(
                getIssueType(),
                "The JVM ran out of memory and could not allocate more heap space. " +
                "This can be caused by memory leaks, processing large datasets in memory, " +
                "or insufficient heap configuration.",
                List.of(
                    "Increase the JVM heap size with -Xmx (e.g. -Xmx2g for 2GB)",
                    "Profile the application with tools like VisualVM or JProfiler to find memory leaks",
                    "Check for collections that grow unboundedly (e.g. caches without eviction)",
                    "Use streaming or pagination instead of loading large datasets into memory",
                    "Review ThreadLocal usage — improperly cleared ThreadLocals can cause leaks",
                    "Enable GC logging (-Xlog:gc) to understand garbage collection behavior",
                    "Consider using a memory-efficient data structure"
                ),
                line.trim()
            );
            return Optional.of(issue);
        }

        return Optional.empty();
    }
}
