package org.nipunaml.ramltoopenapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for resource-related RAML to OpenAPI conversion scenarios.
 * Tests nested resources and parameters.
 */
@DisplayName("Resource Conversion Tests")
class ResourceConversionTest extends BaseConversionTest {

    private static final String TEST_DIR = "test-cases/resources";

    @Test
    @DisplayName("07 - Nested resources")
    void testNestedResources() throws Exception {
        testConversion(TEST_DIR, "07-nested-resources");
    }

    @Test
    @DisplayName("08 - Parameters (query, path, header)")
    void testParameters() throws Exception {
        testConversion(TEST_DIR, "08-parameters");
    }
}

