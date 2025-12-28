package org.ballerina.ramltoopenapi.model.openapi;

import io.swagger.v3.oas.models.OpenAPI;

/**
 * Internal model representing a converted OpenAPI document.
 * Wraps the OpenAPI object with additional metadata.
 */
public class OpenApiDocument {
    
    private final OpenAPI openApi;
    private final String sourceFileName;
    private final String targetFileName;
    
    public OpenApiDocument(OpenAPI openApi, String sourceFileName, String targetFileName) {
        this.openApi = openApi;
        this.sourceFileName = sourceFileName;
        this.targetFileName = targetFileName;
    }
    
    /**
     * Gets the OpenAPI object.
     */
    public OpenAPI getOpenApi() {
        return openApi;
    }
    
    /**
     * Gets the source RAML file name.
     */
    public String getSourceFileName() {
        return sourceFileName;
    }
    
    /**
     * Gets the target OpenAPI file name.
     */
    public String getTargetFileName() {
        return targetFileName;
    }
    
    /**
     * Gets the OpenAPI version.
     */
    public String getOpenApiVersion() {
        return openApi.getOpenapi() != null ? openApi.getOpenapi() : "3.0.0";
    }
    
    /**
     * Gets the API title.
     */
    public String getTitle() {
        return openApi.getInfo() != null ? openApi.getInfo().getTitle() : "Untitled";
    }
    
    /**
     * Gets the API version.
     */
    public String getVersion() {
        return openApi.getInfo() != null ? openApi.getInfo().getVersion() : "1.0.0";
    }
    
    @Override
    public String toString() {
        return String.format("OpenApiDocument{source='%s', target='%s', title='%s', version='%s'}", 
            sourceFileName, targetFileName, getTitle(), getVersion());
    }
}
