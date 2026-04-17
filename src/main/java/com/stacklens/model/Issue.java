package com.stacklens.model;

import java.util.List;

/**
 * Represents a detected issue found in a log or stack trace.
 *
 * Carries both the static analysis (type, explanation, suggestions) set by the
 * detector and the runtime context (occurrence count, stack trace frames,
 * application-level location) added by the classifier.
 */
public class Issue {

    /** A short, machine-friendly name for the issue type (e.g., "NullPointerException"). */
    private final String type;

    /** How severe this issue is. */
    private final Severity severity;

    /** A human-readable explanation of what the issue means. */
    private final String explanation;

    /** A list of actionable suggestions to help the developer fix the issue. */
    private final List<String> suggestions;

    /** The original log line or snippet where the issue was first detected. */
    private final String matchedLine;

    /** How many times this issue type appeared in the log. */
    private final int occurrenceCount;

    /**
     * Stack trace lines collected after the matched line (up to ~15 frames).
     * Empty when no stack trace follows the match.
     */
    private final List<String> stackContext;

    /**
     * The first application-owned stack frame, e.g. "OrderService.process(OrderService.java:142)".
     * Null when no app frame could be identified (e.g. the issue has no stack trace).
     */
    private final String location;

    /** Full constructor used by the classifier when building enriched issues. */
    public Issue(String type, Severity severity, String explanation, List<String> suggestions,
                 String matchedLine, int occurrenceCount, List<String> stackContext, String location) {
        this.type = type;
        this.severity = severity;
        this.explanation = explanation;
        this.suggestions = suggestions;
        this.matchedLine = matchedLine;
        this.occurrenceCount = occurrenceCount;
        this.stackContext = stackContext != null ? stackContext : List.of();
        this.location = location;
    }

    /** Convenience constructor for detectors — count/context/location filled in by the classifier. */
    public Issue(String type, Severity severity, String explanation, List<String> suggestions, String matchedLine) {
        this(type, severity, explanation, suggestions, matchedLine, 1, List.of(), null);
    }

    public String getType()             { return type; }
    public Severity getSeverity()       { return severity; }
    public String getExplanation()      { return explanation; }
    public List<String> getSuggestions(){ return suggestions; }
    public String getMatchedLine()      { return matchedLine; }
    public int getOccurrenceCount()     { return occurrenceCount; }
    public List<String> getStackContext(){ return stackContext; }
    public String getLocation()         { return location; }

    @Override
    public String toString() {
        return "Issue{type='" + type + "', severity=" + severity
                + ", count=" + occurrenceCount + ", location='" + location + "'}";
    }
}
