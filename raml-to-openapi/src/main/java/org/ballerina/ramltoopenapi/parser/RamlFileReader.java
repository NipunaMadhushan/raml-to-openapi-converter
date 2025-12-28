package org.ballerina.ramltoopenapi.parser;

import org.ballerina.ramltoopenapi.exception.ConverterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Reads and scans RAML files from file system.
 * Handles both single file and directory inputs.
 */
public class RamlFileReader {
    
    private static final Logger logger = LoggerFactory.getLogger(RamlFileReader.class);
    
    private final RamlFileValidator validator;
    
    public RamlFileReader() {
        this.validator = new RamlFileValidator();
    }
    
    /**
     * Reads RAML files from the given input.
     * If input is a file, returns a list with that single file.
     * If input is a directory, scans for RAML files.
     *
     * @param input     the input file or directory
     * @param recursive whether to scan directories recursively
     * @return list of RAML files found
     * @throws ConverterException if reading fails
     */
    public List<File> readFiles(File input, boolean recursive) throws ConverterException {
        if (input == null) {
            throw new ConverterException("Input cannot be null");
        }
        
        if (!input.exists()) {
            throw new ConverterException("Input path does not exist: " + input.getAbsolutePath());
        }
        
        if (input.isFile()) {
            return readSingleFile(input);
        } else if (input.isDirectory()) {
            return scanDirectory(input, recursive);
        } else {
            throw new ConverterException("Input is neither a file nor a directory: " + input.getAbsolutePath());
        }
    }
    
    /**
     * Reads and validates a single RAML file.
     */
    private List<File> readSingleFile(File file) throws ConverterException {
        logger.debug("Reading RAML file: {}", file.getAbsolutePath());
        
        validator.validate(file);
        
        logger.debug("✓ File validated successfully");
        return Collections.singletonList(file);
    }
    
    /**
     * Scans a directory for RAML files.
     */
    private List<File> scanDirectory(File directory, boolean recursive) throws ConverterException {
        logger.debug("Scanning directory: {}", directory.getAbsolutePath());
        logger.debug("Recursive: {}", recursive);
        
        List<File> ramlFiles = new ArrayList<>();
        
        try {
            if (recursive) {
                ramlFiles = scanRecursively(directory.toPath());
            } else {
                ramlFiles = scanNonRecursively(directory.toPath());
            }
        } catch (IOException e) {
            throw new ConverterException(
                "Error scanning directory '" + directory.getName() + "': " + e.getMessage()
            );
        }
        
        if (ramlFiles.isEmpty()) {
            String message = "No RAML files found in directory: " + directory.getName();
            if (!recursive) {
                message += "\nTip: Use -r or --recursive flag to search in subdirectories";
            }
            throw new ConverterException(message);
        }
        
        logger.debug("Found {} RAML file(s)", ramlFiles.size());
        
        // Validate all found files
        validateFiles(ramlFiles);
        
        return ramlFiles;
    }
    
    /**
     * Scans directory recursively for RAML files.
     */
    private List<File> scanRecursively(Path directory) throws IOException {
        logger.debug("Scanning recursively...");
        
        try (Stream<Path> paths = Files.walk(directory)) {
            List<File> files = paths
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(validator::isRamlFile)
                .collect(Collectors.toList());
            
            logger.debug("Found {} RAML file(s) in recursive scan", files.size());
            return files;
        }
    }
    
    /**
     * Scans directory non-recursively (only immediate children).
     */
    private List<File> scanNonRecursively(Path directory) throws IOException {
        logger.debug("Scanning directory (non-recursive)...");
        
        try (Stream<Path> paths = Files.list(directory)) {
            List<File> files = paths
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(validator::isRamlFile)
                .collect(Collectors.toList());
            
            logger.debug("Found {} RAML file(s) in directory", files.size());
            return files;
        }
    }
    
    /**
     * Validates all files in the list.
     */
    private void validateFiles(List<File> files) throws ConverterException {
        logger.debug("Validating {} file(s)...", files.size());
        
        List<String> invalidFiles = new ArrayList<>();
        
        for (File file : files) {
            try {
                validator.validate(file);
                logger.debug("✓ {}", file.getName());
            } catch (ConverterException e) {
                logger.debug("✗ {}: {}", file.getName(), e.getMessage());
                invalidFiles.add("  • " + file.getName() + ": " + e.getMessage());
            }
        }
        
        if (!invalidFiles.isEmpty()) {
            String errorMessage = "Found " + invalidFiles.size() + " invalid RAML file(s):\n" + 
                                String.join("\n", invalidFiles);
            throw new ConverterException(errorMessage);
        }
        
        logger.debug("All files validated successfully");
    }
    
    /**
     * Gets file information for logging/reporting.
     */
    public FileInfo getFileInfo(File file) {
        return new FileInfo(
            file.getName(),
            file.getAbsolutePath(),
            file.length(),
            file.getParent()
        );
    }
    
    /**
     * Simple DTO for file information.
     */
    public static class FileInfo {
        private final String name;
        private final String absolutePath;
        private final long size;
        private final String parentDirectory;
        
        public FileInfo(String name, String absolutePath, long size, String parentDirectory) {
            this.name = name;
            this.absolutePath = absolutePath;
            this.size = size;
            this.parentDirectory = parentDirectory;
        }
        
        public String getName() {
            return name;
        }
        
        public String getAbsolutePath() {
            return absolutePath;
        }
        
        public long getSize() {
            return size;
        }
        
        public String getParentDirectory() {
            return parentDirectory;
        }
        
        @Override
        public String toString() {
            return String.format("%s (%d bytes)", name, size);
        }
    }
}
