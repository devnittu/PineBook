package com.pinebook.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring context loads without errors.
 * Requires a running PostgreSQL and Python AI service unless overridden by test profile.
 */
@SpringBootTest
@ActiveProfiles("test")
class PineApiApplicationTest {

    @Test
    void contextLoads() {
        // If this passes, the full application context wired up correctly
    }
}
