package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

/**
 * Detects ClassCastException in log lines.
 *
 * Matches patterns like:
 *   java.lang.ClassCastException
 *   cannot be cast to
 */
public class ClassCastDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "ClassCastException"; }

    @Override
    public Severity getSeverity() { return Severity.ERROR; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        boolean match =
            line.contains("ClassCastException") ||
            line.contains("cannot be cast to") ||
            line.contains("cannot cast");

        if (match) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "An object was cast to an incompatible type at runtime. The actual type of the " +
                "object does not match the type it is being cast to.",
                List.of(
                    "Use instanceof before casting: if (obj instanceof MyType t) { ... }",
                    "Check if generics are being erased — raw types often cause this at runtime",
                    "Verify that the object is actually of the expected type at the point of the cast",
                    "If deserializing JSON/XML, ensure the target class matches the payload structure",
                    "In Spring, check if a bean is being injected as the wrong interface or class",
                    "Look for places where Object or a parent type is returned and blindly cast"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
