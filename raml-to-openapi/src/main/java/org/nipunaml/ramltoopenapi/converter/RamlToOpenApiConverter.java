package org.nipunaml.ramltoopenapi.converter;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.nipunaml.ramltoopenapi.exception.ConverterException;
import org.nipunaml.ramltoopenapi.mapper.InfoMapper;
import org.nipunaml.ramltoopenapi.mapper.MapperContext;
import org.nipunaml.ramltoopenapi.mapper.PathMapper;
import org.nipunaml.ramltoopenapi.mapper.SchemaMapper;
import org.nipunaml.ramltoopenapi.mapper.SecurityMapper;
import org.nipunaml.ramltoopenapi.mapper.ServerMapper;
import org.nipunaml.ramltoopenapi.model.openapi.OpenApiDocument;
import org.nipunaml.ramltoopenapi.model.raml.RamlDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Main converter class that orchestrates the conversion from RAML to OpenAPI.
 * Coordinates various mappers to transform RAML components into OpenAPI components.
 */
public class RamlToOpenApiConverter {
    
    private static final Logger logger = LoggerFactory.getLogger(RamlToOpenApiConverter.class);
    
    private static final String OPENAPI_VERSION = "3.0.0";
    
    private final InfoMapper infoMapper;
    private final ServerMapper serverMapper;
    private final SchemaMapper schemaMapper;
    private final PathMapper pathMapper;
    private final SecurityMapper securityMapper;
    
    private final boolean strict;
    
    public RamlToOpenApiConverter(boolean strict) {
        this.strict = strict;
        this.infoMapper = new InfoMapper();
        this.serverMapper = new ServerMapper();
        this.schemaMapper = new SchemaMapper();
        this.pathMapper = new PathMapper();
        this.securityMapper = new SecurityMapper();
    }
    
    /**
     * Converts a RAML document to an OpenAPI document.
     *
     * @param ramlDocument the RAML document to convert
     * @return the converted OpenAPI document
     * @throws ConverterException if conversion fails
     */
    public OpenApiDocument convert(RamlDocument ramlDocument) throws ConverterException {
        if (ramlDocument == null) {
            throw new ConverterException("Cannot convert null RAML document");
        }
        
        logger.info("Converting RAML to OpenAPI: {}", ramlDocument.getFileName());
        logger.debug("  RAML Title: {}", ramlDocument.getTitle());
        logger.debug("  RAML Version: {}", ramlDocument.getVersion());
        
        // Create mapper context
        MapperContext context = new MapperContext(ramlDocument, strict);
        
        // Create OpenAPI object
        OpenAPI openApi = new OpenAPI();
        openApi.setOpenapi(OPENAPI_VERSION);
        
        try {
            // Step 1: Map Info
            logger.debug("Mapping Info component...");
            Info info = infoMapper.map(ramlDocument.getApi(), context);
            openApi.setInfo(info);
            
            // Step 2: Map Servers
            logger.debug("Mapping Server component...");
            List<Server> servers = serverMapper.map(ramlDocument.getApi(), context);
            openApi.setServers(servers);
            
            // Step 3: Map Components (schemas) - MUST BE BEFORE PATHS
            logger.debug("Mapping Components (schemas)...");
            Components components = new Components();
            
            Map<String, Schema> schemas = schemaMapper.map(ramlDocument.getApi(), context);
            if (schemas != null && !schemas.isEmpty()) {
                components.setSchemas(schemas);
                logger.debug("Mapped {} schema(s)", schemas.size());
            }
            
            // Step 4: Map security schemes
            Map<String, SecurityScheme> securitySchemes = 
                securityMapper.map(ramlDocument.getApi(), context);
            if (securitySchemes != null && !securitySchemes.isEmpty()) {
                components.setSecuritySchemes(securitySchemes);
                logger.debug("Mapped {} security scheme(s)", securitySchemes.size());
            }
            
            openApi.setComponents(components);
            
            // Step 5: Map Paths (NOW schemas are registered in context)
            logger.debug("Mapping Paths...");
            Paths paths = pathMapper.map(ramlDocument.getApi(), context);
            if (paths != null && !paths.isEmpty()) {
                openApi.setPaths(paths);
                logger.debug("Mapped {} path(s)", paths.size());
            }
            
            // Generate target file name
            String targetFileName = generateTargetFileName(ramlDocument.getFileName());
            
            // Create OpenAPI document wrapper
            OpenApiDocument openApiDocument = new OpenApiDocument(
                openApi,
                ramlDocument.getFileName(),
                targetFileName
            );
            
            logger.info("✓ Conversion completed: {} -> {}", 
                ramlDocument.getFileName(), targetFileName);
            
            return openApiDocument;
            
        } catch (Exception e) {
            String message = "Failed to convert RAML document '" + ramlDocument.getFileName() + "'";
            if (e instanceof ConverterException) {
                throw (ConverterException) e;
            }
            throw new ConverterException(message, e);
        }
    }
    
