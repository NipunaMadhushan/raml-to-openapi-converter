# RAML to OpenAPI Converter

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build](https://img.shields.io/badge/build-passing-brightgreen.svg)]()

A comprehensive Java-based tool for converting RAML (RESTful API Modeling Language) specifications to OpenAPI 3.0 format. This project provides both a Java library and a Ballerina tool for seamless API specification conversion.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
  - [As a Java Library](#as-a-java-library)
  - [As a Ballerina Tool](#as-a-ballerina-tool)
  - [As a CLI Tool](#as-a-cli-tool)
- [Building from Source](#building-from-source)
- [Testing](#testing)
- [Configuration](#configuration)
- [Examples](#examples)
- [Contributing](#contributing)
- [License](#license)

## 🔍 Overview

This project provides tools to convert RAML API specifications to OpenAPI 3.0 format, enabling API developers to:
- Migrate legacy RAML specifications to modern OpenAPI standards
- Integrate RAML conversion into Java applications
- Use Ballerina tooling for API specification management
- Automate conversion processes through CLI interfaces

## ✨ Features

- ✅ **RAML 1.0 to OpenAPI 3.0 Conversion** - Full support for RAML 1.0 specifications
- ✅ **Multiple Output Formats** - YAML and JSON output support
- ✅ **Batch Processing** - Convert multiple files in one operation
- ✅ **Directory Conversion** - Process entire directories of RAML files
- ✅ **Fluent API** - Builder pattern for easy configuration
- ✅ **Stream-based Conversion** - Efficient memory usage for large files
- ✅ **Comprehensive Error Handling** - Detailed error messages and validation
- ✅ **Strict Mode** - Enhanced validation for production use
- ✅ **CLI Support** - Command-line interface for standalone usage
- ✅ **Ballerina Integration** - Native Ballerina tool support

## 📁 Project Structure

```
raml-to-openapi-converter/
├── raml-to-openapi/           # Core Java library
│   ├── src/
│   │   ├── main/java/         # Library source code
│   │   └── test/              # Unit tests
│   └── README.md              # Library documentation
├── cli-raml-to-openapi/       # CLI and Ballerina tool
│   ├── src/
│   │   ├── main/java/         # CLI implementation
│   │   └── main/ballerina/    # Ballerina tool integration
│   └── build.gradle
├── build.gradle               # Root build configuration
└── README.md                  # This file
```

## 📦 Prerequisites

- **Java**: JDK 11 or higher
- **Gradle**: 7.0 or higher (wrapper included)
- **Ballerina**: Swan Lake Update 8 or higher (for Ballerina tool)

## 🚀 Installation

### Using Gradle

Add the dependency to your `build.gradle`:

```gradle
dependencies {
    implementation 'org.nipunaml.ramltoopenapi:raml-to-openapi:1.0.0-SNAPSHOT'
}
```

### Using Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>org.nipunaml.ramltoopenapi</groupId>
    <artifactId>raml-to-openapi</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Installing CLI Tool

Download and extract the distribution:

```bash
# Build from source
./gradlew build

# Extract CLI distribution
cd cli-raml-to-openapi/build/distributions
unzip cli-raml-to-openapi-1.0.0-SNAPSHOT.zip

# Add to PATH (optional)
export PATH=$PATH:/path/to/cli-raml-to-openapi-1.0.0-SNAPSHOT/bin
```

## 💻 Usage

### As a Java Library

#### Basic Conversion

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

#### Advanced Configuration

```java
// Create converter with custom configuration
RamlToOpenApiConverter converter = RamlToOpenApiConverter.builder()
    .strictMode(true)  // Enable strict validation
    .build();

// Convert to JSON string
String jsonString = converter.convertToJson(new File("api.raml"));

// Convert to YAML string
String yamlString = converter.convertToYaml(new File("api.raml"));
```

#### Batch Conversion

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

### As a Ballerina Tool

```bash
# Convert a single RAML file
bal raml-to-openapi -i api.raml -o openapi.yaml

# Convert with options
bal raml-to-openapi -i api.raml -o openapi.json --format json

# Convert entire directory
bal raml-to-openapi -i ./raml-specs -o ./openapi-specs
```

### As a CLI Tool

```bash
# Convert a RAML file to OpenAPI YAML
cli-raml-to-openapi -i api.raml -o openapi.yaml

# Convert to JSON format
cli-raml-to-openapi -i api.raml -o openapi.json --format json

# Enable strict mode
cli-raml-to-openapi -i api.raml -o openapi.yaml --strict
```

## 🔨 Building from Source

### Clone the Repository

```bash
git clone https://github.com/NipunaMadhushan/raml-to-openapi-converter.git
cd raml-to-openapi-converter
```

### Build All Modules

```bash
# Build entire project
./gradlew build

# Build only the core library
./gradlew :raml-to-openapi:build

# Build only the CLI tool
./gradlew :cli-raml-to-openapi:build
```

### Build Artifacts

After building, you'll find:
- JAR files in `*/build/libs/`
- CLI distributions in `cli-raml-to-openapi/build/distributions/`

## 🧪 Testing

### Run All Tests

```bash
./gradlew test
```

### Run Specific Module Tests

```bash
# Test core library
./gradlew :raml-to-openapi:test

# Test CLI tool
./gradlew :cli-raml-to-openapi:test
```

### Generate Test Reports

Test reports are generated in:
- `raml-to-openapi/build/reports/tests/test/`
- `cli-raml-to-openapi/build/reports/tests/test/`

## ⚙️ Configuration

### Converter Options

- **Strict Mode**: Enable enhanced validation and error checking
- **Output Format**: Choose between YAML or JSON output
- **Validation**: Optional schema validation for RAML input

### Example Configuration

```java
RamlToOpenApiConverter converter = RamlToOpenApiConverter.builder()
    .strictMode(true)
    .validate(true)
    .build();
```

## 📚 Examples

For detailed examples and use cases, see the [library documentation](raml-to-openapi/README.md).

### Example RAML Input

```yaml
#%RAML 1.0
title: Sample API
version: v1
baseUri: https://api.example.com/{version}

/users:
  get:
    description: Get all users
    responses:
      200:
        body:
          application/json:
            type: object
```

### Example OpenAPI Output

```yaml
openapi: 3.0.0
info:
  title: Sample API
  version: v1
servers:
  - url: https://api.example.com/v1
paths:
  /users:
    get:
      description: Get all users
      responses:
        '200':
          description: OK
          content:
            application/json:
              schema:
                type: object
```

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Quality

- Follow Java coding conventions
- Add unit tests for new features
- Run `./gradlew check` before committing
- Update documentation as needed

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- RAML specification: [https://raml.org/](https://raml.org/)
- OpenAPI specification: [https://www.openapis.org/](https://www.openapis.org/)
- Ballerina platform: [https://ballerina.io/](https://ballerina.io/)

## 📞 Support

For issues, questions, or contributions, please:
- Open an issue on [GitHub Issues](https://github.com/NipunaMadhushan/raml-to-openapi-converter/issues)
- Submit a pull request for improvements
- Check the [documentation](raml-to-openapi/README.md) for detailed usage

---

**Note**: This project is under active development. API and features may change.
This will convert RAML API specifications to OpenAPI specifications
