package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

/**
 * Detects StackOverflowError in log lines.
 *
 * Matches patterns like:
 *   java.lang.StackOverflowError
 *   StackOverflowError
 */
public class StackOverflowDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "StackOverflowError"; }

    @Override
    public Severity getSeverity() { return Severity.CRITICAL; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        if (line.contains("StackOverflowError")) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "The call stack exceeded its maximum depth, usually due to infinite or very deep " +
                "recursion. The application thread has crashed.",
                List.of(
                    "Look for recursive method calls that have no base case or a broken termination condition",
                    "Check @ToString / @EqualsAndHashCode on JPA entities — Lombok on bidirectional relations causes this",
                    "In Spring, check for circular bean proxy calls (a bean calling itself through a proxy)",
                    "Increase the stack size with -Xss (e.g. -Xss4m) as a temporary workaround",
                    "Convert deep recursion to an iterative approach using an explicit stack",
                    "Review Jackson serialization of object graphs with cycles — use @JsonIgnore or @JsonManagedReference"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
