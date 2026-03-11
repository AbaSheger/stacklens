package com.stacklens.detector;

import com.stacklens.model.Issue;

import java.util.List;
import java.util.Optional;

/**
 * Detects database connection failures in log lines.
 *
 * Matches patterns like:
 *   Unable to acquire JDBC Connection
 *   Connection refused (database port)
 *   Communications link failure
 *   could not connect to server
 *   No suitable driver found
 */
public class DatabaseConnectionDetector implements IssueDetector {

    @Override
    public String getIssueType() {
        return "DatabaseConnectionFailure";
    }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) {
            return Optional.empty();
        }

        String lower = line.toLowerCase();

        boolean isDatabaseError =
            lower.contains("unable to acquire jdbc connection") ||
            lower.contains("communications link failure") ||
            lower.contains("could not connect to server") ||
            lower.contains("no suitable driver found") ||
            lower.contains("datasource") && lower.contains("connection refused") ||
            lower.contains("hikari") && lower.contains("connection") ||
            lower.contains("connection pool exhausted") ||
            lower.contains("sqlexception") && lower.contains("connection");

        if (isDatabaseError) {
            Issue issue = new Issue(
                getIssueType(),
                "The application failed to establish a connection to the database. " +
                "This typically means the database server is unreachable, credentials " +
                "are wrong, or the connection pool is exhausted.",
                List.of(
                    "Verify the database host, port, and credentials in your configuration",
                    "Ensure the database server is running and reachable from this host",
                    "Check firewall rules that may be blocking the database port",
                    "Review HikariCP or connection pool settings (maximumPoolSize, connectionTimeout)",
                    "Look for long-running queries that may be holding connections open",
                    "Check if the database has reached its maximum connection limit"
                ),
                line.trim()
            );
            return Optional.of(issue);
        }

        return Optional.empty();
    }
}
