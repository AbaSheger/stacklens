package com.stacklens.JSONlogs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LogProcessorTest {

    private final LogProcessor processor = new LogProcessor();

    @Test
    void preservesStructuredLogContext() {
        String logLine = """
            {"timestamp":"2026-07-03T12:34:56Z","level":"ERROR","message":"Request failed","stack_trace":"java.lang.IllegalStateException: boom"}
            """.trim();

        assertEquals(
            "2026-07-03T12:34:56Z\nERROR\nRequest failed\njava.lang.IllegalStateException: boom",
            processor.parseToPlainText(logLine)
        );
    }

    @Test
    void skipsMissingAndBlankFields() {
        String logLine = """
            {"timestamp":"","level":"WARN","message":"Retrying request"}
            """.trim();

        assertEquals("WARN\nRetrying request", processor.parseToPlainText(logLine));
    }

    @Test
    void returnsOriginalJsonWhenNoUsefulFieldsExist() {
        String logLine = "{\"logger\":\"com.example.Service\"}";

        assertEquals(logLine, processor.parseToPlainText(logLine));
    }
}
