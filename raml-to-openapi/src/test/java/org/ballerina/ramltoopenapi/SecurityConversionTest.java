package org.ballerina.ramltoopenapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for security-related RAML to OpenAPI conversion scenarios.
 * Tests OAuth2, API Key, and other security schemes.
 */
@DisplayName("Security Conversion Tests")
class SecurityConversionTest extends BaseConversionTest {

    private static final String TEST_DIR = "test-cases/security";

    @Test
    @DisplayName("05 - OAuth2 security scheme")
    void testOAuth2() throws Exception {
        testConversion(TEST_DIR, "05-oauth2");
    }

    @Test
    @DisplayName("06 - API Key security scheme")
    void testApiKey() throws Exception {
        testConversion(TEST_DIR, "06-api-key");
    }
}

