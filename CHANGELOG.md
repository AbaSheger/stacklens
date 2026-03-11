# Changelog

All notable changes to StackLens will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Planned
- AI-powered explanations using LLM APIs
- Spring Boot structured log format support
- Kubernetes log analysis (`kubectl logs` output)
- Plugin system for custom detectors
- Watch mode to tail log files in real time

---

## [1.0.0] - 2024-01-15

### Added

- Initial release of StackLens
- `analyze` CLI command supporting file input and inline text (`--text`)
- Human-readable terminal output with ANSI color support
- JSON output mode (`--output json`) for scripting and integrations
- **8 built-in error detectors:**
  - `NullPointerDetector` — detects `NullPointerException`
  - `DatabaseConnectionDetector` — detects JDBC and HikariCP connection failures
  - `TimeoutDetector` — detects `SocketTimeoutException` and request timeouts
  - `ConnectionRefusedDetector` — detects `Connection refused` errors
  - `OutOfMemoryDetector` — detects `OutOfMemoryError` (heap space, GC overhead)
  - `AuthenticationErrorDetector` — detects 401/403, bad credentials, expired JWT
  - `ThreadPoolExhaustionDetector` — detects `RejectedExecutionException`
  - `Http500Detector` — detects HTTP 500 Internal Server Error
- Deduplication of repeated errors (each issue type reported once)
- Exit code signaling: `0` = clean, `1` = error, `2` = issues detected
- JUnit 5 unit tests for all detectors, analyzer, and CLI
- Sample log files for manual testing
- GitHub Actions CI workflow (Java 17 and 21)
- MIT License
