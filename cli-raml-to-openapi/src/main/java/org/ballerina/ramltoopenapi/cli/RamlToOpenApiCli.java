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
package org.ballerina.ramltoopenapi.cli;

import org.ballerina.ramltoopenapi.converter.RamlToOpenApiConverter;
import org.ballerina.ramltoopenapi.exception.ConverterException;
import org.ballerina.ramltoopenapi.model.openapi.OpenApiDocument;
import org.ballerina.ramltoopenapi.model.raml.RamlDocument;
import org.ballerina.ramltoopenapi.parser.RamlFileValidator;
import org.ballerina.ramltoopenapi.parser.RamlParser;
import org.ballerina.ramltoopenapi.writer.OpenApiWriter;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Command-line interface for RAML to OpenAPI converter.
 */
public class RamlToOpenApiCli {

    private static final Logger logger = Logger.getLogger(RamlToOpenApiCli.class.getName());
    private static final PrintStream outStream = System.out;
    private static final PrintStream errStream = System.err;

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 4) {
            printUsageAndExit();
        }

        String inputPath = args[0];
        String outputPath = null;
        String format = "yaml";

        // Parse arguments
        for (int i = 1; i < args.length; i++) {
            if ((args[i].equals("-o") || args[i].equals("--output")) && i + 1 < args.length) {
                outputPath = args[++i];
            } else if ((args[i].equals("-f") || args[i].equals("--format")) && i + 1 < args.length) {
                format = args[++i];
            }
        }

        // Validate format
        if (!format.equalsIgnoreCase("yaml") && !format.equalsIgnoreCase("json")) {
            logger.severe("Invalid format: " + format + ". Use 'yaml' or 'json'.");
            System.exit(1);
        }

        try {
            convertRamlToOpenApi(inputPath, outputPath, format);
            outStream.println("✓ Conversion completed successfully!");
        } catch (Exception e) {
            logger.severe("Conversion failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void convertRamlToOpenApi(String inputPath, String outputPath,
                                            String format) throws ConverterException {
        File input = new File(inputPath);

        if (!input.exists()) {
            throw new ConverterException("Input path does not exist: " + inputPath);
        }

        // Collect RAML files
        List<File> ramlFiles = collectRamlFiles(input);

        if (ramlFiles.isEmpty()) {
            throw new ConverterException("No RAML files found in: " + inputPath);
        }

        outStream.println("Found " + ramlFiles.size() + " RAML file(s) to convert");

        // Initialize components
        RamlParser parser = new RamlParser();
        RamlToOpenApiConverter converter = new RamlToOpenApiConverter(false);
        OpenApiWriter writer = new OpenApiWriter();

        // Process each file
        for (File ramlFile : ramlFiles) {
            outStream.println("Converting: " + ramlFile.getName());

            // Parse RAML
            RamlDocument ramlDoc = parser.parse(ramlFile);

            // Convert to OpenAPI
            OpenApiDocument openApiDoc = converter.convert(ramlDoc);

            // Determine output file
            File outputFile = null;
            if (outputPath != null) {
                File outputDir = new File(outputPath);
                if (ramlFiles.size() > 1 || outputDir.isDirectory()) {
                    outputDir.mkdirs();
                    String fileName = generateFileName(ramlFile.getName(), format);
                    outputFile = new File(outputDir, fileName);
                } else {
                    outputFile = outputDir;
                }
            }

            // Write output
            File resultFile = writer.write(openApiDoc, input, outputFile, format);
            outStream.println("  → " + resultFile.getAbsolutePath());
        }
    }

    private static List<File> collectRamlFiles(File input) {
        List<File> ramlFiles = new ArrayList<>();
        RamlFileValidator validator = new RamlFileValidator();

        if (input.isFile()) {
            if (validator.isRamlFile(input)) {
                ramlFiles.add(input);
            }
        } else if (input.isDirectory()) {
            File[] files = input.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && validator.isRamlFile(file)) {
                        ramlFiles.add(file);
                    }
                }
            }
        }

        return ramlFiles;
    }

    private static String generateFileName(String sourceFileName, String format) {
        String baseName = sourceFileName;

        if (baseName.toLowerCase().endsWith(".raml")) {
            baseName = baseName.substring(0, baseName.length() - 5);
        }

        String extension = format.equalsIgnoreCase("json") ? ".json" : ".yaml";
        return baseName + extension;
    }

    private static void printUsageAndExit() {
        errStream.println("Usage: java -jar raml-to-openapi.jar <raml-file-or-directory> " +
                "[-o|--output <output-path>] [-f|--format <yaml|json>]");
        errStream.println();
        errStream.println("Examples:");
        errStream.println("  java -jar raml-to-openapi.jar api.raml");
        errStream.println("  java -jar raml-to-openapi.jar api.raml -o openapi.yaml");
        errStream.println("  java -jar raml-to-openapi.jar api.raml -f json");
        errStream.println("  java -jar raml-to-openapi.jar /path/to/raml-files -o /path/to/output");
        System.exit(1);
    }
}

