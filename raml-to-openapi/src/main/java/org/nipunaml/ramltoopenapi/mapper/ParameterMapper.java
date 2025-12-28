package org.nipunaml.ramltoopenapi.mapper;

import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.PathParameter;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import org.nipunaml.ramltoopenapi.exception.ConverterException;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps RAML parameters to OpenAPI parameters.
 * Handles URI parameters, query parameters, and headers.
 */
public class ParameterMapper {
    
    private static final Logger logger = LoggerFactory.getLogger(ParameterMapper.class);
    
    private final TypeConverter typeConverter;
    
    public ParameterMapper(TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
    }
    
    /**
     * Maps RAML URI parameters to OpenAPI path parameters.
     */
    public List<Parameter> mapUriParameters(List<TypeDeclaration> uriParameters, 
                                           MapperContext context) throws ConverterException {
        
        if (uriParameters == null || uriParameters.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.debug("  Mapping {} URI parameter(s)", uriParameters.size());
        
        List<Parameter> parameters = new ArrayList<>();
        
        for (TypeDeclaration param : uriParameters) {
            PathParameter pathParam = new PathParameter();
            pathParam.setName(param.name());
            pathParam.setRequired(true); // Path parameters are always required
            
            // Set description
            if (param.description() != null && param.description().value() != null) {
                pathParam.setDescription(param.description().value());
            }
            
            // Set schema
            Schema<?> schema = typeConverter.convertType(param, context);
            pathParam.setSchema(schema);
            
            // Set example
            if (param.example() != null && param.example().value() != null) {
                pathParam.setExample(param.example().value());
            }
            
            parameters.add(pathParam);
            logger.debug("    Path parameter: {}", param.name());
        }
        
        return parameters;
    }
    
    /**
     * Maps RAML query parameters to OpenAPI query parameters.
     */
    public List<Parameter> mapQueryParameters(List<TypeDeclaration> queryParameters,
                                             MapperContext context) throws ConverterException {
        
        if (queryParameters == null || queryParameters.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.debug("  Mapping {} query parameter(s)", queryParameters.size());
        
        List<Parameter> parameters = new ArrayList<>();
        
        for (TypeDeclaration param : queryParameters) {
            QueryParameter queryParam = new QueryParameter();
            queryParam.setName(param.name());
            queryParam.setRequired(param.required());
            
            // Set description
            if (param.description() != null && param.description().value() != null) {
                queryParam.setDescription(param.description().value());
            }
            
            // Set schema
            Schema<?> schema = typeConverter.convertType(param, context);
            queryParam.setSchema(schema);
            
            // Set example
            if (param.example() != null && param.example().value() != null) {
                queryParam.setExample(param.example().value());
            }
            
            parameters.add(queryParam);
            logger.debug("    Query parameter: {} (required: {})", 
                param.name(), param.required());
        }
        
        return parameters;
    }
    
    /**
     * Maps RAML headers to OpenAPI header parameters.
     */
    public List<Parameter> mapHeaders(List<TypeDeclaration> headers,
                                     MapperContext context) throws ConverterException {
        
        if (headers == null || headers.isEmpty()) {
            return new ArrayList<>();
        }
        
        logger.debug("  Mapping {} header(s)", headers.size());
        
        List<Parameter> parameters = new ArrayList<>();
        
        for (TypeDeclaration header : headers) {
            HeaderParameter headerParam = new HeaderParameter();
            headerParam.setName(header.name());
            headerParam.setRequired(header.required());
            
            // Set description
            if (header.description() != null && header.description().value() != null) {
                headerParam.setDescription(header.description().value());
            }
            
            // Set schema
            Schema<?> schema = typeConverter.convertType(header, context);
            headerParam.setSchema(schema);
            
            // Set example
            if (header.example() != null && header.example().value() != null) {
                headerParam.setExample(header.example().value());
            }
            
            parameters.add(headerParam);
            logger.debug("    Header: {} (required: {})", 
                header.name(), header.required());
        }
        
        return parameters;
    }
}
