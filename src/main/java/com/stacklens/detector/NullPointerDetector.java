package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

public class NullPointerDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "NullPointerException"; }

    @Override
    public Severity getSeverity() { return Severity.ERROR; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        if (line.contains("NullPointerException")) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "A null object reference was accessed. This happens when code calls a method " +
                "or accesses a field on an object that has not been initialized (is null).",
                List.of(
                    "Ensure all objects are properly initialized before use",
                    "Add null checks before accessing object fields or methods",
                    "Use Optional<T> to express that a value may be absent",
                    "Validate method parameters at the start of each method",
                    "Check if a dependency injection (e.g. @Autowired) is missing or failed"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