    /**
     * Converts multiple RAML documents to OpenAPI documents.
     *
     * @param ramlDocuments list of RAML documents to convert
     * @return list of converted OpenAPI documents
     * @throws ConverterException if conversion fails
     */
    public List<OpenApiDocument> convertMultiple(List<RamlDocument> ramlDocuments) 
            throws ConverterException {
        
        if (ramlDocuments == null || ramlDocuments.isEmpty()) {
            throw new ConverterException("No RAML documents provided for conversion");
        }
        
        logger.info("Converting {} RAML document(s) to OpenAPI", ramlDocuments.size());
        
        List<OpenApiDocument> openApiDocuments = new java.util.ArrayList<>();
        
        for (RamlDocument ramlDoc : ramlDocuments) {
            try {
                OpenApiDocument openApiDoc = convert(ramlDoc);
                openApiDocuments.add(openApiDoc);
            } catch (ConverterException e) {
                logger.error("✗ Conversion failed for: {}", ramlDoc.getFileName());
                throw new ConverterException(
                    "Failed to convert '" + ramlDoc.getFileName() + "': " + e.getMessage(), 
                    e
                );
            }
        }
        
        logger.info("✓ Successfully converted all {} document(s)", openApiDocuments.size());
        
        return openApiDocuments;
    }
    
    /**
     * Generates target file name from source RAML file name.
     */
    private String generateTargetFileName(String sourceFileName) {
        if (sourceFileName == null || sourceFileName.isEmpty()) {
            return "openapi.yaml";
        }
        
        // Replace .raml with .yaml
        if (sourceFileName.toLowerCase().endsWith(".raml")) {
            return sourceFileName.substring(0, sourceFileName.length() - 5) + ".yaml";
        }
        
        return sourceFileName + ".yaml";
    }
    
    /**
     * Gets conversion statistics.
     */
    public String getConversionStats(OpenApiDocument document) {
        StringBuilder stats = new StringBuilder();
        stats.append("Conversion Statistics:\n");
        stats.append("  OpenAPI Version: ").append(document.getOpenApiVersion()).append("\n");
        stats.append("  Title: ").append(document.getTitle()).append("\n");
        stats.append("  Version: ").append(document.getVersion()).append("\n");
        
        OpenAPI openApi = document.getOpenApi();
        
        if (openApi.getServers() != null) {
            stats.append("  Servers: ").append(openApi.getServers().size()).append("\n");
        }
        
        if (openApi.getComponents() != null) {
            if (openApi.getComponents().getSchemas() != null) {
                stats.append("  Schemas: ").append(
                    openApi.getComponents().getSchemas().size()).append("\n");
            }
            if (openApi.getComponents().getSecuritySchemes() != null) {
                stats.append("  Security Schemes: ").append(
                    openApi.getComponents().getSecuritySchemes().size()).append("\n");
            }
        }
        
        if (openApi.getPaths() != null) {
            stats.append("  Paths: ").append(openApi.getPaths().size()).append("\n");
        }
        
        if (openApi.getSecurity() != null) {
            stats.append("  Global Security: ").append(
                openApi.getSecurity().size()).append(" requirement(s)\n");
        }
        
        return stats.toString();
    }
}
