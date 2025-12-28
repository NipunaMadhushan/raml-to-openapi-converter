/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerina.ramltoopenapi;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import org.ballerina.ramltoopenapi.exception.ConverterException;
import org.ballerina.ramltoopenapi.model.openapi.OpenApiDocument;
import org.ballerina.ramltoopenapi.model.raml.RamlDocument;
import org.ballerina.ramltoopenapi.parser.RamlFileValidator;
import org.ballerina.ramltoopenapi.parser.RamlParser;
import org.ballerina.ramltoopenapi.writer.OpenApiWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main facade class for RAML to OpenAPI conversion.
 * Provides a simple, fluent API for converting RAML specifications to OpenAPI 3.0 format.
 *
 * <p>Usage examples:</p>
 * <pre>
 * // Basic conversion
 * RamlToOpenApiConverter converter = RamlToOpenApiConverter.create();
 * OpenAPI openApi = converter.convertToOpenApi(new File("api.raml"));
 *
 * // With strict mode
 * RamlToOpenApiConverter converter = RamlToOpenApiConverter.builder()
 *     .strictMode(true)
 *     .build();
 * </pre>
 *
 * @since 1.0.0
 */
public class RamlToOpenApiConverter {
    
    private static final Logger logger = LoggerFactory.getLogger(RamlToOpenApiConverter.class);
    
    private final boolean strictMode;
    private final RamlParser parser;
    private final org.ballerina.ramltoopenapi.converter.RamlToOpenApiConverter coreConverter;
    private final OpenApiWriter writer;
    private final RamlFileValidator validator;
    
    /**
     * Private constructor. Use factory methods to create instances.
     *
     * @param strictMode whether to enable strict validation mode
     */
    private RamlToOpenApiConverter(boolean strictMode) {
        this.strictMode = strictMode;
        this.parser = new RamlParser();
        this.coreConverter = new org.ballerina.ramltoopenapi.converter.RamlToOpenApiConverter(strictMode);
        this.writer = new OpenApiWriter();
        this.validator = new RamlFileValidator();
    }
    
    /**
     * Creates a converter instance with default configuration.
     *
     * @return a new converter instance
     */
    public static RamlToOpenApiConverter create() {
        return new RamlToOpenApiConverter(false);
    }
    
    /**
     * Creates a builder for custom configuration.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Converts a RAML file to an OpenAPI object.
     *
     * @param ramlFile the RAML file to convert
     * @return the OpenAPI object
     * @throws ConverterException if conversion fails
     */
    public OpenAPI convertToOpenApi(File ramlFile) throws ConverterException {
        validateFile(ramlFile);
        RamlDocument ramlDoc = parser.parse(ramlFile);
        OpenApiDocument openApiDoc = coreConverter.convert(ramlDoc);
        return openApiDoc.getOpenApi();
    }
    
    /**
     * Converts a RAML file path to an OpenAPI object.
     *
     * @param ramlFilePath the path to the RAML file
     * @return the OpenAPI object
     * @throws ConverterException if conversion fails
     */
    public OpenAPI convertToOpenApi(String ramlFilePath) throws ConverterException {
        return convertToOpenApi(new File(ramlFilePath));
    }
    
