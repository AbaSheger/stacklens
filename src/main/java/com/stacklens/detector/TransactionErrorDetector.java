package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

public class TransactionErrorDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "TransactionError"; }

    @Override
    public Severity getSeverity() { return Severity.ERROR; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        String lower = line.toLowerCase();

        boolean match =
            lower.contains("transactionsystemexception") ||
            lower.contains("rollbackexception") ||
            lower.contains("cannotcreatetransactionexception") ||
            lower.contains("could not execute statement");

        if (match) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "A Spring-managed transaction failed and was rolled back. " +
                "This often indicates a persistence error, constraint violation, or another exception inside a transactional operation.",
                List.of(
                    "Inspect the root cause and any nested exception to find the original database or validation failure",
                    "Check for constraint violations, deadlocks, or invalid entity state in the failing transaction",
                    "Review the @Transactional boundary to confirm rollback behavior and exception handling are correct",
                    "Verify the database logs for the matching failed statement or rollback event",
                    "Consider idempotent retry handling only for transient failures such as deadlocks or lock timeouts"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
