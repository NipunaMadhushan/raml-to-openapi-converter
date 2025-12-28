package org.nipunaml.ramltoopenapi.writer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import org.nipunaml.ramltoopenapi.exception.ConverterException;
import org.nipunaml.ramltoopenapi.model.openapi.OpenApiDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * Writes OpenAPI documents to YAML or JSON files.
 */
public class OpenApiWriter {
    
    private static final Logger logger = LoggerFactory.getLogger(OpenApiWriter.class);
    
    private final OutputFileGenerator fileGenerator;
    
    public OpenApiWriter() {
        this.fileGenerator = new OutputFileGenerator();
    }
    
    /**
     * Writes an OpenAPI document to a file.
     *
     * @param document the OpenAPI document to write
     * @param input the input file or directory
     * @param outputFile the output file (can be null for auto-generation)
     * @param format the output format (yaml or json)
     * @return the written file
     * @throws ConverterException if writing fails
     */
    public File write(OpenApiDocument document, File input, File outputFile, 
                     String format) throws ConverterException {
        
        if (document == null) {
            throw new ConverterException("Cannot write null OpenAPI document");
        }
        
        // Generate output file path
        File targetFile = fileGenerator.generateOutputPath(document, input, outputFile, format);
        
        // Ensure parent directory exists
        fileGenerator.ensureDirectoryExists(targetFile);
        
        // Handle existing file
        targetFile = fileGenerator.handleExistingFile(targetFile, true);
        
        logger.info("Writing OpenAPI to: {}", targetFile.getName());
        logger.debug("  Format: {}", format.toUpperCase());
        logger.debug("  Path: {}", targetFile.getAbsolutePath());
        
        try {
            if (format.equalsIgnoreCase("json")) {
                writeJson(document.getOpenApi(), targetFile);
            } else {
                writeYaml(document.getOpenApi(), targetFile);
            }
            
            logger.info("✓ Successfully written: {} ({} bytes)", 
                targetFile.getName(), targetFile.length());
            
            return targetFile;
            
        } catch (IOException e) {
            throw new ConverterException(
                "Failed to write OpenAPI file '" + targetFile.getName() + "': " + e.getMessage(), 
                e
            );
        }
    }
    
