# StackLens Architecture

This document explains the internal design of StackLens for contributors and maintainers.

---

## Design Goals

1. **Simple** — each class does one thing
2. **Extensible** — adding a new detector requires touching only two files
3. **Testable** — all components are independently unit-testable
4. **Beginner-friendly** — no magic frameworks; just plain Java

---

## Component Overview

```
┌─────────────────────────────────────────────────────────────┐
│  CLI Layer (picocli)                                         │
│  Main.java  →  AnalyzeCommand.java                          │
└────────────────────────┬────────────────────────────────────┘
                         │ calls
┌────────────────────────▼────────────────────────────────────┐
│  Analysis Layer                                              │
│  LogAnalyzer — reads files or text, calls IssueClassifier   │
└────────────────────────┬────────────────────────────────────┘
                         │ calls
┌────────────────────────▼────────────────────────────────────┐
│  Classification Layer                                        │
│  IssueClassifier — runs all detectors against each line      │
└────────────────────────┬────────────────────────────────────┘
                         │ calls (many)
┌────────────────────────▼────────────────────────────────────┐
│  Detector Layer (IssueDetector interface)                    │
│  NullPointerDetector, DatabaseConnectionDetector, ...        │
└─────────────────────────────────────────────────────────────┘
                         │ produces
┌─────────────────────────────────────────────────────────────┐
│  Model                                                       │
│  Issue, AnalysisResult                                       │
└────────────────────────┬────────────────────────────────────┘
                         │ consumed by
┌────────────────────────▼────────────────────────────────────┐
│  Output Layer                                                │
│  HumanReadableFormatter, JsonFormatter                       │
└─────────────────────────────────────────────────────────────┘
```

---

## Data Flow

1. User runs `stacklens analyze app.log`
2. `AnalyzeCommand` receives the arguments
3. `LogAnalyzer.analyzeFile(path)` reads the file into a list of lines
4. `IssueClassifier.classify(lines)` iterates over every line and runs each `IssueDetector`
5. Matching detectors return an `Issue` object
6. `IssueClassifier` deduplicates by issue type and returns a list of unique issues
7. `AnalysisResult` wraps the source and issue list
8. `HumanReadableFormatter` (or `JsonFormatter`) converts the result to a string
9. The string is printed to stdout

---

## The IssueDetector Interface

```java
public interface IssueDetector {
    Optional<Issue> detect(String line);
    String getIssueType();
}
```

Each detector:
- Gets called with one log line at a time
- Returns `Optional.empty()` if no match
- Returns `Optional.of(issue)` when a match is found
- Is stateless (safe to reuse across threads)

---

## Adding New Detectors

See [CONTRIBUTING.md](../CONTRIBUTING.md).

---

## Future Extension Points

### AI Explanations

The `IssueDetector` interface can be extended with an `explain(Issue)` method or wrapped in an `AiEnhancedDetector` decorator that calls an LLM API to generate context-aware explanations.

### Plugin System

Detectors could be loaded from external JARs via Java's `ServiceLoader` mechanism, allowing third-party plugins without recompiling StackLens.

### Kubernetes Log Support

A `KubernetesLogParser` could pre-process `kubectl logs` output to extract pod name, namespace, and container metadata before passing lines to the existing classifier.
