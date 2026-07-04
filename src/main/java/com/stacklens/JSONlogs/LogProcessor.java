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

            appendField(plainText, jsonLog.timestamp());
            appendField(plainText, jsonLog.level());
            appendField(plainText, jsonLog.message());
            appendField(plainText, jsonLog.stackTrace());

            return plainText.isEmpty() ? logLine : plainText.toString();
        } catch (Exception e) {
            return logLine;
        }
    }

    private void appendField(StringBuilder plainText, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!plainText.isEmpty()) {
            plainText.append("\n");
        }
        plainText.append(value);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record JsonLogStructure(
        @JsonProperty("timestamp") String timestamp,
        @JsonProperty("level") String level,
        @JsonProperty("message") String message,
        @JsonProperty("stack_trace") String stackTrace
    ) {}
}
