package org.ballerina.ramltoopenapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.core.util.Json;
import org.ballerina.ramltoopenapi.converter.RamlToOpenApiConverter;
import org.ballerina.ramltoopenapi.exception.ConverterException;
import org.ballerina.ramltoopenapi.model.openapi.OpenApiDocument;
import org.ballerina.ramltoopenapi.model.raml.RamlDocument;
import org.ballerina.ramltoopenapi.parser.RamlParser;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for RAML to OpenAPI conversion tests.
 * Provides utility methods for loading test resources and comparing JSON output.
 */
public abstract class BaseConversionTest {

    protected RamlParser parser;
    protected RamlToOpenApiConverter converter;
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        parser = new RamlParser();
        converter = new RamlToOpenApiConverter(false);
        // Use swagger's Json.mapper() to ensure proper enum serialization
        objectMapper = Json.mapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Gets a file from the test resources directory.
     */
    protected File getResourceFile(String path) {
        URL resource = getClass().getClassLoader().getResource(path);
        assertThat(resource).as("Resource file not found: " + path).isNotNull();
        return new File(resource.getFile());
    }

    /**
     * Loads expected JSON content from a file.
     */
    protected String loadExpectedJson(String path) throws IOException {
        File file = getResourceFile(path);
        return objectMapper.writeValueAsString(objectMapper.readTree(file));
    }

    /**
     * Converts a RAML file to OpenAPI and returns the JSON string representation.
     */
    protected String convertRamlToJson(String ramlPath) throws ConverterException, IOException {
        File ramlFile = getResourceFile(ramlPath);
        RamlDocument ramlDocument = parser.parse(ramlFile);
        OpenApiDocument openApiDocument = converter.convert(ramlDocument);

        // Convert OpenAPI object to JSON string
        return objectMapper.writeValueAsString(openApiDocument.getOpenApi());
    }

    /**
     * Compares two JSON strings with lenient mode (ignores extra fields and array order).
     */
    protected void assertJsonEquals(String expected, String actual, String testCaseName) throws JSONException {
        JSONAssert.assertEquals(
            "JSON mismatch for test case: " + testCaseName,
            expected,
            actual,
            JSONCompareMode.LENIENT
        );
    }

    /**
     * Performs a conversion test by comparing RAML input with expected JSON output.
     */
    protected void testConversion(String testCaseDir, String baseName) throws Exception {
        String ramlPath = testCaseDir + "/" + baseName + ".raml";
        String expectedPath = testCaseDir + "/" + baseName + ".expected.json";

        String expected = loadExpectedJson(expectedPath);
        String actual = convertRamlToJson(ramlPath);

        assertJsonEquals(expected, actual, baseName);
    }
}

