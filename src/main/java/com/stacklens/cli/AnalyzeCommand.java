package com.stacklens.cli;

import com.stacklens.analyzer.LogAnalyzer;
import com.stacklens.model.AnalysisResult;
import com.stacklens.output.HumanReadableFormatter;
import com.stacklens.output.JsonFormatter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * The "analyze" subcommand — the main command users interact with.
 *
 * Usage examples:
 *   stacklens analyze app.log
 *   stacklens analyze app.log --output json
 *   stacklens analyze app.log --summary
 *   stacklens analyze --text "java.lang.NullPointerException at OrderService"
 *   kubectl logs my-pod | stacklens analyze -
 */
@Command(
    name = "analyze",
    description = "Analyze a log file or stack trace and explain detected errors.",
    mixinStandardHelpOptions = true
)
public class AnalyzeCommand implements Callable<Integer> {

    /**
     * Path to the log file to analyze.
     * Use "-" to read from stdin (e.g. piped from kubectl logs or docker logs).
     */
    @Parameters(
        index = "0",
        description = "Path to the log file to analyze, or '-' to read from stdin.",
        arity = "0..1"
    )
    private String logFilePath;

    /** Inline text mode: pass a stack trace or log snippet directly. */
    @Option(
        names = {"--text", "-t"},
        description = "Analyze inline text (stack trace or log snippet) instead of a file."
    )
    private String inlineText;

    /**
     * Output format. Defaults to "human". Use "json" for machine-readable output.
     */
    @Option(
        names = {"--output", "-o"},
        description = "Output format: human (default) or json.",
        defaultValue = "human"
    )
    private String outputFormat;

    /**
     * Summary mode: show a compact one-line-per-issue table with counts and locations.
     * Mutually exclusive with --output json.
     */
    @Option(
        names = {"--summary", "-s"},
        description = "Show a compact summary table instead of full issue details.",
        defaultValue = "false"
    )
    private boolean summary;

    /** Disable ANSI color codes in output — useful in CI or when writing to a log file. */
    @Option(
        names = {"--no-color"},
        description = "Disable ANSI color codes in output.",
        defaultValue = "false"
    )
    private boolean noColor;

    @Override
    public Integer call() {
        boolean isStdin = "-".equals(logFilePath);

        if (logFilePath == null && inlineText == null) {
            System.err.println("Error: Provide a log file path, '-' for stdin, or use --text.");
            System.err.println("Examples:");
            System.err.println("  stacklens analyze app.log");
            System.err.println("  stacklens analyze --text \"NullPointerException at OrderService\"");
            System.err.println("  kubectl logs my-pod | stacklens analyze -");
            return 1;
        }

        if (logFilePath != null && inlineText != null) {
            System.err.println("Error: Provide either a file path or --text, not both.");
            return 1;
        }

        try {
            LogAnalyzer analyzer = new LogAnalyzer();
            AnalysisResult result;

            if (isStdin) {
                result = analyzer.analyzeStream(System.in, "stdin");
            } else if (logFilePath != null) {
                result = analyzer.analyzeFile(Path.of(logFilePath));
            } else {
                result = analyzer.analyzeText(inlineText);
            }

            System.out.println(formatResult(result));

            return result.hasIssues() ? 2 : 0;

        } catch (java.nio.file.NoSuchFileException e) {
            System.err.println("Error: File not found: " + e.getMessage());
            return 1;
        } catch (java.io.IOException e) {
            System.err.println("Error reading input: " + e.getMessage());
            return 1;
        }
    }

    private String formatResult(AnalysisResult result) {
        if ("json".equalsIgnoreCase(outputFormat)) {
            return new JsonFormatter().format(result);
        }
        boolean useColors = !noColor;
        HumanReadableFormatter formatter = new HumanReadableFormatter(useColors);
        return summary ? formatter.formatSummary(result) : formatter.format(result);
    }
}
