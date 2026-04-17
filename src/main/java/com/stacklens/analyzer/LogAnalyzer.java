package com.stacklens.analyzer;

import com.stacklens.classifier.IssueClassifier;
import com.stacklens.model.AnalysisResult;
import com.stacklens.model.Issue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads log content (from file, stdin, or inline text) and coordinates analysis.
 */
public class LogAnalyzer {

    private final IssueClassifier classifier;

    public LogAnalyzer() {
        this.classifier = new IssueClassifier();
    }

    LogAnalyzer(IssueClassifier classifier) {
        this.classifier = classifier;
    }

    /** Reads a log file from disk and analyzes its contents. */
    public AnalysisResult analyzeFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        return new AnalysisResult(filePath.toString(), classifier.classify(lines));
    }

    /** Reads from an InputStream (e.g. System.in when using stdin mode). */
    public AnalysisResult analyzeStream(InputStream stream, String sourceLabel) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            return new AnalysisResult(sourceLabel, classifier.classify(lines));
        }
    }

    /** Analyzes a stack trace or log text pasted directly as a string. */
    public AnalysisResult analyzeText(String text) {
        List<String> lines = Arrays.asList(text.split("\\r?\\n"));
        return new AnalysisResult("inline text", classifier.classify(lines));
    }
}
