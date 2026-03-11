package com.stacklens.model;

import java.util.List;

/**
 * Holds the full result of analyzing a log file or stack trace text.
 *
 * Contains the source description (file name or "inline text") and
 * all issues that were detected during analysis.
 */
public class AnalysisResult {

    /** Describes where the input came from (file path or "inline text"). */
    private final String source;

    /** All issues detected during analysis. Empty list means no issues found. */
    private final List<Issue> issues;

    public AnalysisResult(String source, List<Issue> issues) {
        this.source = source;
        this.issues = issues;
    }

    public String getSource() {
        return source;
    }

    public List<Issue> getIssues() {
        return issues;
    }

    /** Returns true if at least one issue was detected. */
    public boolean hasIssues() {
        return !issues.isEmpty();
    }
}
