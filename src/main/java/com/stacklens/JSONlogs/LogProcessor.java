package com.stacklens.JSONlogs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LogProcessor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public boolean isJsonLog(String logLine) {
        if (logLine == null) return false;
        String trimmed = logLine.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}");
    }

    public String parseToPlainText(String logLine) {
        try {
            JsonLogStructure jsonLog = objectMapper.readValue(logLine, JsonLogStructure.class);
            StringBuilder plainText = new StringBuilder();
            
            if (jsonLog.message() != null) {
                plainText.append(jsonLog.message());
            }
            if (jsonLog.stackTrace() != null && !jsonLog.stackTrace().isEmpty()) {
                plainText.append("\n").append(jsonLog.stackTrace());
            }
            return plainText.toString();
        } catch (Exception e) {
            return logLine;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record JsonLogStructure(
        @JsonProperty("message") String message,
        @JsonProperty("stack_trace") String stackTrace
    ) {}
}