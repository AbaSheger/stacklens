package com.stacklens.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.stacklens.model.AnalysisResult;
import com.stacklens.model.Issue;

import java.util.List;

/**
 * Formats analysis results as JSON.
 *
 * Example output:
 * {
 *   "source": "app.log",
 *   "issueCount": 2,
 *   "totalOccurrences": 55,
 *   "issues": [
 *     {
 *       "issue": "NullPointerException",
 *       "severity": "ERROR",
 *       "occurrences": 47,
 *       "location": "OrderService.processOrder(OrderService.java:142)",
 *       "matchedLine": "java.lang.NullPointerException: ...",
 *       "stackContext": ["at com.example...", ...],
 *       "explanation": "...",
 *       "suggestions": ["..."]
 *     }
 *   ]
 * }
 */
public class JsonFormatter {

    private final ObjectMapper objectMapper;

    public JsonFormatter() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Omit null fields (e.g. location when not found)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public String format(AnalysisResult result) {
        try {
            return objectMapper.writeValueAsString(buildJsonOutput(result));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize result to JSON", e);
        }
    }

    private JsonOutput buildJsonOutput(AnalysisResult result) {
        int totalOccurrences = result.getIssues().stream()
            .mapToInt(Issue::getOccurrenceCount)
            .sum();

        List<JsonIssue> jsonIssues = result.getIssues().stream()
            .map(this::toJsonIssue)
            .toList();

        return new JsonOutput(result.getSource(), jsonIssues.size(), totalOccurrences, jsonIssues);
    }

    private JsonIssue toJsonIssue(Issue issue) {
        return new JsonIssue(
            issue.getType(),
            issue.getSeverity().name(),
            issue.getOccurrenceCount(),
            issue.getLocation(),
            issue.getMatchedLine(),
            issue.getStackContext().isEmpty() ? null : issue.getStackContext(),
            issue.getExplanation(),
            issue.getSuggestions()
        );
    }

    record JsonOutput(
        @JsonProperty("source")           String source,
        @JsonProperty("issueCount")        int issueCount,
        @JsonProperty("totalOccurrences")  int totalOccurrences,
        @JsonProperty("issues")            List<JsonIssue> issues
    ) {}

    record JsonIssue(
        @JsonProperty("issue")         String issue,
        @JsonProperty("severity")      String severity,
        @JsonProperty("occurrences")   int occurrences,
        @JsonProperty("location")      String location,
        @JsonProperty("matchedLine")   String matchedLine,
        @JsonProperty("stackContext")  List<String> stackContext,
        @JsonProperty("explanation")   String explanation,
        @JsonProperty("suggestions")   List<String> suggestions
    ) {}
}
