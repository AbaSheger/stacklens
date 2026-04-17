package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

/**
 * Detects Spring application context / dependency injection failures.
 *
 * Matches patterns like:
 *   NoSuchBeanDefinitionException
 *   BeanCreationException
 *   UnsatisfiedDependencyException
 *   Error creating bean with name
 *   ApplicationContext failed to load
 */
public class SpringBeanDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "SpringBeanFailure"; }

    @Override
    public Severity getSeverity() { return Severity.CRITICAL; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        String lower = line.toLowerCase();

        boolean match =
            lower.contains("nosuchbeandefinitionexception") ||
            lower.contains("beancreationexception") ||
            lower.contains("unsatisfieddependencyexception") ||
            lower.contains("error creating bean with name") ||
            lower.contains("applicationcontext") && lower.contains("failed to load") ||
            lower.contains("circular dependency") ||
            lower.contains("the dependencies of some of the beans in the application context form a cycle");

        if (match) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "Spring failed to start or wire the application context. A bean could not be " +
                "created, a dependency is missing, or there is a circular dependency between beans.",
                List.of(
                    "Check the full startup log — the root cause is usually a few lines below this one",
                    "Ensure all @Component / @Service / @Repository classes are in a package scanned by @SpringBootApplication",
                    "If a NoSuchBeanDefinitionException, verify the bean type matches exactly (generics count)",
                    "For circular dependencies, introduce @Lazy on one injection point or refactor to break the cycle",
                    "If using @ConfigurationProperties, verify the property prefix matches application.yml / .properties",
                    "Check that required environment variables or config values are present at startup"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
