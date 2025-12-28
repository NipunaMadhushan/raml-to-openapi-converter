package org.ballerina.ramltoopenapi.mapper;

import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.ballerina.ramltoopenapi.exception.ConverterException;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.security.SecuritySchemeRef;
import org.raml.v2.api.model.v10.system.types.AnnotableStringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps RAML security schemes to OpenAPI security components.
 * Handles OAuth 2.0, API Keys, Basic Auth, and other security schemes.
 */
public class SecurityMapper
        implements ComponentMapper<Api, Map<String, SecurityScheme>> {

    private static final Logger logger = LoggerFactory.getLogger(SecurityMapper.class);
    
    @Override
    public Map<String, SecurityScheme> map(Api source, MapperContext context)
            throws ConverterException {
        
        if (source == null) {
            throw new ConverterException("Cannot map null API to security schemes");
        }
        
        logger.debug("Mapping RAML security schemes to OpenAPI");
        
        Map<String, io.swagger.v3.oas.models.security.SecurityScheme> securitySchemes = new LinkedHashMap<>();
        
        // Map all security scheme definitions
        if (source.securitySchemes() != null && !source.securitySchemes().isEmpty()) {
            logger.debug("Found {} security scheme(s)", source.securitySchemes().size());
            
            for (org.raml.v2.api.model.v10.security.SecurityScheme ramlScheme : source.securitySchemes()) {
                String schemeName = ramlScheme.name();
                logger.debug("Mapping security scheme: {}", schemeName);
                
                try {
                    io.swagger.v3.oas.models.security.SecurityScheme openApiScheme = 
                        mapSecurityScheme(ramlScheme, context);
                    securitySchemes.put(schemeName, openApiScheme);
                    logger.debug("✓ Mapped security scheme: {}", schemeName);
                } catch (Exception e) {
                    String message = "Failed to map security scheme '" + schemeName + "': " + 
                                   e.getMessage();
                    if (context.isStrict()) {
                        throw new ConverterException(message, e);
                    } else {
                        logger.warn("⚠ {}", message);
                    }
                }
            }
        } else {
            logger.debug("No security schemes defined in RAML");
        }
        
        logger.debug("✓ Security mapping completed: {} scheme(s)", securitySchemes.size());
        return securitySchemes;
    }
    
    /**
     * Maps a RAML security scheme to an OpenAPI security scheme.
     */
    private io.swagger.v3.oas.models.security.SecurityScheme mapSecurityScheme(
            org.raml.v2.api.model.v10.security.SecurityScheme ramlScheme, 
            MapperContext context) throws ConverterException {
        
        String type = ramlScheme.type();
        logger.debug("  Security type: {}", type);
        
        io.swagger.v3.oas.models.security.SecurityScheme openApiScheme = 
            new io.swagger.v3.oas.models.security.SecurityScheme();
        
        // Set description
        if (ramlScheme.description() != null && ramlScheme.description().value() != null) {
            openApiScheme.setDescription(ramlScheme.description().value());
        }
        
        // Map based on type
        switch (type.toLowerCase()) {
            case "oauth 2.0":
                mapOAuth2Scheme(ramlScheme, openApiScheme);
                break;
            case "oauth 1.0":
                mapOAuth1Scheme(ramlScheme, openApiScheme);
                break;
            case "basic authentication":
                mapBasicAuthScheme(ramlScheme, openApiScheme);
                break;
            case "digest authentication":
                mapDigestAuthScheme(ramlScheme, openApiScheme);
                break;
            case "pass through":
                mapPassThroughScheme(ramlScheme, openApiScheme);
                break;
            case "x-custom":
            case "x-other":
                mapCustomScheme(ramlScheme, openApiScheme);
                break;
            default:
                logger.warn("Unknown security scheme type: {}, treating as custom", type);
                mapCustomScheme(ramlScheme, openApiScheme);
        }
        
        return openApiScheme;
    }
    
    /**
     * Maps OAuth 2.0 security scheme.
     */
    private void mapOAuth2Scheme(org.raml.v2.api.model.v10.security.SecurityScheme ramlScheme, 
                                io.swagger.v3.oas.models.security.SecurityScheme openApiScheme) {
        
        openApiScheme.setType(io.swagger.v3.oas.models.security.SecurityScheme.Type.OAUTH2);
        
        OAuthFlows flows = new OAuthFlows();
        
        String authorizationUri = null;
        String tokenUri = null;
        List<String> scopesList = new ArrayList<>();
        List<String> authorizationGrants = new ArrayList<>();
        
        // Try to extract settings from the described by annotations
        if (ramlScheme.describedBy() != null && 
            ramlScheme.describedBy().annotations() != null) {
            
            for (org.raml.v2.api.model.v10.declarations.AnnotationRef annotation : 
                ramlScheme.describedBy().annotations()) {
                logger.debug("    Found annotation: {}", annotation.annotation().name());
            }
        }
        
        // Access settings through structured value if available
        if (ramlScheme.settings() != null) {
            // Get the settings as a node tree
            try {
                // Use reflection or direct access to get values
                java.lang.reflect.Method[] methods = ramlScheme.settings().getClass().getMethods();
                
                for (java.lang.reflect.Method method : methods) {
                    String methodName = method.getName();
                    
                    // Look for getter methods
                    if (methodName.equals("authorizationUri")) {
                        try {
                            Object result = method.invoke(ramlScheme.settings());
                            if (result != null) {
                                if (result instanceof AnnotableStringType) {
                                    authorizationUri =
                                            ((AnnotableStringType) result).value();
                                } else {
                                    authorizationUri = result.toString();
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Could not get authorizationUri: {}", e.getMessage());
                        }
                    } else if (methodName.equals("accessTokenUri")) {
                        try {
                            Object result = method.invoke(ramlScheme.settings());
                            if (result != null) {
                                if (result instanceof AnnotableStringType) {
                                    tokenUri =
                                            ((AnnotableStringType) result).value();
                                } else {
                                    tokenUri = result.toString();
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Could not get accessTokenUri: {}", e.getMessage());
                        }
                    } else if (methodName.equals("scopes")) {
                        try {
                            Object result = method.invoke(ramlScheme.settings());
                            if (result != null && result instanceof List) {
                                List<?> scopeList = (List<?>) result;
                                for (Object scope : scopeList) {
                                    if (scope != null) {
                                        scopesList.add(scope.toString());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Could not get scopes: {}", e.getMessage());
                        }
                    } else if (methodName.equals("authorizationGrants")) {
                        try {
                            Object result = method.invoke(ramlScheme.settings());
                            if (result != null && result instanceof List) {
                                List<?> grantList = (List<?>) result;
                                for (Object grant : grantList) {
                                    if (grant != null) {
                                        authorizationGrants.add(grant.toString());
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.debug("Could not get authorizationGrants: {}", e.getMessage());
                        }
                    }
                }
                
            } catch (Exception e) {
                logger.warn("Error extracting OAuth settings: {}", e.getMessage());
            }
        }
        
        logger.debug("    Authorization URI: {}", authorizationUri);
        logger.debug("    Token URI: {}", tokenUri);
        logger.debug("    Scopes: {}", scopesList);
        logger.debug("    Grants: {}", authorizationGrants);
        
        // Determine flow type based on grant types
        if (authorizationGrants != null && !authorizationGrants.isEmpty()) {
            for (String grant : authorizationGrants) {
                switch (grant.toLowerCase()) {
                    case "authorization_code":
                        flows.setAuthorizationCode(createOAuthFlow(
                            authorizationUri, tokenUri, scopesList));
                        logger.debug("    Flow: authorization_code");
                        break;
                    case "implicit":
                        flows.setImplicit(createOAuthFlow(
                            authorizationUri, null, scopesList));
                        logger.debug("    Flow: implicit");
                        break;
                    case "password":
                        flows.setPassword(createOAuthFlow(
                            null, tokenUri, scopesList));
                        logger.debug("    Flow: password");
                        break;
                    case "client_credentials":
                        flows.setClientCredentials(createOAuthFlow(
                            null, tokenUri, scopesList));
                        logger.debug("    Flow: client_credentials");
                        break;
                    default:
                        logger.warn("Unknown OAuth grant type: {}", grant);
                }
            }
        } else {
            // Default to authorization code
            flows.setAuthorizationCode(createOAuthFlow(
                authorizationUri, tokenUri, scopesList));
        }
        
        openApiScheme.setFlows(flows);
    }

    /**
     * Creates an OAuth flow configuration.
     */
    private OAuthFlow createOAuthFlow(String authorizationUrl, String tokenUrl, 
                                    List<String> ramlScopes) {
        
        OAuthFlow flow = new OAuthFlow();
        
        if (authorizationUrl != null && !authorizationUrl.isEmpty()) {
            flow.setAuthorizationUrl(authorizationUrl);
        }
        
        if (tokenUrl != null && !tokenUrl.isEmpty()) {
            flow.setTokenUrl(tokenUrl);
        }
        
        // Map scopes
        Scopes scopes = new Scopes();
        if (ramlScopes != null && !ramlScopes.isEmpty()) {
            for (String scope : ramlScopes) {
                // Auto-generate description from scope name
                String description = scope.replace(":", " - ").replace("_", " ");
                scopes.addString(scope, description);
            }
        }
        flow.setScopes(scopes);
        
        return flow;
    }
    
    /**
     * Maps OAuth 1.0 security scheme.
     * Note: OpenAPI 3.0 doesn't directly support OAuth 1.0, so we use API Key as fallback.
     */
    private void mapOAuth1Scheme(org.raml.v2.api.model.v10.security.SecurityScheme ramlScheme, 
                                io.swagger.v3.oas.models.security.SecurityScheme openApiScheme) {
        
        logger.warn("OAuth 1.0 is not directly supported in OpenAPI 3.0, mapping as API Key");
        
        openApiScheme.setType(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY);
        openApiScheme.setIn(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER);
        openApiScheme.setName("Authorization");
        
        // Add extension to indicate original type
        openApiScheme.addExtension("x-raml-type", "OAuth 1.0");
    }
    
    /**
     * Maps Basic Authentication security scheme.
     */
    private void mapBasicAuthScheme(org.raml.v2.api.model.v10.security.SecurityScheme ramlScheme, 
                                   io.swagger.v3.oas.models.security.SecurityScheme openApiScheme) {
        
        openApiScheme.setType(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP);
        openApiScheme.setScheme("basic");
        
        logger.debug("    Scheme: basic");
    }
    
    /**
     * Maps Digest Authentication security scheme.
     */
    private void mapDigestAuthScheme(org.raml.v2.api.model.v10.security.SecurityScheme ramlScheme, 
                                    io.swagger.v3.oas.models.security.SecurityScheme openApiScheme) {
        
        openApiScheme.setType(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP);
        openApiScheme.setScheme("digest");
        
        logger.debug("    Scheme: digest");
    }
    
    /**
     * Maps Pass Through (API Key) security scheme.
     */
    private void mapPassThroughScheme(org.raml.v2.api.model.v10.security.SecurityScheme ramlScheme, 
                                     io.swagger.v3.oas.models.security.SecurityScheme openApiScheme) {
        
        openApiScheme.setType(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY);
        
        // Determine location (header or query parameter)
        String parameterName = "api_key"; // default
        String location = "header"; // default
        
        if (ramlScheme.describedBy() != null) {
            // Check headers
            if (ramlScheme.describedBy().headers() != null && 
                !ramlScheme.describedBy().headers().isEmpty()) {
                
                TypeDeclaration header = ramlScheme.describedBy().headers().get(0);
                parameterName = header.name();
                location = "header";
                logger.debug("    API Key in header: {}", parameterName);
            } else if (ramlScheme.describedBy().queryParameters() != null &&
                     !ramlScheme.describedBy().queryParameters().isEmpty()) {
                
                TypeDeclaration queryParam = ramlScheme.describedBy().queryParameters().get(0);
                parameterName = queryParam.name();
                location = "query";
                logger.debug("    API Key in query: {}", parameterName);
            }
        }
        
        openApiScheme.setName(parameterName);
        openApiScheme.setIn(location.equals("header") ? 
            io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER : 
            io.swagger.v3.oas.models.security.SecurityScheme.In.QUERY);
    }
    
    /**
     * Maps custom/unknown security schemes.
     */
    private void mapCustomScheme(org.raml.v2.api.model.v10.security.SecurityScheme ramlScheme, 
                                io.swagger.v3.oas.models.security.SecurityScheme openApiScheme) {
        
        // Default to API Key for custom schemes
        openApiScheme.setType(io.swagger.v3.oas.models.security.SecurityScheme.Type.APIKEY);
        openApiScheme.setIn(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER);
        openApiScheme.setName("Authorization");
        
        // Add RAML type as extension
        openApiScheme.addExtension("x-raml-type", ramlScheme.type());
        
        logger.debug("    Custom scheme mapped as API Key");
    }
    
    /**
     * Maps global security requirements from RAML securedBy.
     */
    public List<io.swagger.v3.oas.models.security.SecurityRequirement> mapSecurityRequirements(
            Api source) {
        
        List<io.swagger.v3.oas.models.security.SecurityRequirement> requirements = 
            new ArrayList<>();
        
        if (source.securedBy() != null && !source.securedBy().isEmpty()) {
            logger.debug("Mapping global security requirements");
            
            for (SecuritySchemeRef schemeRef : source.securedBy()) {
                // Get the security scheme name
                String schemeName = schemeRef.name();
                
                if (schemeName != null) {
                    io.swagger.v3.oas.models.security.SecurityRequirement requirement = 
                        new io.swagger.v3.oas.models.security.SecurityRequirement();
                    
                    // Add with empty scopes list (scopes would need to be parsed from structured value)
                    requirement.addList(schemeName, new ArrayList<>());
                    requirements.add(requirement);
                    
                    logger.debug("  Security requirement: {}", schemeName);
                }
            }
        }
        
        return requirements.isEmpty() ? null : requirements;
    }
}
