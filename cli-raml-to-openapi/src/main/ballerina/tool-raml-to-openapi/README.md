## Tool Overview
The `raml-to-openapi` tool enables the conversion of [RAML](https://raml.org/) (RESTful API Modeling Language) API specifications to [OpenAPI](https://www.openapis.org/) specifications.
It accepts either a RAML file or a directory containing RAML files as input and produces equivalent OpenAPI specification files in JSON or YAML format.

## Supported RAML Versions

The conversion tool supports RAML 1.0 specifications.

## Installation

To pull the `raml-to-openapi` tool from Ballerina Central, run the following command:
```bash
$ bal tool pull raml-to-openapi
```

## Usage

### Command Syntax

```bash
$ bal raml-to-openapi <source-file-or-directory> [-o|--out <output-directory>] [-f|--format <json|yaml>] [-v|--verbose]
```

### Parameters

- **source-file-or-directory** - *Required*. The path to the RAML file or directory containing RAML files to be converted.
- **-o or --out** - *Optional*. The directory where the converted OpenAPI specification files will be created. If not provided:
    - For a file input, the OpenAPI file is created in the same directory as the source file.
    - For a directory input, the OpenAPI files are created in the source directory.
- **-f or --format** - *Optional*. The output format for the OpenAPI specification. Supported values are `json` and `yaml`. Default is `json`.
- **-v or --verbose** - *Optional*. Enable verbose output during conversion.

### Examples

#### Convert a RAML File with Default Output Location

```bash
$ bal raml-to-openapi /path/to/api.raml
```

This will create an OpenAPI specification file in the same directory as the input RAML file.

#### Convert a RAML File with a Custom Output Location

```bash
$ bal raml-to-openapi /path/to/api.raml --out /path/to/output-dir
```

This will create an OpenAPI specification file at `/path/to/output-dir`.

#### Convert a RAML File to YAML Format

```bash
$ bal raml-to-openapi /path/to/api.raml --format yaml
```

This will create an OpenAPI specification file in YAML format instead of the default JSON format.

#### Convert All RAML Files in a Directory

```bash
$ bal raml-to-openapi /path/to/raml-directory
```

This will convert all RAML files in the specified directory and create corresponding OpenAPI specification files.

#### Convert with Verbose Output

```bash
$ bal raml-to-openapi /path/to/api.raml --verbose
```

This will convert the RAML file with detailed logging during the conversion process.

#### Convert with Custom Output Location and Format

```bash
$ bal raml-to-openapi /path/to/api.raml --out /path/to/output-dir --format yaml --verbose
```

This will create an OpenAPI specification file in YAML format at the specified output directory with verbose logging.

## Output

- For a RAML file input: An OpenAPI specification file is created with the same base name as the input file, with the extension changed to `.json` or `.yaml` based on the selected format.
    - For example, if the input file is `my-api.raml` and the format is `json`, the output file will be `my-api.json`.

- For a directory input: Each RAML file in the directory is converted to a corresponding OpenAPI specification file with the same base name.
    - For example, if you have `users-api.raml` and `products-api.raml` in the input directory, the output will be `users-api.json` and `products-api.json`.

## Conversion Details

The conversion process includes:

- **API Metadata**: Title, version, description, and base URI
- **Resource Paths**: All resource endpoints and their hierarchy
- **HTTP Methods**: GET, POST, PUT, DELETE, PATCH, and other HTTP methods
- **Request/Response Schemas**: Data types, examples, and schemas
- **Query Parameters**: Including types, descriptions, and constraints
- **Headers**: Request and response headers
- **Security Schemes**: Authentication and authorization configurations
- **Data Types**: RAML types converted to OpenAPI schemas

## Limitations

- Some advanced RAML 1.0 features may require manual adjustments in the generated OpenAPI specification.
- Custom annotations and extensions in RAML may not be fully preserved in the conversion.
- Complex data type inheritance may need manual verification.

