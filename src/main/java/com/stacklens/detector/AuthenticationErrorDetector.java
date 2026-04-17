package com.stacklens.detector;

import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;
import java.util.Optional;

public class AuthenticationErrorDetector implements IssueDetector {

    @Override
    public String getIssueType() { return "AuthenticationError"; }

    @Override
    public Severity getSeverity() { return Severity.WARNING; }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        String lower = line.toLowerCase();

        boolean match =
            lower.contains("401") && lower.contains("unauthorized") ||
            lower.contains("403") && lower.contains("forbidden") ||
            lower.contains("access denied") ||
            lower.contains("access is denied") ||
            lower.contains("authenticationexception") ||
            lower.contains("bad credentials") ||
            lower.contains("jwt expired") ||
            lower.contains("token expired") ||
            lower.contains("invalid token") ||
            lower.contains("authorizationdeniedexception");

        if (match) {
            return Optional.of(new Issue(
                getIssueType(),
                getSeverity(),
                "An authentication or authorization failure occurred. A request was rejected " +
                "because the user or service was not authenticated, or lacked the required permissions.",
                List.of(
                    "Verify the API key, token, or credentials are correct and not expired",
                    "Check that the JWT token is being passed in the Authorization header",
                    "Ensure the user has the correct roles or permissions for this resource",
                    "Review Spring Security configuration for endpoint access rules",
                    "If using OAuth2, check that the token scopes match what the endpoint requires",
                    "Verify clock synchronization if JWTs are expiring unexpectedly"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
