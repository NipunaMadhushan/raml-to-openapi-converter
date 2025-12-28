package org.ballerina.ramltoopenapi.mapper;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import org.ballerina.ramltoopenapi.exception.ConverterException;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.datamodel.StringTypeDeclaration;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Maps RAML baseUri to OpenAPI Server objects.
 * Handles base URI variables and protocol extraction.
 */
public class ServerMapper implements ComponentMapper<Api, List<Server>> {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerMapper.class);
    
    // Pattern to match URI variables like {version} or {environment}
    private static final Pattern URI_VARIABLE_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    @Override
    public List<Server> map(Api source, MapperContext context) throws ConverterException {
        if (source == null) {
            throw new ConverterException("Cannot map null API to Servers");
        }
        
        logger.debug("Mapping RAML baseUri to OpenAPI Servers");
        
        List<Server> servers = new ArrayList<>();
        
        // Get base URI
        String baseUri = source.baseUri() != null ? source.baseUri().value() : null;
        
        if (baseUri == null || baseUri.trim().isEmpty()) {
            logger.warn("No baseUri specified in RAML. Using default server.");
            servers.add(createDefaultServer());
            return servers;
        }
        
        logger.debug("  Base URI: {}", baseUri);
        
        // Create server from base URI
        Server server = new Server();
        server.setUrl(baseUri);
        
        // Extract and map URI parameters to server variables
        if (source.baseUriParameters() != null && !source.baseUriParameters().isEmpty()) {
            ServerVariables variables = mapBaseUriParameters(source.baseUriParameters());
            if (!variables.isEmpty()) {
                server.setVariables(variables);
                logger.debug("  Mapped {} server variable(s)", variables.size());
            }
        }
        
        // Add description if available
        if (source.description() != null && source.description().value() != null) {
            server.setDescription("Base server for " + 
                (source.title() != null ? source.title().value() : "API"));
        }
        
        servers.add(server);
        
        logger.debug("✓ Server mapping completed");
        return servers;
    }
    
    /**
     * Maps RAML base URI parameters to OpenAPI server variables.
     */
    private ServerVariables mapBaseUriParameters(List<TypeDeclaration> parameters) {
        ServerVariables variables = new ServerVariables();
        
        for (TypeDeclaration param : parameters) {
            String paramName = param.name();
            ServerVariable variable = new ServerVariable();
            
            // Set description
            if (param.description() != null && param.description().value() != null) {
                variable.setDescription(param.description().value());
            }
            
            // Set default value
            String defaultValue = getDefaultValue(param);
            if (defaultValue != null) {
                variable._default(defaultValue);
                logger.debug("    Variable '{}' default: {}", paramName, defaultValue);
            } else {
                // If no default, try to infer from name
                variable._default(getDefaultValueForParameter(paramName));
            }
            
            // Set enum values if available
            List<String> enumValues = getEnumValues(param);
            if (enumValues != null && !enumValues.isEmpty()) {
                variable.setEnum(enumValues);
                logger.debug("    Variable '{}' enum: {}", paramName, enumValues);
            }
            
            variables.addServerVariable(paramName, variable);
        }
        
        return variables;
    }
    
    /**
     * Gets the default value from a type declaration.
     */
    private String getDefaultValue(TypeDeclaration param) {
        if (param.defaultValue() != null) {
            return param.defaultValue();
        }
        
        // Try to get from example
        if (param.example() != null && param.example().value() != null) {
            return param.example().value();
        }
        
        return null;
    }
    
    /**
     * Gets enum values from a type declaration.
     */
    private List<String> getEnumValues(TypeDeclaration param) {
        List<String> enumValues = new ArrayList<>();
        
        // Check if it's a StringTypeDeclaration (which can have enum values)
        if (param instanceof StringTypeDeclaration) {
            StringTypeDeclaration stringType = (StringTypeDeclaration) param;
            if (stringType.enumValues() != null && !stringType.enumValues().isEmpty()) {
                for (String value : stringType.enumValues()) {
                    enumValues.add(value);
                }
            }
        }
        
        return enumValues.isEmpty() ? null : enumValues;
    }
    
    /**
     * Gets a default value for a parameter based on common naming conventions.
     */
    private String getDefaultValueForParameter(String paramName) {
        String lowerName = paramName.toLowerCase();
        
        if (lowerName.contains("version")) {
            return "v1";
        } else if (lowerName.contains("environment") || lowerName.contains("env")) {
            return "production";
        } else if (lowerName.contains("region")) {
            return "us-east-1";
        } else if (lowerName.contains("host")) {
            return "api.example.com";
        }
        
        return "default";
    }
    
    /**
     * Creates a default server when no baseUri is specified.
     */
    private Server createDefaultServer() {
        Server server = new Server();
        server.setUrl("/");
        server.setDescription("Default server");
        return server;
    }
}
