# RAML to OpenAPI Converter - Java Library

A Java converter for converting RAML (RESTful API Modeling Language) specifications to OpenAPI 3.0 format.

## Features

- ✅ Convert RAML 1.0 to OpenAPI 3.0
- ✅ Support for YAML and JSON output formats
- ✅ Batch conversion of multiple files
- ✅ Directory-based conversion
- ✅ Fluent API with builder pattern
- ✅ Stream-based conversion
- ✅ Comprehensive error handling
- ✅ Validation support
- ✅ Strict mode for enhanced validation

## Installation

### Gradle

```gradle
dependencies {
    implementation 'org.nipunaml:raml-to-openapi:1.0.0-SNAPSHOT'
}
```

### Maven

```xml
<dependency>
    <groupId>org.nipunaml</groupId>
    <artifactId>raml-to-openapi</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Basic Conversion

```java
import org.nipunaml.ramltoopenapi.RamlToOpenApiConverter;
import io.swagger.v3.oas.models.OpenAPI;
import java.io.File;

// Create converter instance
RamlToOpenApiConverter converter = RamlToOpenApiConverter.create();

// Convert RAML file to OpenAPI object
OpenAPI openApi = converter.convertToOpenApi(new File("api.raml"));

// Convert and write to file
File outputFile = converter.convertAndWrite(
    new File("api.raml"), 
    new File("openapi.yaml")
);
```

### Convert to String

```java
// Convert to YAML string
String yamlString = converter.convertToYaml(new File("api.raml"));

// Convert to JSON string
String jsonString = converter.convertToJson(new File("api.raml"));
```

### Using Builder Pattern

```java
// Create converter with custom configuration
RamlToOpenApiConverter converter = RamlToOpenApiConverter.builder()
    .strictMode(true)  // Enable strict validation
    .build();

// Use the configured converter
OpenAPI openApi = converter.convertToOpenApi(new File("api.raml"));
```

## Advanced Usage

### Batch Conversion

```java
import java.util.List;
import java.util.Arrays;

// Convert multiple RAML files
List<File> ramlFiles = Arrays.asList(
    new File("api1.raml"),
    new File("api2.raml"),
    new File("api3.raml")
);

List<OpenAPI> openApiDocs = converter.convertMultiple(ramlFiles);
```

### Directory Conversion

```java
// Convert all RAML files in a directory
List<OpenAPI> results = converter.convertDirectory(
    new File("raml-specs"),
    true  // recursive
);

// Convert directory and write to output directory
List<File> outputFiles = converter.convertAndWriteDirectory(
    new File("raml-specs"),
    new File("openapi-specs"),
    "yaml",  // or "json"
    true     // recursive
);
```

### Stream-Based Conversion

```java
import java.io.InputStream;
import java.io.FileInputStream;

// Convert from InputStream
try (InputStream is = new FileInputStream("api.raml")) {
    OpenAPI openApi = converter.convertToOpenApi(is, "api.raml");
}
```

### Validation Only

```java
// Validate RAML file without conversion
File ramlFile = new File("api.raml");
boolean isValid = converter.validateRamlFile(ramlFile);

if (isValid) {
    System.out.println("RAML file is valid");
} else {
    System.out.println("RAML file is invalid");
}
```

### Error Handling

```java
import org.nipunaml.ramltoopenapi.exception.ConverterException;

try {
    OpenAPI openApi = converter.convertToOpenApi(new File("api.raml"));
    System.out.println("Conversion successful!");
} catch (ConverterException e) {
    System.err.println("Conversion failed: " + e.getMessage());
    e.printStackTrace();
}
```

## API Reference

### RamlToOpenApiConverter

Main facade class for RAML to OpenAPI conversion.

#### Factory Methods

- `static RamlToOpenApiConverter create()` - Creates instance with default configuration
- `static Builder builder()` - Creates a builder for custom configuration

#### Core Conversion Methods

| Method | Description |
|--------|-------------|
| `OpenAPI convertToOpenApi(File ramlFile)` | Converts RAML file to OpenAPI object |
| `OpenAPI convertToOpenApi(String ramlFilePath)` | Converts RAML file path to OpenAPI object |
| `OpenAPI convertToOpenApi(InputStream inputStream, String fileName)` | Converts from InputStream |
| `String convertToYaml(File ramlFile)` | Converts to YAML string |
| `String convertToJson(File ramlFile)` | Converts to JSON string |
| `File convertAndWrite(File ramlFile, File outputFile)` | Converts and writes to file (auto-detect format) |
| `File convertAndWrite(File ramlFile, File outputFile, String format)` | Converts and writes with specific format |

#### Batch Conversion Methods

| Method | Description |
|--------|-------------|
| `List<OpenAPI> convertMultiple(List<File> ramlFiles)` | Converts multiple files |
| `List<OpenAPI> convertDirectory(File directory, boolean recursive)` | Converts all RAML files in directory |
| `List<File> convertAndWriteDirectory(File inputDir, File outputDir, String format, boolean recursive)` | Converts directory and writes output |

#### Utility Methods

| Method | Description |
|--------|-------------|
| `boolean validateRamlFile(File ramlFile)` | Validates RAML file without conversion |
| `boolean isStrictMode()` | Checks if strict mode is enabled |

### Builder

Configuration builder for RamlToOpenApiConverter.

| Method | Description |
|--------|-------------|
| `Builder strictMode(boolean strict)` | Enables/disables strict validation mode |
| `RamlToOpenApiConverter build()` | Builds the converter instance |

## Configuration Options

### Strict Mode

When strict mode is enabled, the converter will:
- Perform enhanced validation
- Fail on warnings (not just errors)
- Provide detailed error messages

```java
RamlToOpenApiConverter converter = RamlToOpenApiConverter.builder()
    .strictMode(true)
    .build();
