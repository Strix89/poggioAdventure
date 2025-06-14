package com.mycompany.poggioserver.filters;

// Import JAX-RS (Jakarta RESTful Web Services) API per filtri e contesto richiesta
import jakarta.ws.rs.container.ContainerRequestContext; // Fornisce informazioni sulla richiesta HTTP in arrivo e permette di modificarla/interromperla
import jakarta.ws.rs.container.ContainerRequestFilter; // Interfaccia che definisce un filtro da eseguire sulle richieste in entrata
import jakarta.ws.rs.core.MediaType; // Definisce i tipi di media (es. application/json) per le risposte
import jakarta.ws.rs.core.Response; // Permette di costruire risposte HTTP personalizzate
import jakarta.ws.rs.ext.Provider; // Annotazione JAX-RS che marca questa classe per il rilevamento automatico da parte del runtime (Jersey)

// Import Logging (SLF4J)
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Import standard Java
import java.io.IOException; // Eccezione base per errori I/O (richiesta dalla firma del metodo filter)

/**
 * Un filtro JAX-RS ({@link ContainerRequestFilter}) che intercetta le richieste HTTP in entrata
 * per implementare un meccanismo di autenticazione basato su una chiave API statica.
 * <p>
 * Il filtro controlla la presenza e la validità di un header HTTP specifico ({@code X-API-Key})
 * confrontando il suo valore con un segreto predefinito nel server.
 * Se la chiave API fornita dal client non corrisponde a quella attesa, la richiesta
 * viene bloccata immediatamente con una risposta HTTP {@code 401 Unauthorized}.
 * Se la chiave è valida, il filtro permette alla richiesta di proseguire verso la
 * risorsa JAX-RS di destinazione (es. metodi in {@code PlayerResource}).
 * </p>
 * L'annotazione {@link Provider} assicura che Jersey rilevi e registri automaticamente
 * questo filtro all'avvio dell'applicazione.
 *
 */
@Provider // Rende questa classe un provider JAX-RS, rilevabile e registrabile automaticamente
public class ApiKeyFilter implements ContainerRequestFilter {

    // Logger SLF4J per questa classe
    private static final Logger logger = LoggerFactory.getLogger(ApiKeyFilter.class);

    // Il valore segreto che il client DEVE inviare nell'header per essere autenticato.
    // ATTENZIONE: Hardcodare segreti nel codice è una pratica INSICURA per produzione.
    // Dovrebbe essere caricato da configurazione esterna/sicura.
    private static final String SEGRETO_SERVER = "Z10_F4_WO4H";

    // Il nome dell'header HTTP che il client deve utilizzare per inviare la chiave API.
    private static final String HEADER_API_KEY = "X-API-Key";

    /**
     * Metodo principale del filtro, chiamato da Jersey per ogni richiesta in entrata
     * che corrisponde al percorso dell'applicazione (prima che raggiunga la risorsa).
     *
     * @param requestContext Il contesto della richiesta corrente, permette di accedere
     * agli header, URI, metodo HTTP e di interrompere la richiesta.
     * @throws IOException Dichiarata dall'interfaccia, anche se non usata direttamente qui.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // 1. Estrae il valore dell'header 'X-API-Key' dalla richiesta HTTP in arrivo.
        // requestContext.getHeaderString() restituisce null se l'header non è presente.
        String apiKeyClient = requestContext.getHeaderString(HEADER_API_KEY);

        // Log a livello DEBUG per tracciare il controllo (utile per sviluppo/diagnosi)
        logger.debug("Controllo API Key per richiesta a URI: {}. Header '{}' ricevuto: {}",
                     requestContext.getUriInfo().getPath(), HEADER_API_KEY, apiKeyClient);

        // 2. Verifica se la chiave API ricevuta è valida.
        //    Controlla se l'header è presente (apiKeyClient != null) E se il suo valore
        //    corrisponde esattamente al segreto atteso dal server.
        if (apiKeyClient == null || !apiKeyClient.equals(SEGRETO_SERVER)) {
            // --- Accesso Negato ---
            // Logga un avviso perché questo è un evento di sicurezza significativo.
            logger.warn("Accesso NEGATO per richiesta a URI: {}. Chiave API mancante o non valida. Chiave ricevuta: '{}'",
                        requestContext.getUriInfo().getPath(), apiKeyClient);

            // 3. Blocca (Abort) la richiesta.
            //    Costruisce una risposta HTTP 401 Unauthorized standard.
            Response unauthorizedResponse = Response
                .status(Response.Status.UNAUTHORIZED) // Imposta lo status code a 401
                // Imposta il corpo (entity) della risposta con un messaggio JSON di errore
                .entity("{\"error\":\"Accesso non autorizzato. Chiave API mancante o non valida.\"}")
                // Specifica il tipo di contenuto del corpo della risposta
                .type(MediaType.APPLICATION_JSON)
                .build(); // Costruisce l'oggetto Response

            // Interrompe immediatamente l'elaborazione della richiesta e invia
            // la risposta 'unauthorizedResponse' al client. La richiesta non
            // raggiungerà mai la risorsa JAX-RS (es. PlayerResource).
            requestContext.abortWith(unauthorizedResponse);

        } else {
            // --- Accesso Consentito ---
            // La chiave API è valida. Logga a livello DEBUG.
            // Il metodo filter termina semplicemente senza chiamare abortWith().
            // Questo permette a Jersey di continuare l'elaborazione della richiesta
            // e inviarla alla risorsa JAX-RS appropriata.
            logger.debug("API Key valida. Accesso consentito per richiesta a URI: {}",
                         requestContext.getUriInfo().getPath());
        }
    }
}