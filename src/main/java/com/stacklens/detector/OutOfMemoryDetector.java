package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

public class OutOfMemoryDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "OutOfMemoryError"; }

    @Override
    public Severity getSeverity() { return Severity.CRITICAL; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        String lower = line.toLowerCase();

        boolean match =
            lower.contains("outofmemoryerror") ||
            lower.contains("java heap space") ||
            lower.contains("gc overhead limit exceeded") ||
            lower.contains("metaspace") && lower.contains("error") ||
            lower.contains("permgen space");

        if (match) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "The JVM ran out of memory. This can be caused by memory leaks, " +
                "processing large datasets in memory, or insufficient heap configuration.",
                List.of(
                    "Increase the JVM heap size with -Xmx (e.g. -Xmx2g for 2GB)",
                    "Profile with VisualVM or JProfiler to find memory leaks",
                    "Check for collections that grow unboundedly (caches without eviction)",
                    "Use streaming or pagination instead of loading large datasets into memory",
                    "Review ThreadLocal usage — improperly cleared ThreadLocals cause leaks",
                    "Enable GC logging (-Xlog:gc) to understand garbage collection behavior"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
