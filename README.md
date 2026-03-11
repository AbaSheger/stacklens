# StackLens

**A CLI developer tool that analyzes Java and Spring Boot logs or stack traces and explains the root cause of errors — with suggested fixes.**

[![CI](https://github.com/AbaSheger/stacklens/actions/workflows/ci.yml/badge.svg)](https://github.com/AbaSheger/stacklens/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 17+](https://img.shields.io/badge/Java-17%2B-blue.svg)](https://adoptium.net/)

---

## Why StackLens?

Debugging production logs is slow and repetitive. You scan hundreds of lines, recognize the same patterns over and over, and Google the same error messages every time.

StackLens automates that first step. Point it at a log file or paste a stack trace, and it tells you:

- **What** the problem is
- **Why** it happens
- **How** to fix it

It's fast, offline, and works with any Java or Spring Boot application.

---

## Features

- Analyze log **files** or **paste stack traces** directly
- Detects **8 common backend failure types** out of the box
- Outputs **human-readable** or **JSON** format
- **Deduplicates** repeated errors — each issue type reported once
- **Exit codes** for scripting: `0` = clean, `2` = issues found
- Extensible: add new detectors by implementing one interface

### Detected Error Types

| Error Type | Example Pattern |
|---|---|
| `NullPointerException` | `java.lang.NullPointerException at ...` |
| `DatabaseConnectionFailure` | `Unable to acquire JDBC Connection` |
| `TimeoutError` | `SocketTimeoutException: Read timed out` |
| `ConnectionRefused` | `Connection refused: localhost:8080` |
| `OutOfMemoryError` | `OutOfMemoryError: Java heap space` |
| `AuthenticationError` | `401 Unauthorized`, `Bad credentials`, `JWT expired` |
| `ThreadPoolExhaustion` | `RejectedExecutionException: Task rejected` |
| `Http500InternalServerError` | `500 Internal Server Error` |

---

## Installation

### Prerequisites

- Java 17 or higher
- Maven 3.8 or higher (to build from source)

### Build from Source

```bash
git clone https://github.com/AbaSheger/stacklens.git
cd stacklens
mvn clean package -DskipTests
```

This produces `target/stacklens.jar`.

### Create a Shell Alias (Optional)

For convenience, add this to your `~/.bashrc` or `~/.zshrc`:

```bash
alias stacklens='java -jar /path/to/stacklens.jar'
```

Then reload your shell:

```bash
source ~/.bashrc
```

---

## Usage

### Analyze a log file

```bash
java -jar stacklens.jar analyze app.log
```

### Analyze an inline stack trace

```bash
java -jar stacklens.jar analyze --text "java.lang.NullPointerException at OrderService.java:42"
```

### Output as JSON

```bash
java -jar stacklens.jar analyze app.log --output json
```

### Disable ANSI colors (for CI or log files)

```bash
java -jar stacklens.jar analyze app.log --no-color
```

### Get help

```bash
java -jar stacklens.jar --help
java -jar stacklens.jar analyze --help
```

---

## Example Output

### Human-Readable

```
StackLens Analysis Report
Source: samples/sample-npe.log
────────────────────────────────────────────────────────────

✗ 1 issue(s) detected:

────────────────────────────────────────────────────────────
Issue #1: NullPointerException

Detected in:
  java.lang.NullPointerException: Cannot invoke "com.example.User.getEmail()" because "user" is null

Explanation:
  A null object reference was accessed in the application.
  This happens when code tries to call a method or access a
  field on an object that has not been initialized (is null).

Suggested fixes:
  1. Ensure all objects are properly initialized before use
  2. Add null checks before accessing object fields or methods
  3. Use Optional<T> to express that a value may be absent
  4. Validate method parameters at the start of each method
  5. Check if a dependency injection (e.g. @Autowired) is missing
```

### JSON Output

```json
{
  "source" : "samples/sample-npe.log",
  "issueCount" : 1,
  "issues" : [ {
    "issue" : "NullPointerException",
    "explanation" : "A null object reference was accessed in the application. ...",
    "suggestions" : [
      "Ensure all objects are properly initialized before use",
      "Add null checks before accessing object fields or methods",
      "Use Optional<T> to express that a value may be absent",
      "Validate method parameters at the start of each method",
      "Check if a dependency injection (e.g. @Autowired) is missing"
    ]
  } ]
}
```

### Clean Log

```
StackLens Analysis Report
Source: samples/sample-healthy.log
────────────────────────────────────────────────────────────

✓ No known issues detected in the provided log.
  The log looks clean, but always review warnings manually.
```

---

## Exit Codes

StackLens uses exit codes to support shell scripting:

| Code | Meaning |
|---|---|
| `0` | No issues detected |
| `1` | Error (file not found, invalid arguments) |
| `2` | One or more issues detected |

**Example: fail a CI build if issues are found:**

```bash
java -jar stacklens.jar analyze app.log || exit 1
```

---

## Sample Log Files

The `samples/` directory contains log files you can use to try StackLens:

| File | Contains |
|---|---|
| `sample-npe.log` | NullPointerException |
| `sample-db-failure.log` | Database connection failure |
| `sample-oom.log` | OutOfMemoryError |
| `sample-mixed-errors.log` | Auth failure, timeout, thread pool exhaustion, HTTP 500 |
| `sample-healthy.log` | Clean log (no errors) |

```bash
java -jar target/stacklens.jar analyze samples/sample-mixed-errors.log
```

---

## Architecture

StackLens follows a clean, layered architecture that makes it easy to extend:

```
CLI (picocli)
    └── AnalyzeCommand
            └── LogAnalyzer          (reads files / text, drives analysis)
                    └── IssueClassifier   (applies all detectors to each line)
                            └── IssueDetector (interface)
                                    ├── NullPointerDetector
                                    ├── DatabaseConnectionDetector
                                    ├── TimeoutDetector
                                    ├── ConnectionRefusedDetector
                                    ├── OutOfMemoryDetector
                                    ├── AuthenticationErrorDetector
                                    ├── ThreadPoolExhaustionDetector
                                    └── Http500Detector
            └── HumanReadableFormatter / JsonFormatter
```

### Adding a New Detector

1. Create a class implementing `IssueDetector`
2. Register it in `IssueClassifier`

That's it. No other changes required. See [CONTRIBUTING.md](CONTRIBUTING.md) for a step-by-step guide.

---

## Project Structure

```
stacklens/
├── src/
│   ├── main/java/com/stacklens/
│   │   ├── cli/           # CLI commands
│   │   ├── analyzer/      # LogAnalyzer — entry point for analysis
│   │   ├── classifier/    # IssueClassifier — orchestrates detectors
│   │   ├── detector/      # One class per error type
│   │   ├── model/         # Issue, AnalysisResult
│   │   └── output/        # HumanReadableFormatter, JsonFormatter
│   └── test/java/com/stacklens/
│       ├── analyzer/      # LogAnalyzerTest
│       ├── detector/      # Per-detector unit tests
│       └── cli/           # CLI integration tests
├── samples/               # Sample log files for manual testing
├── docs/                  # Additional documentation
├── .github/
│   ├── workflows/ci.yml   # GitHub Actions CI
│   └── ISSUE_TEMPLATE/    # Bug and feature request templates
├── pom.xml
├── README.md
├── CONTRIBUTING.md
├── CHANGELOG.md
└── LICENSE
```

---

## Running Tests

```bash
mvn test
```

---

## Roadmap

- [ ] **AI explanations** — use an LLM API to generate context-aware explanations
- [ ] **Spring Boot structured logs** — parse JSON log format from Logback
- [ ] **Kubernetes log support** — analyze `kubectl logs` output with pod context
- [ ] **Plugin system** — load custom detectors from external JARs
- [ ] **Watch mode** — tail a log file and detect issues in real time
- [ ] **Severity levels** — classify issues as CRITICAL / WARNING / INFO
- [ ] **HTML report** — generate a standalone HTML report file

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) to get started.

The most impactful contributions are **new error detectors**. If you've seen a common Java or Spring Boot error that StackLens doesn't detect yet, please add it!

---

## License

StackLens is released under the [MIT License](LICENSE).
