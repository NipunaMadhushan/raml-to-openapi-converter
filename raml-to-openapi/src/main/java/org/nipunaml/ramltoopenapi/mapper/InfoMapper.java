package org.nipunaml.ramltoopenapi.mapper;

import io.swagger.v3.oas.models.info.Info;
import org.nipunaml.ramltoopenapi.exception.ConverterException;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.api.DocumentationItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maps RAML API info to OpenAPI Info object.
 * Handles title, version, description, and other metadata.
 */
public class InfoMapper implements ComponentMapper<Api, Info> {
    
    private static final Logger logger = LoggerFactory.getLogger(InfoMapper.class);
    
    @Override
    public Info map(Api source, MapperContext context) throws ConverterException {
        if (source == null) {
            throw new ConverterException("Cannot map null API to Info");
        }
        
        logger.debug("Mapping RAML API info to OpenAPI Info");
        
        Info info = new Info();
        
        // Map title (required in OpenAPI)
        String title = source.title() != null ? source.title().value() : "Untitled API";
        info.setTitle(title);
        logger.debug("  Title: {}", title);
        
        // Map version (required in OpenAPI)
        if (source.version() != null && source.version().value() != null) {
            String version = source.version().value();
            // Force string type even for numeric-looking versions
            info.setVersion(version);
        } else {
            info.setVersion("1.0.0");
        }
        
        // Map description (optional)
        if (source.description() != null && source.description().value() != null) {
            String description = source.description().value();
            info.setDescription(description);
            logger.debug("  Description: {} chars", description.length());
        }
        
        // Map documentation to termsOfService or description extension
        if (source.documentation() != null && !source.documentation().isEmpty()) {
            mapDocumentation(source, info);
        }
        
        // Add extension for media type if specified
        if (source.mediaType() != null && !source.mediaType().isEmpty()) {
            info.addExtension("x-raml-mediaType", source.mediaType().get(0).value());
        }
        
        logger.debug("✓ Info mapping completed");
        return info;
    }
    
    /**
     * Maps RAML documentation to OpenAPI info.
     * Can be extended to map to contact, license, etc.
     */
    private void mapDocumentation(Api source, Info info) {
        for (DocumentationItem docItem : source.documentation()) {
            String docTitle = docItem.title() != null ? docItem.title().value() : "";
            String docContent = docItem.content() != null ? docItem.content().value() : "";
            
            logger.debug("  Documentation: {}", docTitle);
            
            // Add documentation as extension for now
            // In future, could parse for contact/license info
            if (!docTitle.isEmpty()) {
                info.addExtension("x-raml-documentation-" + sanitizeExtensionName(docTitle), 
                                docContent);
            }
        }
    }
    
    /**
     * Sanitizes a string to be used as an extension name.
     */
    private String sanitizeExtensionName(String name) {
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9-]", "-")
                   .replaceAll("-+", "-")
                   .replaceAll("^-|-$", "");
    }
}