    /**
     * Writes OpenAPI as YAML.
     */
    private void writeYaml(OpenAPI openApi, File file) throws IOException {
        logger.debug("Writing YAML format...");
        
        // Create YAML mapper with custom configuration
        YAMLFactory yamlFactory = new YAMLFactory()
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .enable(YAMLGenerator.Feature.INDENT_ARRAYS_WITH_INDICATOR);
        
        ObjectMapper yamlMapper = new ObjectMapper(yamlFactory);
        configureMapper(yamlMapper);
        
        // Write to string first
        String yamlContent = yamlMapper.writeValueAsString(openApi);
        
        // Post-process: Force quotes on version field
        yamlContent = forceQuoteVersion(yamlContent);
        
        // Write to file
        Files.writeString(file.toPath(), yamlContent, 
            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Post-processes YAML to ensure version field is quoted.
     * Handles patterns like:
     *   version: 2.0  ->  version: "2.0"
     *   version: 1.0  ->  version: "1.0"
     */
    private String forceQuoteVersion(String yamlContent) {
        // Match "version: " followed by unquoted value (number or string)
        // This regex finds:  version: <value>  where value is not already quoted
        return yamlContent.replaceAll(
            "(?m)^(\\s*version:\\s+)([^\"'\\n]+)$",
            "$1\'$2\'"
        );
    }
    
    /**
     * Writes OpenAPI as JSON.
     */
    public void writeJson(OpenAPI openApi, File file) throws IOException {
        logger.debug("Writing JSON format...");
        
        // Create JSON mapper
        ObjectMapper jsonMapper = Json.mapper();
        configureMapper(jsonMapper);
        
        // Write to file with pretty printing
        jsonMapper.writerWithDefaultPrettyPrinter().writeValue(file, openApi);
    }
    
    /**
     * Configures the ObjectMapper with common settings.
     */
    private void configureMapper(ObjectMapper mapper) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        
        // Filter out internal Swagger properties
        mapper.addMixIn(io.swagger.v3.oas.models.media.Schema.class, SchemaPropertyFilter.class);
        mapper.addMixIn(io.swagger.v3.oas.models.media.MediaType.class, MediaTypePropertyFilter.class);

        // ADD THIS LINE - Control OpenAPI property order
        mapper.addMixIn(io.swagger.v3.oas.models.OpenAPI.class, OpenAPIPropertyOrder.class);
    }

    /**
     * Mixin to control the order of OpenAPI properties in output.
     */
    @com.fasterxml.jackson.annotation.JsonPropertyOrder({
        "openapi",
        "info",
        "servers",
        "components",
        "paths",
        "security",
        "tags",
        "externalDocs"
    })
    private abstract static class OpenAPIPropertyOrder {
    }

    /**
     * Mixin to filter out internal Schema properties.
     */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({
        "exampleSetFlag", 
        "types",
        "name",
        "specVersion"
    })
    private abstract static class SchemaPropertyFilter {
    }

    /**
     * Mixin to filter out internal MediaType properties.
     */
    @JsonIgnoreProperties({
        "exampleSetFlag"
    })
    private abstract static class MediaTypePropertyFilter {
    }
    
    /**
     * Writes multiple OpenAPI documents.
     *
     * @param documents list of OpenAPI documents
     * @param input the input file or directory
     * @param outputFile the output file (only used if single document)
     * @param format the output format
     * @return list of written files
     * @throws ConverterException if writing fails
     */
    public java.util.List<File> writeMultiple(java.util.List<OpenApiDocument> documents,
                                              File input, File outputFile, 
                                              String format) throws ConverterException {
        
        if (documents == null || documents.isEmpty()) {
            throw new ConverterException("No OpenAPI documents to write");
        }
        
        logger.info("Writing {} OpenAPI document(s)...", documents.size());
        
        java.util.List<File> writtenFiles = new java.util.ArrayList<>();
        
        for (int i = 0; i < documents.size(); i++) {
            OpenApiDocument doc = documents.get(i);
            
            // Only use outputFile if there's a single document
            File output = (documents.size() == 1) ? outputFile : null;
            
            try {
                File writtenFile = write(doc, input, output, format);
                writtenFiles.add(writtenFile);
            } catch (ConverterException e) {
                logger.error("✗ Failed to write: {}", doc.getSourceFileName());
                throw new ConverterException(
                    "Failed to write document '" + doc.getSourceFileName() + "': " + e.getMessage(),
                    e
                );
            }
        }
        
        logger.info("✓ Successfully written all {} file(s)", writtenFiles.size());
        
        return writtenFiles;
    }
    
    /**
     * Gets a preview of the OpenAPI content.
     */
    public String getPreview(OpenAPI openApi, String format, int maxLines) throws ConverterException {
        try {
            String content;
            if (format.equalsIgnoreCase("json")) {
                ObjectMapper jsonMapper = Json.mapper();
                configureMapper(jsonMapper);
                content = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(openApi);
            } else {
                YAMLFactory yamlFactory = new YAMLFactory()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
                ObjectMapper yamlMapper = new ObjectMapper(yamlFactory);
                configureMapper(yamlMapper);
                content = yamlMapper.writeValueAsString(openApi);
            }
            
            // Return first N lines
            String[] lines = content.split("\n");
            int lineCount = Math.min(lines.length, maxLines);
            StringBuilder preview = new StringBuilder();
            
            for (int i = 0; i < lineCount; i++) {
                preview.append(lines[i]).append("\n");
            }
            
            if (lines.length > maxLines) {
                preview.append("... (").append(lines.length - maxLines)
                       .append(" more lines)\n");
            }
            
            return preview.toString();
            
        } catch (Exception e) {
            throw new ConverterException("Failed to generate preview: " + e.getMessage(), e);
        }
    }
}
