package org.nipunaml.ramltoopenapi.mapper;

import org.nipunaml.ramltoopenapi.model.raml.RamlDocument;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Context object passed to mappers during conversion.
 * Holds shared state and configuration for the mapping process.
 */
public class MapperContext {
    
    private final RamlDocument ramlDocument;
    private final Map<String, Object> properties;
    private final boolean strict;
    private final Set<String> schemaNames;
    
    public MapperContext(RamlDocument ramlDocument, boolean strict) {
        this.ramlDocument = ramlDocument;
        this.strict = strict;
        this.properties = new HashMap<>();
        this.schemaNames = new HashSet<>();
    }
    
    /**
     * Gets the RAML document being converted.
     */
    public RamlDocument getRamlDocument() {
        return ramlDocument;
    }
    
    /**
     * Checks if strict mode is enabled.
     */
    public boolean isStrict() {
        return strict;
    }
    
    /**
     * Sets a property in the context.
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
    
    /**
     * Gets a property from the context.
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * Gets a property with a default value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        Object value = properties.get(key);
        return value != null ? (T) value : defaultValue;
    }
    
    /**
     * Checks if a property exists.
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    /**
     * Removes a property from the context.
     */
    public void removeProperty(String key) {
        properties.remove(key);
    }
    
    /**
     * Clears all properties.
     */
    public void clearProperties() {
        properties.clear();
    }

    /**
     * Registers a schema name from components.
     */
    public void addSchemaName(String schemaName) {
        schemaNames.add(schemaName);
    }
    
    /**
     * Checks if a schema name exists in components.
     */
    public boolean hasSchema(String schemaName) {
        return schemaNames.contains(schemaName);
    }
    
    /**
     * Gets all registered schema names.
     */
    public Set<String> getSchemaNames() {
        return new HashSet<>(schemaNames);
    }
}
