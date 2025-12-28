package org.ballerina.ramltoopenapi.mapper;

import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.ballerina.ramltoopenapi.exception.ConverterException;
import org.raml.v2.api.model.v10.bodies.Response;
import org.raml.v2.api.model.v10.datamodel.ExampleSpec;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps RAML responses to OpenAPI responses.
 * Enhanced version with header mapping and better example handling.
 */
public class ResponseMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(ResponseMapper.class);
    
    private final TypeConverter typeConverter;
    
    public ResponseMapper(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }
    
    /**
     * Maps RAML responses to OpenAPI ApiResponses.
     */
    public ApiResponses mapResponses(List<Response> ramlResponses, 
                                    MapperContext context) throws ConverterException {
        
        ApiResponses responses = new ApiResponses();
        
        if (ramlResponses == null || ramlResponses.isEmpty()) {
            // Add default response
            responses.addApiResponse("default", createDefaultResponse());
            return responses;
        }
        
        logger.debug("  Mapping {} response(s)", ramlResponses.size());
        
        for (Response ramlResponse : ramlResponses) {
            String statusCode = ramlResponse.code().value();
            ApiResponse apiResponse = mapResponse(ramlResponse, context);
            responses.addApiResponse(statusCode, apiResponse);
            logger.debug("    Response: {}", statusCode);
        }
        
        return responses;
    }
    
    /**
     * Maps a single RAML response to an OpenAPI ApiResponse.
     */
    private ApiResponse mapResponse(Response ramlResponse, MapperContext context) 
            throws ConverterException {
        
        ApiResponse apiResponse = new ApiResponse();
        
        // Set description
        String description = getResponseDescription(ramlResponse);
        apiResponse.setDescription(description);
        
        // Map response body
        if (ramlResponse.body() != null && !ramlResponse.body().isEmpty()) {
            Content content = mapResponseContent(ramlResponse.body(), context);
            apiResponse.setContent(content);
        }
        
        // Map response headers
        if (ramlResponse.headers() != null && !ramlResponse.headers().isEmpty()) {
            Map<String, Header> headers = mapResponseHeaders(ramlResponse.headers(), context);
            apiResponse.setHeaders(headers);
            logger.debug("      Mapped {} response header(s)", headers.size());
        }
        
        return apiResponse;
    }
    
    /**
     * Maps RAML response bodies to OpenAPI Content.
     */
    private Content mapResponseContent(List<TypeDeclaration> bodies, MapperContext context) 
            throws ConverterException {
        
        Content content = new Content();
        
        for (TypeDeclaration body : bodies) {
            String mediaTypeName = body.name();
            MediaType mediaType = mapMediaType(body, context);
            content.addMediaType(mediaTypeName, mediaType);
            logger.debug("      Media type: {}", mediaTypeName);
        }
        
        return content;
    }
    
    /**
     * Maps a RAML body to OpenAPI MediaType with enhanced example handling.
     */
    private MediaType mapMediaType(TypeDeclaration body, MapperContext context) 
            throws ConverterException {
        
        MediaType mediaType = new MediaType();
        
        // Convert body type to schema
        Schema<?> schema = typeConverter.convertType(body, context);
        mediaType.setSchema(schema);
        
        // Map examples (single or multiple)
        if (body.examples() != null && !body.examples().isEmpty()) {
            // Multiple examples
            Map<String, Example> examples = mapExamples(body.examples());
            mediaType.setExamples(examples);
            logger.debug("        Examples: {}", examples.size());
        } else if (body.example() != null && body.example().value() != null) {
            // Single example
            Example example = new Example();
            example.setValue(parseExampleValue(body.example().value()));
            mediaType.setExample(example.getValue());
            logger.debug("        Example: provided");
        }
        
        return mediaType;
    }
    
    /**
     * Maps multiple RAML examples to OpenAPI examples.
     */
    private Map<String, Example> mapExamples(List<ExampleSpec> ramlExamples) {
        Map<String, Example> examples = new LinkedHashMap<>();
        
        for (ExampleSpec ramlExample : ramlExamples) {
            String exampleName = ramlExample.name();
            Example example = new Example();
            
            // Set value
            if (ramlExample.value() != null) {
                example.setValue(parseExampleValue(ramlExample.value()));
            }
            
            // ExampleSpec doesn't have description() or displayName() methods
            // Just use the name as summary
            if (exampleName != null && !exampleName.isEmpty()) {
                example.setSummary(exampleName);
            }
            
            examples.put(exampleName, example);
        }
        
        return examples;
    }
        
    /**
     * Maps RAML response headers to OpenAPI headers.
     */
    private Map<String, Header> mapResponseHeaders(List<TypeDeclaration> ramlHeaders, 
                                                   MapperContext context) 
            throws ConverterException {
        
        Map<String, Header> headers = new LinkedHashMap<>();
        
        for (TypeDeclaration ramlHeader : ramlHeaders) {
            String headerName = ramlHeader.name();
            Header header = new Header();
            
            // Set description
            if (ramlHeader.description() != null && ramlHeader.description().value() != null) {
                header.setDescription(ramlHeader.description().value());
            }
            
            // Set required - response headers default to false in OpenAPI
            // Only set to true if explicitly required in RAML
            if (ramlHeader.required()) {
                header.setRequired(true);
            } else {
                header.setRequired(false);
            }

            // Set schema
            Schema<?> schema = typeConverter.convertType(ramlHeader, context);
            header.setSchema(schema);
            
            // Set example
            if (ramlHeader.example() != null && ramlHeader.example().value() != null) {
                header.setExample(ramlHeader.example().value());
            }
            
            headers.put(headerName, header);
        }
        
        return headers;
    }
    
    /**
     * Parses example value, attempting to handle JSON.
     */
    private Object parseExampleValue(String exampleValue) {
        if (exampleValue == null) {
            return null;
        }
        
        // Trim the value
        exampleValue = exampleValue.trim();
        
        // Try to detect JSON and return as-is for proper formatting
        if (exampleValue.startsWith("{") || exampleValue.startsWith("[")) {
            // It's likely JSON, return as string
            // The OpenAPI serializer will handle it properly
            return exampleValue;
        }
        
        // Try to parse as number
        try {
            if (exampleValue.contains(".")) {
                return Double.parseDouble(exampleValue);
            } else {
                return Integer.parseInt(exampleValue);
            }
        } catch (NumberFormatException e) {
            // Not a number
        }
        
        // Try to parse as boolean
        if (exampleValue.equalsIgnoreCase("true")) {
            return true;
        } else if (exampleValue.equalsIgnoreCase("false")) {
            return false;
        }
        
        // Return as string
        return exampleValue;
    }
    
    /**
     * Gets response description with fallback.
     */
    private String getResponseDescription(Response ramlResponse) {
        if (ramlResponse.description() != null && 
            ramlResponse.description().value() != null) {
            return ramlResponse.description().value();
        }
        
        // Fallback based on status code
        String code = ramlResponse.code().value();
        return getDefaultDescriptionForCode(code);
    }
    
    /**
     * Gets default description based on HTTP status code.
     */
    private String getDefaultDescriptionForCode(String code) {
        switch (code) {
            case "200": return "Successful response";
            case "201": return "Created";
            case "202": return "Accepted";
            case "204": return "No content";
            case "400": return "Bad request";
            case "401": return "Unauthorized";
            case "403": return "Forbidden";
            case "404": return "Not found";
            case "409": return "Conflict";
            case "422": return "Unprocessable entity";
            case "500": return "Internal server error";
            case "502": return "Bad gateway";
            case "503": return "Service unavailable";
            default: return "Response";
        }
    }
    
    /**
     * Creates a default response when none are specified.
     */
    private ApiResponse createDefaultResponse() {
        ApiResponse response = new ApiResponse();
        response.setDescription("Successful response");
        return response;
    }
}
