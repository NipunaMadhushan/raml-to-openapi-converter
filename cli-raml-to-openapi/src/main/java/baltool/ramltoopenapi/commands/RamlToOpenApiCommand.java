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
 *  KIND, eitherexpress or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package baltool.ramltoopenapi.commands;

import io.ballerina.cli.BLauncherCmd;
import org.nipunaml.ramltoopenapi.RamlToOpenApiConverter;
import org.nipunaml.ramltoopenapi.exception.ConverterException;
import org.nipunaml.ramltoopenapi.parser.RamlFileValidator;
import picocli.CommandLine;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the "raml-to-openapi" bal tool command.
 *
 * @since 1.0.0
 */
@CommandLine.Command(
        name = "raml-to-openapi",
        description = "Convert RAML files to OpenAPI specification")
public class RamlToOpenApiCommand implements BLauncherCmd {

    private static final PrintStream outStream = System.out;
    private static final PrintStream errStream = System.err;
    private static final String CMD_NAME = "raml-to-openapi";
    private static final String USAGE = "bal raml-to-openapi <raml-file-or-directory> " +
            "[-o|--output <output-file-or-directory>] " +
            "[-f|--format <yaml|json>] " +
            "[--strict]";

    @CommandLine.Parameters(description = "RAML file or directory containing RAML files", arity = "0..1")
    private String inputPath;

    @CommandLine.Option(names = {"--output", "-o"},
            description = "Output file or directory path")
    private String outputPath;

    @CommandLine.Option(names = {"--format", "-f"},
            description = "Output format: yaml or json (default: yaml)",
            defaultValue = "yaml")
    private String format;

    @CommandLine.Option(names = {"--strict"},
            description = "Enable strict mode for conversion",
            defaultValue = "false")
    private boolean strict;

    @Override
    public void execute() {
        if (inputPath == null) {
            errStream.println("Error: RAML file or directory path is required.");
            onInvalidInput();
            return;
        }

        // Validate format
        if (!format.equalsIgnoreCase("yaml") && !format.equalsIgnoreCase("json")) {
            errStream.println("Error: Invalid format '" + format + "'. Use 'yaml' or 'json'.");
            onInvalidInput();
            return;
        }

        try {
            convertRamlToOpenApi(inputPath, outputPath, format, strict);
        } catch (Exception e) {
            errStream.println("Error: " + e.getMessage());
            if (strict) {
                e.printStackTrace(errStream);
            }
            System.exit(1);
        }
    }

    private void convertRamlToOpenApi(String inputPath, String outputPath,
                                     String format, boolean strict) throws ConverterException {
        File input = new File(inputPath);

        // Validate input
        if (!input.exists()) {
            throw new ConverterException("Input path does not exist: " + inputPath);
        }

        outStream.println("╔════════════════════════════════════════════════════════════════╗");
        outStream.println("║           RAML to OpenAPI Converter                            ║");
        outStream.println("╚════════════════════════════════════════════════════════════════╝");
        outStream.println();

        // Process files
        List<File> ramlFiles = collectRamlFiles(input);

        if (ramlFiles.isEmpty()) {
            throw new ConverterException("No RAML files found in: " + inputPath);
        }

        outStream.println("Found " + ramlFiles.size() + " RAML file(s) to convert");
        outStream.println();

        // Initialize converter with facade API
        RamlToOpenApiConverter converter = RamlToOpenApiConverter.builder()
            .strictMode(strict)
            .build();

        int successCount = 0;
        int failureCount = 0;

        // Process each file
        for (File ramlFile : ramlFiles) {
            try {
                outStream.println("Converting: " + ramlFile.getName());

                // Determine output file
                File outputFile;
                if (outputPath != null) {
                    File outputDir = new File(outputPath);
                    if (ramlFiles.size() > 1 || outputDir.isDirectory()) {
                        // Multiple files or directory output
                        outputDir.mkdirs();
                        String fileName = generateFileName(ramlFile.getName(), format);
                        outputFile = new File(outputDir, fileName);
                    } else {
                        // Single file output
                        outputFile = outputDir;
                    }
                } else {
                    // No output path specified, use same directory as input
                    String fileName = generateFileName(ramlFile.getName(), format);
                    outputFile = new File(ramlFile.getParentFile(), fileName);
                }

                // Convert and write using the facade API
                File resultFile = converter.convertAndWrite(ramlFile, outputFile, format);

                outStream.println("  ✓ Success: " + resultFile.getAbsolutePath());
                successCount++;

            } catch (Exception e) {
                errStream.println("  ✗ Failed: " + e.getMessage());
                failureCount++;
                if (strict) {
                    throw new ConverterException("Conversion failed for " + ramlFile.getName(), e);
                }
            }
            outStream.println();
        }

        // Summary
        outStream.println("════════════════════════════════════════════════════════════════");
        outStream.println("Conversion Summary:");
        outStream.println("  Total files:    " + ramlFiles.size());
        outStream.println("  Successful:     " + successCount);
        outStream.println("  Failed:         " + failureCount);
        outStream.println("════════════════════════════════════════════════════════════════");

        if (failureCount > 0 && strict) {
            throw new ConverterException("Conversion completed with " + failureCount + " failure(s)");
        }
    }

    private List<File> collectRamlFiles(File input) {
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

    private void onInvalidInput() {
        errStream.println("Usage: " + USAGE);
        System.exit(1);
    }

    @Override
    public String getName() {
        return CMD_NAME;
    }

    @Override
    public void printLongDesc(StringBuilder stringBuilder) {
        stringBuilder.append("Convert RAML files to OpenAPI specification\n\n");
        stringBuilder.append("This command accepts a RAML file or directory containing RAML files as input\n");
        stringBuilder.append("and generates equivalent OpenAPI specification files in YAML or JSON format.\n\n");
        stringBuilder.append("Optional flags:\n");
        stringBuilder.append("  --output, -o             Output file or directory path\n");
        stringBuilder.append("  --format, -f             Output format: yaml or json (default: yaml)\n");
        stringBuilder.append("  --strict                 Enable strict mode (fail on any error)\n");
    }

    @Override
    public void printUsage(StringBuilder stringBuilder) {
        stringBuilder.append(USAGE).append("\n\n");
        stringBuilder.append("Examples:\n");
        stringBuilder.append("  bal raml-to-openapi api.raml\n");
        stringBuilder.append("  bal raml-to-openapi api.raml --output openapi.yaml\n");
        stringBuilder.append("  bal raml-to-openapi api.raml --format json\n");
        stringBuilder.append("  bal raml-to-openapi /path/to/raml-files --output /path/to/output\n");
        stringBuilder.append("  bal raml-to-openapi api.raml --strict\n");
    }

    @Override
    public void setParentCmdParser(CommandLine commandLine) {
    }

    // Main method for standalone execution
    public static void main(String[] args) {
        RamlToOpenApiCommand command = new RamlToOpenApiCommand();
        CommandLine cmd = new CommandLine(command);

        if (args.length == 0) {
            cmd.usage(System.out);
            System.exit(0);
        }

        try {
            cmd.parseArgs(args);
            command.execute();
        } catch (Exception e) {
            errStream.println("Error: " + e.getMessage());
            cmd.usage(System.err);
            System.exit(1);
        }
    }
}

