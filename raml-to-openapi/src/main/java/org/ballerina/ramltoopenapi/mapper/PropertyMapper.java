package org.ballerina.ramltoopenapi.mapper;

import io.swagger.v3.oas.models.media.Schema;
import org.ballerina.ramltoopenapi.exception.ConverterException;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps RAML property declarations to OpenAPI schema properties.
 */
public class PropertyMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(PropertyMapper.class);
    
    private final TypeConverter typeConverter;
    
    public PropertyMapper(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }
    
    /**
     * Maps a RAML property to an OpenAPI schema.
     */
    public Schema<?> mapProperty(TypeDeclaration property, MapperContext context) 
            throws ConverterException {
        
        logger.debug("  Mapping property: {}", property.name());
        
        // Convert the property type to schema
        Schema<?> schema = typeConverter.convertType(property, context);
        
        // Set description
        if (property.description() != null && property.description().value() != null) {
            schema.setDescription(property.description().value());
        }
        
        // Set example
        if (property.example() != null && property.example().value() != null) {
            schema.setExample(property.example().value());
        }
        
        // Set default value
        if (property.defaultValue() != null) {
            schema.setDefault(property.defaultValue());
        }
        
        // Add RAML-specific extensions
        addRamlExtensions(property, schema);
        
        return schema;
    }
    
    /**
     * Extracts required properties from a list of properties.
     */
    public List<String> extractRequiredProperties(List<TypeDeclaration> properties) {
        List<String> required = new ArrayList<>();
        
        for (TypeDeclaration property : properties) {
            if (property.required()) {
                required.add(property.name());
                logger.debug("  Required property: {}", property.name());
            }
        }
        
        return required.isEmpty() ? null : required;
    }
    
    /**
     * Adds RAML-specific extensions to the schema.
     */
    private void addRamlExtensions(TypeDeclaration property, Schema<?> schema) {
        // Add displayName if different from name
        if (property.displayName() != null && 
            !property.displayName().equals(property.name())) {
            schema.addExtension("x-raml-displayName", property.displayName());
        }
        
        // Add annotations as extensions
        if (property.annotations() != null && !property.annotations().isEmpty()) {
            property.annotations().forEach(annotation -> {
                String name = annotation.annotation().name();
                Object value = annotation.structuredValue() != null ? 
                    annotation.structuredValue().value() : null;
                if (value != null) {
                    schema.addExtension("x-raml-annotation-" + name, value);
                }
            });
        }
    }
}
