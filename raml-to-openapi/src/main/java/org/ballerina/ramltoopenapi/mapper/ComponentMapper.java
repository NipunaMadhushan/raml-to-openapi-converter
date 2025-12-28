package org.ballerina.ramltoopenapi.mapper;

import org.ballerina.ramltoopenapi.exception.ConverterException;

/**
 * Base interface for all mappers.
 * Mappers convert RAML components to OpenAPI components.
 *
 * @param <S> Source type (RAML)
 * @param <T> Target type (OpenAPI)
 */
public interface ComponentMapper<S, T> {
    
    /**
     * Maps source component to target component.
     *
     * @param source the source component
     * @param context the mapper context
     * @return the mapped target component
     * @throws ConverterException if mapping fails
     */
    T map(S source, MapperContext context) throws ConverterException;
    
    /**
     * Checks if this mapper supports the given source.
     *
     * @param source the source to check
     * @return true if supported, false otherwise
     */
    default boolean supports(S source) {
        return source != null;
    }
}
