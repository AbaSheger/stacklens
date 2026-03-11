package com.stacklens.detector;

import com.stacklens.model.Issue;

import java.util.List;
import java.util.Optional;

/**
 * Detects timeout errors in log lines.
 *
 * Matches patterns like:
 *   SocketTimeoutException
 *   Read timed out
 *   Connection timed out
 *   Timeout waiting for connection from pool
 *   Gateway Timeout
 */
public class TimeoutDetector implements IssueDetector {

    @Override
    public String getIssueType() {
        return "TimeoutError";
    }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) {
            return Optional.empty();
        }

        String lower = line.toLowerCase();

        boolean isTimeout =
            lower.contains("sockettimeoutexception") ||
            lower.contains("read timed out") ||
            lower.contains("connection timed out") ||
            lower.contains("timeout waiting for connection") ||
            lower.contains("timeoutexception") ||
            lower.contains("gateway timeout") ||
            lower.contains("request timeout") ||
            lower.contains("connect timeout");

        if (isTimeout) {
            Issue issue = new Issue(
                getIssueType(),
                "A timeout occurred while waiting for a response. " +
                "This can happen when a remote service, database, or external API " +
                "takes longer to respond than the configured timeout threshold.",
                List.of(
                    "Increase the timeout value if the operation is expected to take longer",
                    "Investigate why the downstream service is responding slowly",
                    "Add circuit breaker logic (e.g. Resilience4j) to fail fast",
                    "Check for slow database queries that may be causing the delay",
                    "Review connection pool timeout settings (e.g. HikariCP connectionTimeout)",
                    "Ensure external APIs and services are healthy"
                ),
                line.trim()
            );
            return Optional.of(issue);
        }

        return Optional.empty();
    }
}
