package org.ballerina.ramltoopenapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for advanced RAML to OpenAPI conversion scenarios.
 * Tests complete APIs with all features combined.
 */
@DisplayName("Advanced Conversion Tests")
class AdvancedConversionTest extends BaseConversionTest {

    private static final String TEST_DIR = "test-cases/advanced";

    @Test
    @DisplayName("09 - Complete API with all features")
    void testCompleteApi() throws Exception {
        testConversion(TEST_DIR, "09-complete-api");
    }
}

