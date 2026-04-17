package com.stacklens.model;

/**
 * Severity level for a detected issue.
 *
 * CRITICAL - Application is likely down or about to crash (OOM, thread pool exhausted, stack overflow)
 * ERROR    - Functional failure affecting users (NPE, DB connection loss, ClassCast)
 * WARNING  - Degraded but may recover (timeout, auth failure - could be transient)
 */
public enum Severity {
    CRITICAL,
    ERROR,
    WARNING;

    public String label() {
        return name();
    }
}
