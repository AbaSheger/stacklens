package com.stacklens.analyzer;

import com.stacklens.JSONlogs.LogProcessor; 
import com.stacklens.model.AnalysisResult;
import com.stacklens.classifier.IssueClassifier;

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

public class LogAnalyzer {

    private final IssueClassifier classifier;
    private final LogProcessor logProcessor; 

    public LogAnalyzer() {
        this.classifier = new IssueClassifier();
        this.logProcessor = new LogProcessor(); 
    }

    LogAnalyzer(IssueClassifier classifier) {
        this.classifier = classifier;
        this.logProcessor = new LogProcessor();
    }

    private List<String> preprocessLines(List<String> lines) {
        return lines.stream()
                .map(line -> {
                    if (logProcessor.isJsonLog(line)) {
                        return logProcessor.parseToPlainText(line);
                    }
                    return line; 
                }).flatMap(line -> Arrays.stream(line.split("\\r?\\n")))
                .collect(Collectors.toList());
    }

    public AnalysisResult analyzeFile(Path filePath) throws IOException {
        List<String> lines = Files.readAllLines(filePath);
        List<String> processedLines = preprocessLines(lines); 
        return new AnalysisResult(filePath.toString(), classifier.classify(processedLines));
    }

    public AnalysisResult analyzeStream(InputStream stream, String sourceLabel) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            List<String> processedLines = preprocessLines(lines);
            return new AnalysisResult(sourceLabel, classifier.classify(processedLines));
        }
    }

    public AnalysisResult analyzeText(String text) {
        List<String> lines = Arrays.asList(text.split("\\r?\\n"));
        List<String> processedLines = preprocessLines(lines);
        return new AnalysisResult("inline text", classifier.classify(processedLines));
    }
}