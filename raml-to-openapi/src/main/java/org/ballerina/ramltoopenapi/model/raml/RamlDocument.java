package org.ballerina.ramltoopenapi.model.raml;

import org.raml.v2.api.model.v10.api.Api;

/**
 * Internal model representing a parsed RAML document.
 * This wraps the RAML parser's Api object and provides additional metadata.
 */
public class RamlDocument {
    
    private final Api api;
    private final String sourceFile;
    private final String fileName;
    
    public RamlDocument(Api api, String sourceFile) {
        this.api = api;
        this.sourceFile = sourceFile;
        this.fileName = extractFileName(sourceFile);
    }
    
    /**
     * Gets the RAML API object.
     */
    public Api getApi() {
        return api;
    }
    
    /**
     * Gets the source file path.
     */
    public String getSourceFile() {
        return sourceFile;
    }
    
    /**
     * Gets the file name.
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Gets the API title from RAML.
     */
    public String getTitle() {
        return api.title() != null ? api.title().value() : "Untitled API";
    }
    
    /**
     * Gets the API version from RAML.
     */
    public String getVersion() {
        return api.version() != null ? api.version().value() : "1.0";
    }
    
    /**
     * Gets the API description from RAML.
     */
    public String getDescription() {
        return api.description() != null ? api.description().value() : null;
    }
    
    /**
     * Gets the base URI from RAML.
     */
    public String getBaseUri() {
        return api.baseUri() != null ? api.baseUri().value() : null;
    }
    
    /**
     * Gets the media type from RAML.
     */
    public String getMediaType() {
        return api.mediaType() != null && !api.mediaType().isEmpty() 
            ? api.mediaType().get(0).value() 
            : null;
    }
    
    /**
     * Checks if the API has resources.
     */
    public boolean hasResources() {
        return api.resources() != null && !api.resources().isEmpty();
    }
    
    /**
     * Gets the number of resources.
     */
    public int getResourceCount() {
        return api.resources() != null ? api.resources().size() : 0;
    }
    
    /**
     * Checks if the API has types/schemas.
     */
    public boolean hasTypes() {
        return api.types() != null && !api.types().isEmpty();
    }
    
    /**
     * Gets the number of types.
     */
    public int getTypeCount() {
        return api.types() != null ? api.types().size() : 0;
    }
    
    /**
     * Checks if the API has security schemes.
     */
    public boolean hasSecuritySchemes() {
        return api.securitySchemes() != null && !api.securitySchemes().isEmpty();
    }
    
    /**
     * Gets basic summary of the RAML document.
     */
    public String getSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Title: ").append(getTitle()).append("\n");
        summary.append("Version: ").append(getVersion()).append("\n");
        
        if (getBaseUri() != null) {
            summary.append("Base URI: ").append(getBaseUri()).append("\n");
        }
        
        summary.append("Resources: ").append(getResourceCount()).append("\n");
        summary.append("Types: ").append(getTypeCount()).append("\n");
        
        if (hasSecuritySchemes()) {
            summary.append("Security Schemes: ").append(api.securitySchemes().size());
        }
        
        return summary.toString();
    }
    
    /**
     * Extracts file name from full path.
     */
    private String extractFileName(String path) {
        if (path == null) {
            return "unknown";
        }
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
    
    @Override
    public String toString() {
        return String.format("RamlDocument{file='%s', title='%s', version='%s', resources=%d}", 
            fileName, getTitle(), getVersion(), getResourceCount());
    }
}
