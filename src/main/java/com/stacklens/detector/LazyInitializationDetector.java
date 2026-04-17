package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

/**
 * Detects Hibernate LazyInitializationException — one of the most common
 * errors in Spring + JPA applications.
 *
 * Matches patterns like:
 *   LazyInitializationException
 *   could not initialize proxy - no Session
 *   failed to lazily initialize a collection
 */
public class LazyInitializationDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "LazyInitializationException"; }

    @Override
    public Severity getSeverity() { return Severity.ERROR; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        String lower = line.toLowerCase();

        boolean match =
            lower.contains("lazyinitializationexception") ||
            lower.contains("could not initialize proxy") ||
            lower.contains("failed to lazily initialize a collection") ||
            lower.contains("no session") && lower.contains("proxy") ||
            lower.contains("session was already closed");

        if (match) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "A Hibernate lazy-loaded association was accessed outside of an active database " +
                "session. This typically happens when accessing a lazy collection or proxy after " +
                "the transaction has already closed.",
                List.of(
                    "Add @Transactional to the service method that accesses the lazy association",
                    "Use fetch joins in your JPQL/Criteria query: JOIN FETCH entity.collection",
                    "Switch the problematic field to FetchType.EAGER if always needed (consider N+1 impact)",
                    "Use a DTO projection to load only the data you need within the transaction",
                    "Enable spring.jpa.open-in-view=false and fix the root cause rather than masking it",
                    "Use Hibernate.initialize() to force-load the association while the session is open"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
