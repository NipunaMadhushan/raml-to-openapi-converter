package org.nipunaml.ramltoopenapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests for type-related RAML to OpenAPI conversion scenarios.
 * Tests type inheritance and complex type structures.
 */
@DisplayName("Type Conversion Tests")
class TypeConversionTest extends BaseConversionTest {

    private static final String TEST_DIR = "test-cases/types";

    @Test
    @DisplayName("04 - Type inheritance")
    void testTypeInheritance() throws Exception {
        testConversion(TEST_DIR, "04-type-inheritance");
    }
}

