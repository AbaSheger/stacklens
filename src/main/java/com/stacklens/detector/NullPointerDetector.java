package com.stacklens.detector;

import com.stacklens.model.Issue;

import java.util.List;
import java.util.Optional;

/**
 * Detects NullPointerException in log lines.
 *
 * Matches patterns like:
 *   java.lang.NullPointerException
 *   NullPointerException at SomeClass
 */
public class NullPointerDetector implements IssueDetector {

    @Override
    public String getIssueType() {
        return "NullPointerException";
    }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) {
            return Optional.empty();
        }

        if (line.contains("NullPointerException")) {
            Issue issue = new Issue(
                getIssueType(),
                "A null object reference was accessed in the application. " +
                "This happens when code tries to call a method or access a field " +
                "on an object that has not been initialized (is null).",
                List.of(
                    "Ensure all objects are properly initialized before use",
                    "Add null checks before accessing object fields or methods",
                    "Use Optional<T> to express that a value may be absent",
                    "Validate method parameters at the start of each method",
                    "Check if a dependency injection (e.g. @Autowired) is missing"
                ),
                line.trim()
            );
            return Optional.of(issue);
        }

        return Optional.empty();
    }
}