```

## Output Formats

The converter supports two output formats:

1. **YAML** (default) - `.yaml` or `.yml` extension
2. **JSON** - `.json` extension

The format is auto-detected from the output file extension when using `convertAndWrite()`, or can be explicitly specified.

## Examples

### Example 1: Simple Conversion

```java
import org.nipunaml.ramltoopenapi.RamlToOpenApiConverter;
import io.swagger.v3.oas.models.OpenAPI;
import java.io.File;

public class SimpleConversion {
    public static void main(String[] args) {
        try {
            RamlToOpenApiConverter converter = RamlToOpenApiConverter.create();
            
            OpenAPI openApi = converter.convertToOpenApi(new File("petstore.raml"));
            
            System.out.println("Title: " + openApi.getInfo().getTitle());
            System.out.println("Version: " + openApi.getInfo().getVersion());
            System.out.println("Paths: " + openApi.getPaths().size());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Example 2: Batch Processing

```java
import org.nipunaml.ramltoopenapi.RamlToOpenApiConverter;
import java.io.File;
import java.util.List;

public class BatchConversion {
    public static void main(String[] args) {
        try {
            RamlToOpenApiConverter converter = RamlToOpenApiConverter.create();
            
            // Convert all RAML files in a directory
            List<File> outputFiles = converter.convertAndWriteDirectory(
                new File("src/raml"),
                new File("target/openapi"),
                "yaml",
                true  // recursive
            );
            
            System.out.println("Converted " + outputFiles.size() + " files:");
            outputFiles.forEach(f -> System.out.println("  - " + f.getName()));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Example 3: Integration with Web Service

```java
import org.nipunaml.ramltoopenapi.RamlToOpenApiConverter;
import io.swagger.v3.oas.models.OpenAPI;
import java.io.InputStream;

public class WebServiceIntegration {
    
    private final RamlToOpenApiConverter converter;
    
    public WebServiceIntegration() {
        this.converter = RamlToOpenApiConverter.builder()
            .strictMode(true)
            .build();
    }
    
    public String convertRamlToOpenApiYaml(InputStream ramlContent, String fileName) {
        try {
            OpenAPI openApi = converter.convertToOpenApi(ramlContent, fileName);
            return io.swagger.v3.core.util.Yaml.mapper().writeValueAsString(openApi);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert RAML: " + e.getMessage(), e);
        }
    }
}
```

## Exception Handling

The converter uses `ConverterException` for all conversion-related errors:

```java
import org.nipunaml.ramltoopenapi.exception.ConverterException;

try {
    OpenAPI openApi = converter.convertToOpenApi(new File("api.raml"));
} catch (ConverterException e) {
    // Handle conversion errors
    System.err.println("Error: " + e.getMessage());
    
    // Get root cause if needed
    Throwable cause = e.getCause();
    if (cause != null) {
        System.err.println("Caused by: " + cause.getMessage());
    }
}
```

## Logging

The converter uses SLF4J for logging. You can configure logging levels in your application:

```xml
<!-- logback.xml -->
<configuration>
    <logger name="org.nipunaml.ramltoopenapi" level="INFO"/>
</configuration>
```

Available log levels:
- `DEBUG` - Detailed conversion process information
- `INFO` - General conversion progress (default)
- `WARN` - Warnings during conversion
- `ERROR` - Conversion errors

## Requirements

- Java 17 or higher
- Dependencies managed by Gradle/Maven

## License

Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)

Licensed under the Apache License, Version 2.0

## Support

For issues, questions, or contributions, please visit the project repository.

