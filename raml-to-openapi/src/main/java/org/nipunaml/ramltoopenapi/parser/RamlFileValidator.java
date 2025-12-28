package org.nipunaml.ramltoopenapi.parser;

import org.nipunaml.ramltoopenapi.exception.ConverterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;


/**
 * Validates RAML files for basic correctness.
 */
public class RamlFileValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(RamlFileValidator.class);
    
    // RAML header pattern: #%RAML 0.8 or #%RAML 1.0
    private static final Pattern RAML_HEADER_PATTERN = Pattern.compile("^#%RAML\\s+(0\\.8|1\\.0)\\s*$");
    
    // Supported RAML version
    private static final String SUPPORTED_VERSION = "1.0";
    
    /**
     * Validates if the file is a valid RAML file.
     *
     * @param file the file to validate
     * @throws ConverterException if the file is not valid
     */
    public void validate(File file) throws ConverterException {
        if (file == null) {
            throw new ConverterException("File cannot be null");
        }
        
        if (!file.exists()) {
            throw new ConverterException("File does not exist: " + file.getName());
        }
        
        if (!file.isFile()) {
            throw new ConverterException("Not a file: " + file.getName());
        }
        
        if (!file.canRead()) {
            throw new ConverterException("Cannot read file (permission denied): " + file.getName());
        }
        
        // Check file extension
        if (!hasRamlExtension(file)) {
            throw new ConverterException("File '" + file.getName() + "' does not have .raml extension");
        }
        
        // Validate RAML header
        validateRamlHeader(file);
    }
    
    /**
     * Checks if file has .raml extension.
     */
    private boolean hasRamlExtension(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".raml");
    }
    
    /**
     * Validates the RAML header in the file.
     */
    private void validateRamlHeader(File file) throws ConverterException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            
            if (firstLine == null) {
                throw new ConverterException("File '" + file.getName() + "' is empty");
            }
            
            firstLine = firstLine.trim();
            
            if (!RAML_HEADER_PATTERN.matcher(firstLine).matches()) {
                throw new ConverterException(
                    "Invalid RAML header in file '" + file.getName() + "'\n" +
                    "  Expected: #%RAML 1.0\n" +
                    "  Found: " + firstLine
                );
            }
            
            // Extract version
            String version = extractVersion(firstLine);
            
            if (!SUPPORTED_VERSION.equals(version)) {
                throw new ConverterException(
                    "Unsupported RAML version in file '" + file.getName() + "'\n" +
                    "  Found: RAML " + version + "\n" +
                    "  Supported: RAML 1.0 only"
                );
            }
            
            logger.debug("✓ Valid RAML {} file: {}", version, file.getName());
            
        } catch (IOException e) {
            throw new ConverterException("Error reading file '" + file.getName() + "': " + e.getMessage());
        }
    }
    
    /**
     * Extracts RAML version from header line.
     */
    private String extractVersion(String headerLine) {
        // Extract version from "#%RAML 1.0"
        String[] parts = headerLine.split("\\s+");
        if (parts.length >= 2) {
            return parts[1];
        }
        return "";
    }
    
    /**
     * Quick check if file appears to be a RAML file (without full validation).
     */
    public boolean isRamlFile(File file) {
        if (file == null || !file.isFile() || !file.canRead()) {
            return false;
        }
        return hasRamlExtension(file);
    }
}
