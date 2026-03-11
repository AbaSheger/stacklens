package com.stacklens.detector;

import com.stacklens.model.Issue;

import java.util.List;
import java.util.Optional;

/**
 * Detects HTTP 500 Internal Server Error in log lines.
 *
 * Matches patterns like:
 *   HTTP 500
 *   500 Internal Server Error
 *   Resolved [org.springframework.web.util.NestedServletException]
 *   ERROR ... 500
 */
public class Http500Detector implements IssueDetector {

    @Override
    public String getIssueType() {
        return "Http500InternalServerError";
    }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) {
            return Optional.empty();
        }

        String lower = line.toLowerCase();

        boolean isHttp500 =
            lower.contains("500 internal server error") ||
            lower.contains("http 500") ||
            lower.contains("status=500") ||
            lower.contains("status 500") ||
            lower.contains("nestedservletexception") ||
            lower.contains("whitelabel error page") ||
            lower.contains("responded with 500");

        if (isHttp500) {
            Issue issue = new Issue(
                getIssueType(),
                "The server returned an HTTP 500 Internal Server Error. " +
                "This is a generic server-side error indicating an unexpected condition " +
                "that prevented the request from being fulfilled.",
                List.of(
                    "Check the full server logs for the underlying exception that caused this",
                    "Look for unhandled exceptions in your @Controller or @Service classes",
                    "Add a global @ExceptionHandler or @ControllerAdvice to handle errors gracefully",
                    "Ensure all service dependencies (DB, cache, external APIs) are healthy",
                    "Review recent deployments for introduced bugs",
                    "Enable detailed error logging to capture the full stack trace"
                ),
                line.trim()
            );
            return Optional.of(issue);
        }

        return Optional.empty();
    }
}
