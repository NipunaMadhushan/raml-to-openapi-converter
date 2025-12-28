package org.nipunaml.ramltoopenapi.writer;

import org.nipunaml.ramltoopenapi.exception.ConverterException;
import org.nipunaml.ramltoopenapi.model.openapi.OpenApiDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Generates output file paths for converted OpenAPI documents.
 */
public class OutputFileGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(OutputFileGenerator.class);
    
    /**
     * Generates output file path based on input and configuration.
     *
     * @param document the OpenAPI document
     * @param input the input file or directory
     * @param outputFile the specified output file (can be null)
     * @param format the output format (yaml or json)
     * @return the generated output file path
     * @throws ConverterException if path generation fails
     */
    public File generateOutputPath(OpenApiDocument document, File input, 
                                   File outputFile, String format) throws ConverterException {
        
        // If output file is explicitly specified, use it
        if (outputFile != null) {
            return ensureCorrectExtension(outputFile, format);
        }
        
        // Generate output file in the same directory as input
        if (input.isFile()) {
            return generateOutputForSingleFile(document, input, format);
        } else {
            return generateOutputForDirectory(document, input, format);
        }
    }
    
    /**
     * Generates output path for a single input file.
     */
    private File generateOutputForSingleFile(OpenApiDocument document, 
                                            File inputFile, String format) {
        String directory = inputFile.getParent();
        String fileName = generateFileName(document.getSourceFileName(), format);
        
        if (directory != null) {
            return new File(directory, fileName);
        } else {
            return new File(fileName);
        }
    }
    
    /**
     * Generates output path for directory input.
     */
    private File generateOutputForDirectory(OpenApiDocument document, 
                                           File inputDir, String format) {
        String fileName = generateFileName(document.getSourceFileName(), format);
        return new File(inputDir, fileName);
    }
    
    /**
     * Generates output file name from source file name.
     */
    private String generateFileName(String sourceFileName, String format) {
        String baseName = sourceFileName;
        
        // Remove .raml extension
        if (baseName.toLowerCase().endsWith(".raml")) {
            baseName = baseName.substring(0, baseName.length() - 5);
        }
        
        // Add appropriate extension
        String extension = format.equalsIgnoreCase("json") ? ".json" : ".yaml";
        return baseName + extension;
    }
    
    /**
     * Ensures the output file has the correct extension.
     */
    private File ensureCorrectExtension(File file, String format) {
        String path = file.getPath();
        String extension = format.equalsIgnoreCase("json") ? ".json" : ".yaml";
        
        if (format.equalsIgnoreCase("json")) {
            if (!path.toLowerCase().endsWith(".json")) {
                return new File(path + extension);
            }
        } else {
            if (!path.toLowerCase().endsWith(".yaml") && 
                !path.toLowerCase().endsWith(".yml")) {
                return new File(path + extension);
            }
        }
        
        return file;
    }
    
    /**
     * Creates parent directories if they don't exist.
     */
    public void ensureDirectoryExists(File file) throws ConverterException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            logger.debug("Creating directory: {}", parent.getAbsolutePath());
            if (!parent.mkdirs()) {
                throw new ConverterException(
                    "Failed to create directory: " + parent.getAbsolutePath()
                );
            }
        }
    }
    
    /**
     * Checks if file already exists and generates unique name if needed.
     */
    public File handleExistingFile(File file, boolean overwrite) {
        if (!file.exists() || overwrite) {
            return file;
        }
        
        // Generate unique file name
        String path = file.getPath();
        String extension = getExtension(path);
        String basePath = path.substring(0, path.length() - extension.length());
        
        int counter = 1;
        File uniqueFile;
        do {
            uniqueFile = new File(basePath + "_" + counter + extension);
            counter++;
        } while (uniqueFile.exists());
        
        logger.info("File exists, writing to: {}", uniqueFile.getName());
        return uniqueFile;
    }
    
    /**
     * Gets file extension including the dot.
     */
    private String getExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            return path.substring(lastDot);
        }
        return "";
    }
}