    /**
     * Converts a RAML file from an InputStream to an OpenAPI object.
     *
     * @param inputStream the input stream containing RAML content
     * @param fileName the name of the file (for reference)
     * @return the OpenAPI object
     * @throws ConverterException if conversion fails
     */
    public OpenAPI convertToOpenApi(InputStream inputStream, String fileName) throws ConverterException {
        try {
            // Create temporary file from stream
            File tempFile = File.createTempFile("raml-", ".raml");
            tempFile.deleteOnExit();
            Files.copy(inputStream, tempFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            RamlDocument ramlDoc = parser.parse(tempFile);
            OpenApiDocument openApiDoc = coreConverter.convert(ramlDoc);
            return openApiDoc.getOpenApi();
        } catch (IOException e) {
            throw new ConverterException("Failed to read RAML from stream: " + e.getMessage(), e);
        }
    }
    
    /**
     * Converts a RAML file to YAML string.
     *
     * @param ramlFile the RAML file to convert
     * @return the OpenAPI specification as YAML string
     * @throws ConverterException if conversion fails
     */
    public String convertToYaml(File ramlFile) throws ConverterException {
        OpenAPI openApi = convertToOpenApi(ramlFile);
        try {
            return Yaml.mapper().writeValueAsString(openApi);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new ConverterException("Failed to serialize to YAML: " + e.getMessage(), e);
        }
    }
    
    /**
     * Converts a RAML file to JSON string.
     *
     * @param ramlFile the RAML file to convert
     * @return the OpenAPI specification as JSON string
     * @throws ConverterException if conversion fails
     */
    public String convertToJson(File ramlFile) throws ConverterException {
        OpenAPI openApi = convertToOpenApi(ramlFile);
        try {
            return io.swagger.v3.core.util.Json.mapper().writeValueAsString(openApi);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new ConverterException("Failed to serialize to JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * Converts a RAML file and writes to output file (auto-detect format from extension).
     *
     * @param ramlFile the RAML file to convert
     * @param outputFile the output file
     * @return the output file
     * @throws ConverterException if conversion fails
     */
    public File convertAndWrite(File ramlFile, File outputFile) throws ConverterException {
        String format = detectFormat(outputFile);
        return convertAndWrite(ramlFile, outputFile, format);
    }
    
    /**
     * Converts a RAML file and writes to output file with specified format.
     *
     * @param ramlFile the RAML file to convert
     * @param outputFile the output file
     * @param format the output format ("yaml" or "json")
     * @return the output file
     * @throws ConverterException if conversion fails
     */
    public File convertAndWrite(File ramlFile, File outputFile, String format) throws ConverterException {
        validateFile(ramlFile);
        
        RamlDocument ramlDoc = parser.parse(ramlFile);
        OpenApiDocument openApiDoc = coreConverter.convert(ramlDoc);
        
        // Create parent directories if needed
        if (outputFile.getParentFile() != null) {
            outputFile.getParentFile().mkdirs();
        }
        
        // Writer needs input file, output file, and format
        writer.write(openApiDoc, ramlFile, outputFile, format);
        
        logger.info("Written OpenAPI specification to: {}", outputFile.getAbsolutePath());
        return outputFile;
    }
    
    /**
     * Converts multiple RAML files to OpenAPI objects.
     *
     * @param ramlFiles list of RAML files to convert
     * @return list of OpenAPI objects
     * @throws ConverterException if conversion fails
     */
    public List<OpenAPI> convertMultiple(List<File> ramlFiles) throws ConverterException {
        if (ramlFiles == null || ramlFiles.isEmpty()) {
            throw new ConverterException("No RAML files provided");
        }
        
        List<OpenAPI> results = new ArrayList<>();
        for (File ramlFile : ramlFiles) {
            OpenAPI openApi = convertToOpenApi(ramlFile);
            results.add(openApi);
        }
        
        return results;
    }
    
    /**
     * Converts all RAML files in a directory.
     *
     * @param directory the directory containing RAML files
     * @param recursive whether to search recursively
     * @return list of OpenAPI objects
     * @throws ConverterException if conversion fails
     */
    public List<OpenAPI> convertDirectory(File directory, boolean recursive) throws ConverterException {
        List<File> ramlFiles = findRamlFiles(directory, recursive);
        if (ramlFiles.isEmpty()) {
            throw new ConverterException("No RAML files found in directory: " + directory.getAbsolutePath());
        }
        
        logger.info("Found {} RAML file(s) in directory: {}", ramlFiles.size(), directory.getAbsolutePath());
        return convertMultiple(ramlFiles);
    }
    
    /**
     * Converts all RAML files in a directory and writes to output directory.
     *
     * @param inputDir the input directory containing RAML files
     * @param outputDir the output directory
     * @param format the output format ("yaml" or "json")
     * @param recursive whether to search recursively
     * @return list of output files
     * @throws ConverterException if conversion fails
     */
    public List<File> convertAndWriteDirectory(File inputDir, File outputDir, String format, boolean recursive) 
            throws ConverterException {
        
        List<File> ramlFiles = findRamlFiles(inputDir, recursive);
        if (ramlFiles.isEmpty()) {
            throw new ConverterException("No RAML files found in directory: " + inputDir.getAbsolutePath());
        }
        
        // Create output directory
        outputDir.mkdirs();
        
        List<File> outputFiles = new ArrayList<>();
        
        for (File ramlFile : ramlFiles) {
            // Generate output file name
            String outputFileName = generateOutputFileName(ramlFile.getName(), format);
            File outputFile = new File(outputDir, outputFileName);
            
            // Convert and write
            convertAndWrite(ramlFile, outputFile, format);
            outputFiles.add(outputFile);
        }
        
        logger.info("Converted {} file(s) to: {}", outputFiles.size(), outputDir.getAbsolutePath());
        return outputFiles;
    }
    
    /**
     * Validates a RAML file without converting it.
     *
     * @param ramlFile the RAML file to validate
     * @return true if valid, false otherwise
     */
    public boolean validateRamlFile(File ramlFile) {
        try {
            validator.validate(ramlFile);
            return true;
        } catch (ConverterException e) {
            logger.error("Validation failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if strict mode is enabled.
     *
     * @return true if strict mode is enabled
     */
    public boolean isStrictMode() {
        return strictMode;
    }
    
    // Helper methods
    
    private void validateFile(File file) throws ConverterException {
        if (file == null) {
            throw new ConverterException("File cannot be null");
        }
        if (!file.exists()) {
            throw new ConverterException("File does not exist: " + file.getAbsolutePath());
        }
        if (!file.isFile()) {
            throw new ConverterException("Path is not a file: " + file.getAbsolutePath());
        }
    }
    
    private String detectFormat(File file) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".json")) {
            return "json";
        }
        return "yaml"; // Default to YAML
    }
    
    private List<File> findRamlFiles(File directory, boolean recursive) throws ConverterException {
        if (!directory.exists() || !directory.isDirectory()) {
            throw new ConverterException("Invalid directory: " + directory.getAbsolutePath());
        }
        
        try {
            Stream<Path> pathStream = recursive 
                ? Files.walk(directory.toPath())
                : Files.list(directory.toPath());
                
            return pathStream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".raml"))
                .map(Path::toFile)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ConverterException("Failed to list directory: " + e.getMessage(), e);
        }
    }
    
    private String generateOutputFileName(String ramlFileName, String format) {
        String baseName = ramlFileName;
        if (baseName.toLowerCase().endsWith(".raml")) {
            baseName = baseName.substring(0, baseName.length() - 5);
        }
        
        String extension = format.equalsIgnoreCase("json") ? ".json" : ".yaml";
        return baseName + extension;
    }
    
    /**
     * Builder for creating RamlToOpenApiConverter instances with custom configuration.
     */
    public static class Builder {
        private boolean strictMode = false;
        
        /**
         * Enables or disables strict validation mode.
         *
         * @param strict true to enable strict mode
         * @return this builder
         */
        public Builder strictMode(boolean strict) {
            this.strictMode = strict;
            return this;
        }
        
        /**
         * Builds the converter instance.
         *
         * @return a new converter instance
         */
        public RamlToOpenApiConverter build() {
            return new RamlToOpenApiConverter(strictMode);
        }
    }
}
