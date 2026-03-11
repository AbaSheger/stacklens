package com.stacklens.detector;

import com.stacklens.model.Issue;

import java.util.List;
import java.util.Optional;

/**
 * Detects "connection refused" errors in log lines.
 *
 * Matches patterns like:
 *   Connection refused
 *   ConnectException: Connection refused
 *   Failed to connect to host
 */
public class ConnectionRefusedDetector implements IssueDetector {

    @Override
    public String getIssueType() {
        return "ConnectionRefused";
    }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) {
            return Optional.empty();
        }

        String lower = line.toLowerCase();

        boolean isConnectionRefused =
            lower.contains("connection refused") ||
            lower.contains("connectexception") ||
            lower.contains("failed to connect") ||
            lower.contains("econnrefused");

        if (isConnectionRefused) {
            Issue issue = new Issue(
                getIssueType(),
                "The connection was actively refused by the target host. " +
                "This means the server is either not running on the expected port, " +
                "or a firewall is blocking access.",
                List.of(
                    "Verify the target service is running on the expected host and port",
                    "Check that the configured URL or hostname is correct",
                    "Ensure there are no firewall or network policies blocking the port",
                    "If connecting to a microservice, verify service discovery is working",
                    "Check if the service crashed and needs to be restarted"
                ),
                line.trim()
            );
            return Optional.of(issue);
        }

        return Optional.empty();
    }
}
