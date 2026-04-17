package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

public class ConnectionRefusedDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "ConnectionRefused"; }

    @Override
    public Severity getSeverity() { return Severity.ERROR; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        String lower = line.toLowerCase();

        boolean match =
            lower.contains("connection refused") ||
            lower.contains("connectexception") ||
            lower.contains("failed to connect") ||
            lower.contains("econnrefused");

        if (match) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "The connection was actively refused by the target host. The server is either " +
                "not running on the expected port, or a firewall is blocking access.",
                List.of(
                    "Verify the target service is running on the expected host and port",
                    "Check that the configured URL or hostname is correct",
                    "Ensure there are no firewall or network policies blocking the port",
                    "If connecting to a microservice, verify service discovery is working",
                    "Check if the service crashed and needs to be restarted"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
