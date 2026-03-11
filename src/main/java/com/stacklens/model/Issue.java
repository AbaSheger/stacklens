package com.stacklens.model;

import java.util.List;

/**
 * Represents a detected issue found in a log or stack trace.
 *
 * This is the central data object passed between the detector,
 * classifier, and output components.
 */
public class Issue {

    /** A short, machine-friendly name for the issue type (e.g., "NullPointerException"). */
    private final String type;

    /** A human-readable explanation of what the issue means. */
    private final String explanation;

    /** A list of actionable suggestions to help the developer fix the issue. */
    private final List<String> suggestions;

    /** The original log line or snippet where the issue was detected. */
    private final String matchedLine;

    public Issue(String type, String explanation, List<String> suggestions, String matchedLine) {
        this.type = type;
        this.explanation = explanation;
        this.suggestions = suggestions;
        this.matchedLine = matchedLine;
    }

    public String getType() {
        return type;
    }

    public String getExplanation() {
        return explanation;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public String getMatchedLine() {
        return matchedLine;
    }

    @Override
    public String toString() {
        return "Issue{type='" + type + "', matchedLine='" + matchedLine + "'}";
    }
}
