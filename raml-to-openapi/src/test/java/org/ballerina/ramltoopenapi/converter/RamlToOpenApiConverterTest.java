package org.ballerina.ramltoopenapi.converter;

import io.swagger.v3.oas.models.OpenAPI;
import org.ballerina.ramltoopenapi.exception.ConverterException;
import org.ballerina.ramltoopenapi.model.openapi.OpenApiDocument;
import org.ballerina.ramltoopenapi.model.raml.RamlDocument;
import org.ballerina.ramltoopenapi.parser.RamlParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for RamlToOpenApiConverter.
 */
@DisplayName("RAML to OpenAPI Converter Tests")
class RamlToOpenApiConverterTest {

    private RamlParser parser;
    private RamlToOpenApiConverter converter;

    @BeforeEach
    void setUp() {
        parser = new RamlParser();
        converter = new RamlToOpenApiConverter(false);
    }

    private File getResourceFile(String path) {
        URL resource = getClass().getClassLoader().getResource(path);
        assertThat(resource).as("Resource file not found: " + path).isNotNull();
        return new File(resource.getFile());
    }

    @Test
    @DisplayName("Should convert simple RAML to OpenAPI")
    void shouldConvertSimpleRaml() throws ConverterException {
        File ramlFile = getResourceFile("test-cases/basic/01-simple-api.raml");
        RamlDocument ramlDoc = parser.parse(ramlFile);

        OpenApiDocument openApiDoc = converter.convert(ramlDoc);

        assertThat(openApiDoc).isNotNull();
        assertThat(openApiDoc.getOpenApi()).isNotNull();
        assertThat(openApiDoc.getSourceFileName()).isEqualTo("01-simple-api.raml");

        OpenAPI openApi = openApiDoc.getOpenApi();
        assertThat(openApi.getOpenapi()).isEqualTo("3.0.0");
        assertThat(openApi.getInfo()).isNotNull();
        assertThat(openApi.getInfo().getTitle()).isEqualTo("Simple API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("1.0");
    }

    @Test
    @DisplayName("Should convert RAML with paths")
    void shouldConvertRamlWithPaths() throws ConverterException {
        File ramlFile = getResourceFile("test-cases/basic/01-simple-api.raml");
        RamlDocument ramlDoc = parser.parse(ramlFile);

        OpenApiDocument openApiDoc = converter.convert(ramlDoc);
        OpenAPI openApi = openApiDoc.getOpenApi();

        assertThat(openApi.getPaths()).isNotNull();
        assertThat(openApi.getPaths()).containsKey("/users");
        assertThat(openApi.getPaths().get("/users").getGet()).isNotNull();
    }

    @Test
    @DisplayName("Should convert RAML with servers")
    void shouldConvertRamlWithServers() throws ConverterException {
        File ramlFile = getResourceFile("test-cases/basic/01-simple-api.raml");
        RamlDocument ramlDoc = parser.parse(ramlFile);

        OpenApiDocument openApiDoc = converter.convert(ramlDoc);
        OpenAPI openApi = openApiDoc.getOpenApi();

        assertThat(openApi.getServers()).isNotEmpty();
        assertThat(openApi.getServers().get(0).getUrl()).isEqualTo("https://api.example.com");
    }

    @Test
    @DisplayName("Should convert RAML with types to schemas")
    void shouldConvertRamlWithTypes() throws ConverterException {
        File ramlFile = getResourceFile("test-cases/basic/03-object-types.raml");
        RamlDocument ramlDoc = parser.parse(ramlFile);

        OpenApiDocument openApiDoc = converter.convert(ramlDoc);
        OpenAPI openApi = openApiDoc.getOpenApi();

        assertThat(openApi.getComponents()).isNotNull();
        assertThat(openApi.getComponents().getSchemas()).isNotNull();
        assertThat(openApi.getComponents().getSchemas()).containsKey("User");
    }

    @Test
    @DisplayName("Should convert RAML with security schemes")
    void shouldConvertRamlWithSecurity() throws ConverterException {
        File ramlFile = getResourceFile("test-cases/security/06-api-key.raml");
        RamlDocument ramlDoc = parser.parse(ramlFile);

        OpenApiDocument openApiDoc = converter.convert(ramlDoc);
        OpenAPI openApi = openApiDoc.getOpenApi();

        assertThat(openApi.getComponents()).isNotNull();
        assertThat(openApi.getComponents().getSecuritySchemes()).isNotNull();
        assertThat(openApi.getComponents().getSecuritySchemes()).isNotEmpty();
    }

    @Test
    @DisplayName("Should throw exception for null RAML document")
    void shouldThrowExceptionForNullDocument() {
        assertThatThrownBy(() -> converter.convert(null))
            .isInstanceOf(ConverterException.class)
            .hasMessageContaining("Cannot convert null RAML document");
    }

    @Test
    @DisplayName("Should create converter in strict mode")
    void shouldCreateConverterInStrictMode() {
        RamlToOpenApiConverter strictConverter = new RamlToOpenApiConverter(true);
        assertThat(strictConverter).isNotNull();
    }
}

