package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

public class Http500Detector implements IssueDetector {

    @Override
    public String getIssueType() { return "Http500InternalServerError"; }

    @Override
    public Severity getSeverity() { return Severity.ERROR; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        String lower = line.toLowerCase();

        boolean match =
            lower.contains("500 internal server error") ||
            lower.contains("http 500") ||
            lower.contains("status=500") ||
            lower.contains("status 500") ||
            lower.contains("nestedservletexception") ||
            lower.contains("whitelabel error page") ||
            lower.contains("responded with 500");

        if (match) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "The server returned an HTTP 500 Internal Server Error — an unexpected condition " +
                "prevented the request from being fulfilled.",
                List.of(
                    "Check the full server logs for the underlying exception that caused this",
                    "Look for unhandled exceptions in your @Controller or @Service classes",
                    "Add a global @ExceptionHandler or @ControllerAdvice to handle errors gracefully",
                    "Ensure all service dependencies (DB, cache, external APIs) are healthy",
                    "Review recent deployments for introduced bugs",
                    "Enable detailed error logging to capture the full stack trace"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
