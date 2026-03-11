# Contributing to StackLens

Thank you for your interest in contributing! StackLens is a beginner-friendly project and we welcome all contributions — from new error detectors to documentation improvements.

---

## Table of Contents

- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Adding a New Error Detector](#adding-a-new-error-detector)
- [Running Tests](#running-tests)
- [Code Style](#code-style)
- [Submitting a Pull Request](#submitting-a-pull-request)

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- Git

### Build the project

```bash
git clone https://github.com/AbaSheger/stacklens.git
cd stacklens
mvn clean package
```

### Run the JAR

```bash
java -jar target/stacklens.jar analyze samples/sample-npe.log
```

---

## Project Structure

```
src/main/java/com/stacklens/
├── cli/          # CLI commands (Main.java, AnalyzeCommand.java)
├── analyzer/     # Reads files and text, drives analysis
├── classifier/   # Applies all detectors to each log line
├── detector/     # One class per error type (IssueDetector implementations)
├── model/        # Data classes (Issue, AnalysisResult)
└── output/       # Formats results (human-readable, JSON)
```

---

## Adding a New Error Detector

Adding a new detector is the most common contribution. Here's how:

### Step 1: Create the detector class

Create a new file in `src/main/java/com/stacklens/detector/`.

```java
package com.stacklens.detector;

import com.stacklens.model.Issue;
import java.util.List;
import java.util.Optional;

public class MyNewErrorDetector implements IssueDetector {

    @Override
    public String getIssueType() {
        return "MyNewError"; // Short, descriptive name
    }

    @Override
    public Optional<Issue> detect(String line) {
        if (line == null) return Optional.empty();

        if (line.contains("my error pattern")) {
            return Optional.of(new Issue(
                getIssueType(),
                "Clear explanation of what this error means.",
                List.of(
                    "First actionable fix",
                    "Second actionable fix"
                ),
                line.trim()
            ));
        }

        return Optional.empty();
    }
}
```

### Step 2: Register the detector

Open `src/main/java/com/stacklens/classifier/IssueClassifier.java` and add your detector to the list:

```java
detectors.add(new MyNewErrorDetector());
```

### Step 3: Write a test

Add a test in `src/test/java/com/stacklens/detector/` that:
- Verifies your detector matches the expected log lines
- Verifies it does NOT match unrelated lines
- Verifies it handles `null` input

### Step 4: Add a sample log file (optional but appreciated)

Add a sample log containing your error to `samples/sample-your-error.log`.

---

## Running Tests

```bash
mvn test
```

To run a specific test class:

```bash
mvn test -Dtest=NullPointerDetectorTest
```

---

## Code Style

- Follow standard Java naming conventions
- Keep classes focused on a single responsibility
- Write comments where logic is not immediately obvious
- Prefer readability over cleverness
- Don't use complex patterns when simple ones work

---

## Submitting a Pull Request

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-new-detector`
3. Make your changes and commit with a clear message
4. Ensure all tests pass: `mvn test`
5. Push your branch and open a Pull Request
6. Fill in the PR template

We'll review your PR and may ask for changes. We aim to respond within a few days.

---

## Questions?

Open a GitHub Issue and we'll be happy to help.
