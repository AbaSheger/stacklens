package com.stacklens.detector;

import com.stacklens.model.Issue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionDetectorTest {

    private DatabaseConnectionDetector detector;

    @BeforeEach
    void setUp() {
        detector = new DatabaseConnectionDetector();
    }

    @Test
    void detectsJdbcConnectionFailure() {
        String line = "ERROR HikariPool: Unable to acquire JDBC Connection";
        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertEquals("DatabaseConnectionFailure", result.get().getType());
    }

    @Test
    void detectsCommunicationsLinkFailure() {
        String line = "com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure";
        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
    }

    @Test
    void detectsCouldNotConnectToServer() {
        String line = "org.postgresql.util.PSQLException: The connection attempt failed. Could not connect to server";
        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
    }

    @Test
    void detectsNoSuitableDriver() {
        String line = "java.sql.SQLException: No suitable driver found for jdbc:postgresql://localhost:5432/mydb";
        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
    }

    @Test
    void doesNotMatchUnrelatedLine() {
        String line = "INFO  Starting application context";
        Optional<Issue> result = detector.detect(line);

        assertFalse(result.isPresent());
    }

    @Test
    void doesNotMatchNullInput() {
        Optional<Issue> result = detector.detect(null);
        assertFalse(result.isPresent());
    }

    @Test
    void issueContainsHelpfulSuggestions() {
        String line = "Unable to acquire JDBC Connection";
        Optional<Issue> result = detector.detect(line);

        assertTrue(result.isPresent());
        assertTrue(result.get().getSuggestions().size() >= 3,
            "Should provide at least 3 suggestions for database connection failures");
    }
}
