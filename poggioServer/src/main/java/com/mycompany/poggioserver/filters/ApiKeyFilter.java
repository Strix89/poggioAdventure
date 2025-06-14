package com.mycompany.poggioserver.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

/**
 * Filtro per l'autenticazione delle richieste tramite API Key.
 * Valida l'header X-API-Key confrontandolo con il segreto configurato.
 * 
 * @author Strix89
 */
@Provider
public class ApiKeyFilter implements ContainerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyFilter.class);
    
    private static final String SEGRETO_SERVER = "Z10_F4_WO4H";
    private static final String HEADER_API_KEY = "X-API-Key";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String apiKeyClient = requestContext.getHeaderString(HEADER_API_KEY);
        
        logger.debug("Verifico API Key per: {}", requestContext.getUriInfo().getPath());

        if (apiKeyClient == null || !apiKeyClient.equals(SEGRETO_SERVER)) {
            logger.warn("Accesso negato per {}: chiave API non valida", 
                       requestContext.getUriInfo().getPath());

            Response unauthorizedResponse = Response
                .status(Response.Status.UNAUTHORIZED)
                .entity("{\"error\":\"Accesso non autorizzato. Chiave API mancante o non valida.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();

            requestContext.abortWith(unauthorizedResponse);
        } else {
            logger.debug("API Key valida per: {}", requestContext.getUriInfo().getPath());
        }
    }
}