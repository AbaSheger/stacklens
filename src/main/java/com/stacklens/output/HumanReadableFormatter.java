package com.stacklens.output;

import com.stacklens.model.AnalysisResult;
import com.stacklens.model.Issue;
import com.stacklens.model.Severity;

import java.util.List;

/**
 * Formats analysis results as human-readable terminal output.
 */
public class HumanReadableFormatter {

    private static final String BOLD   = "\033[1m";
    private static final String RED    = "\033[31m";
    private static final String YELLOW = "\033[33m";
    private static final String GREEN  = "\033[32m";
    private static final String CYAN   = "\033[36m";
    private static final String MAGENTA = "\033[35m";
    private static final String DIM    = "\033[2m";
    private static final String RESET  = "\033[0m";

    private final boolean useColors;

    public HumanReadableFormatter(boolean useColors) {
        this.useColors = useColors;
    }

    public String format(AnalysisResult result) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append(bold("StackLens Analysis Report"));
        sb.append("\n");
        sb.append(bold("Source: ")).append(result.getSource());
        sb.append("\n");
        sb.append("─".repeat(60)).append("\n");

        if (!result.hasIssues()) {
            sb.append(green("\n✓ No known issues detected in the provided log.\n"));
            sb.append("  The log looks clean, but always review warnings manually.\n");
            return sb.toString();
        }

        long criticalCount = result.getIssues().stream().filter(i -> i.getSeverity() == Severity.CRITICAL).count();
        long errorCount    = result.getIssues().stream().filter(i -> i.getSeverity() == Severity.ERROR).count();
        long warningCount  = result.getIssues().stream().filter(i -> i.getSeverity() == Severity.WARNING).count();
        int totalOccurrences = result.getIssues().stream().mapToInt(Issue::getOccurrenceCount).sum();

        sb.append("\n");
        sb.append(red("✗ " + result.getIssues().size() + " issue type(s) detected"));
        sb.append(dim(" (" + totalOccurrences + " total occurrence(s))"));
        sb.append("\n");

        if (criticalCount > 0) sb.append(red("  " + criticalCount + " CRITICAL"));
        if (errorCount > 0)    sb.append("  " + errorCount + " ERROR");
        if (warningCount > 0)  sb.append(yellow("  " + warningCount + " WARNING"));
        sb.append("\n");

        int issueNumber = 1;
        for (Issue issue : result.getIssues()) {
            formatIssue(sb, issue, issueNumber++);
        }

        return sb.toString();
    }

    /**
     * Formats a compact summary table — one line per issue.
     * Used by the --summary flag.
     */
    public String formatSummary(AnalysisResult result) {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append(bold("StackLens Summary"));
        sb.append(" — ").append(bold("Source: ")).append(result.getSource());
        sb.append("\n");
        sb.append("─".repeat(70)).append("\n\n");

        if (!result.hasIssues()) {
            sb.append(green("✓ No known issues detected.\n"));
            return sb.toString();
        }

        int totalOccurrences = result.getIssues().stream().mapToInt(Issue::getOccurrenceCount).sum();
        sb.append(bold(result.getIssues().size() + " issue type(s)"))
          .append("  •  ")
          .append(bold(totalOccurrences + " total occurrence(s)"))
          .append("\n\n");

        for (Issue issue : result.getIssues()) {
            String badge = severityBadge(issue.getSeverity());
            String count = dim("×" + issue.getOccurrenceCount());
            String loc   = issue.getLocation() != null ? cyan(issue.getLocation()) : dim("—");

            sb.append(String.format("%-14s %-35s %s   %s%n",
                badge,
                bold(issue.getType()),
                count,
                loc));
        }

        sb.append("\n");
        return sb.toString();
    }

    private void formatIssue(StringBuilder sb, Issue issue, int number) {
        sb.append("\n");
        sb.append("─".repeat(60)).append("\n");
        sb.append(bold(severityBadge(issue.getSeverity()) + "  Issue #" + number + ": " + issue.getType()));

        if (issue.getOccurrenceCount() > 1) {
            sb.append(dim("  ×" + issue.getOccurrenceCount()));
        }
        sb.append("\n\n");

        if (issue.getLocation() != null) {
            sb.append(bold("Location:")).append("\n");
            sb.append("  ").append(cyan(issue.getLocation())).append("\n\n");
        }

        sb.append(bold("Detected in:")).append("\n");
        sb.append("  ").append(dim(truncate(issue.getMatchedLine(), 120))).append("\n");

        if (!issue.getStackContext().isEmpty()) {
            List<String> frames = issue.getStackContext();
            // Show up to 5 frames — enough to understand the call path
            int shown = Math.min(5, frames.size());
            for (int i = 0; i < shown; i++) {
                sb.append("  ").append(dim(frames.get(i))).append("\n");
            }
            if (frames.size() > shown) {
                sb.append("  ").append(dim("... " + (frames.size() - shown) + " more frame(s)")).append("\n");
            }
        }

        sb.append("\n");
        sb.append(bold("Explanation:")).append("\n");
        for (String line : wordWrap(issue.getExplanation(), 70)) {
            sb.append("  ").append(line).append("\n");
        }
        sb.append("\n");

        sb.append(bold("Suggested fixes:")).append("\n");
        int i = 1;
        for (String suggestion : issue.getSuggestions()) {
            sb.append("  ").append(i++).append(". ").append(suggestion).append("\n");
        }
        sb.append("\n");
    }

    private String severityBadge(Severity severity) {
        return switch (severity) {
            case CRITICAL -> red("[CRITICAL]");
            case ERROR    -> "[ERROR]";
            case WARNING  -> yellow("[WARNING]");
        };
    }

    private String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private List<String> wordWrap(String text, int maxWidth) {
        List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (current.length() + word.length() + 1 > maxWidth) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                if (current.length() > 0) current.append(" ");
                current.append(word);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    private String bold(String text)    { return useColors ? BOLD + text + RESET : text; }
    private String red(String text)     { return useColors ? RED + text + RESET : text; }
    private String yellow(String text)  { return useColors ? YELLOW + text + RESET : text; }
    private String green(String text)   { return useColors ? GREEN + text + RESET : text; }
    private String cyan(String text)    { return useColors ? CYAN + text + RESET : text; }
    private String dim(String text)     { return useColors ? DIM + text + RESET : text; }
}
