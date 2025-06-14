package com.mycompany.poggioadventure.core.utils;

import com.mycompany.poggioadventure.persistence.RankingEntryDTO;
import com.mycompany.poggioadventure.persistence.ResourceLoader;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client REST basato su Jersey per interazione con l'API PoggioServer.
 * 
 * <p>Implementa operazioni CRUD per gestione utenti, registrazione vittorie
 * con upload di log file e recupero classifiche. Utilizza autenticazione
 * via API key e supporta upload multipart per file.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Gestione completa ciclo vita utenti (CRUD)</li>
 *   <li>Registrazione vittorie con file di log associati</li>
 *   <li>Download log da server con gestione stream</li>
 *   <li>Recupero classifiche ordinate</li>
 *   <li>Gestione errori standardizzata via enum ApiClientResult</li>
 * </ul>
 * 
 * <p><b>Pattern implementati:</b>
 * <ul>
 *   <li>Resource Management: auto-closing di Response e stream</li>
 *   <li>Error Handling: mappatura centralizzata status code HTTP</li>
 *   <li>Thread Safety: client Jersey riutilizzabile</li>
 * </ul>
 */
public class PoggioClientJersey {

    /** Segreto condiviso per autenticazione API */
    private static final String SHARED_SECRET = "Z10_F4_WO4H";
    
    /** Header HTTP per trasmissione API key */
    private static final String HEADER_API_KEY = "X-API-Key";
    
    /** URI base del server API REST */
    private static final String SERVER_URI = "http://localhost:8080/";

    /** Client Jersey thread-safe per riutilizzo */
    private final Client client;
    
    /** URI base memorizzato per costruzione endpoint */
    private final String serverBaseUri;

    /**
     * Inizializza client Jersey con supporto multipart per upload file.
     */
    public PoggioClientJersey() {
        this.serverBaseUri = SERVER_URI;
        this.client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .build();
    }

    /**
     * Rilascia risorse del client Jersey (connection pool, thread).
     * Importante chiamare quando il client non è più necessario.
     */
    public void close() {
        if (this.client != null) {
            try {
                this.client.close();
            } catch (Exception e) {
                // Ignora errori durante chiusura
            }
        }
    }

    /**
     * Aggiunge nuovo utente al server tramite POST.
     * 
     * @param username Nome utente da aggiungere
     * @return SUCCESS_CREATED (201), USER_ALREADY_EXISTS (409), o codici errore
     */
    public ApiClientResult addUser(String username) {
        if (username == null || username.trim().isEmpty()) {
             return ApiClientResult.INVALID_INPUT_CLIENT;
        }
        
        WebTarget target = client.target(serverBaseUri).path("players").path(username);
        Response response = null;
        try {
            response = target.request(MediaType.APPLICATION_JSON)
                           .header(HEADER_API_KEY, SHARED_SECRET)
                           .post(Entity.json(null));

            return handleResponseStatus(response, "addUser", username, 201, 409);
        } catch (ProcessingException | WebApplicationException e) {
             return ApiClientResult.CONNECTION_ERROR;
        } catch (Exception e) {
             return ApiClientResult.UNKNOWN_ERROR;
        } finally {
             if (response != null) response.close();
        }
    }

    /**
     * Verifica esistenza utente tramite GET.
     * 
     * @param username Nome utente da verificare
     * @return SUCCESS_OK (200), USER_NOT_FOUND (404), o codici errore
     */
    public ApiClientResult checkUserExists(String username) {
        if (username == null || username.trim().isEmpty()) {
             return ApiClientResult.INVALID_INPUT_CLIENT;
        }
        
        WebTarget target = client.target(serverBaseUri).path("players").path(username);
        Response response = null;
        try {
            response = target.request(MediaType.APPLICATION_JSON)
                           .header(HEADER_API_KEY, SHARED_SECRET)
                           .get();

            return handleResponseStatus(response, "checkUserExists", username, 200, 404);
        } catch (ProcessingException | WebApplicationException e) {
             return ApiClientResult.CONNECTION_ERROR;
        } catch (Exception e) {
             return ApiClientResult.UNKNOWN_ERROR;
        } finally {
             if (response != null) response.close();
        }
    }

    /**
     * Registra vittoria con upload di file di log tramite multipart PUT.
     * 
     * @param username Nome utente
     * @param data Data vittoria (formato YYYY-MM-DD)
     * @param ora Ora vittoria (formato HH:mm:ss) 
     * @param durataMs Durata partita in millisecondi
     * @param logFilePath Percorso locale file di log da caricare
     * @return SUCCESS_OK (200), USER_NOT_FOUND (404), LOG_ALREADY_EXISTS (409), o codici errore
     */
     public ApiClientResult recordVictoryWithLog(String username, String data, String ora, long durataMs, String logFilePath) {
         if (username == null || username.trim().isEmpty() || data == null || ora == null || logFilePath == null || durataMs < 0) {
              return ApiClientResult.INVALID_INPUT_CLIENT;
         }
         
         WebTarget target = client.target(serverBaseUri).path("players").path(username).path("victory");

         File logFile = new File(logFilePath);
         if (!logFile.exists() || !logFile.isFile()) {
              return ApiClientResult.FILE_ERROR;
         }

         try (FormDataMultiPart multipart = new FormDataMultiPart()) {
              // Campi dati della vittoria
              multipart.field("data", data);
              multipart.field("ora", ora);
              multipart.field("durataMs", String.valueOf(durataMs));

              // File di log come attachment
              FileDataBodyPart filePart = new FileDataBodyPart("logFile", logFile, MediaType.APPLICATION_OCTET_STREAM_TYPE);
              multipart.bodyPart(filePart);

              Response response = null;
              try {
                   response = target.request(MediaType.APPLICATION_JSON)
                                  .header(HEADER_API_KEY, SHARED_SECRET)
                                  .put(Entity.entity(multipart, multipart.getMediaType()));

                   return handleResponseStatus(response, "recordVictory", username, 200, 404, 409);
              } finally {
                   if (response != null) response.close();
              }
         } catch (ProcessingException | WebApplicationException e) {
              return ApiClientResult.CONNECTION_ERROR;
         } catch (IOException e) {
             return ApiClientResult.FILE_ERROR;
         } catch (Exception e) {
             return ApiClientResult.UNKNOWN_ERROR;
         }
     }

    /**
     * Helper centralizzato per mappatura status code HTTP a ApiClientResult.
     * Gestisce codici di successo, fallimenti attesi e errori comuni con
     * analisi del corpo della risposta per distinguere tipi di conflitto.
     * 
     * @param response Risposta HTTP ricevuta
     * @param operation Nome operazione per logging
     * @param username Username coinvolto per logging
     * @param successStatus Codice HTTP di successo atteso
     * @param failureStatus Array di codici di fallimento attesi
     * @return ApiClientResult mappato appropriatamente
     */
    private ApiClientResult handleResponseStatus(Response response, String operation, String username, int successStatus, int... failureStatus) {
        if (response == null) {
            System.err.println("Errore Interno Client [" + operation + " " + username + "]: Risposta nulla ricevuta da handleResponseStatus.");
            return ApiClientResult.UNKNOWN_ERROR;
        }

        int statusCode = response.getStatus();

        // Gestione successo
        if (statusCode == successStatus) {
             return (successStatus == 201) ? ApiClientResult.SUCCESS_CREATED : ApiClientResult.SUCCESS_OK;
        }

        // Gestione fallimenti specifici attesi
        for (int failCode : failureStatus) {
             if (statusCode == failCode) {
                 String errorBody = safeReadEntity(response);

                 switch (statusCode) {
                     case 404 -> {
                         return ApiClientResult.USER_NOT_FOUND;
                     }
                     case 409 -> {
                         // Distingue tipo di conflitto basandosi sul contenuto
                         if (errorBody != null && errorBody.contains("USER_ALREADY_EXISTS")) {
                             return ApiClientResult.USER_ALREADY_EXISTS;
                         } else if (errorBody != null && errorBody.contains("LOG_ALREADY_EXISTS")) {
                             return ApiClientResult.LOG_ALREADY_EXISTS;
                         } else {
                             return ApiClientResult.UNKNOWN_ERROR;
                         }
                     }
                     default -> {
                         return ApiClientResult.UNKNOWN_ERROR;
                     }
                 }
             }
        }

        // Gestione errori comuni generici
        String genericErrorBody = safeReadEntity(response);
        System.err.println("Errore Generico [" + operation + " " + username + "]: Status=" + statusCode + ", Body=" + genericErrorBody);
        switch (statusCode) {
            case 400 -> {
                return ApiClientResult.INVALID_INPUT_SERVER;
            }
            case 401 -> {
                return ApiClientResult.UNAUTHORIZED;
            }
            default -> {
                if (statusCode >= 500) {
                    return ApiClientResult.SERVER_ERROR;
                } else {
                    return ApiClientResult.UNKNOWN_ERROR;
                }
            }
        }
    }

    /**
     * Elimina utente dal server tramite DELETE.
     * 
     * @param username Utente da eliminare
     * @return SUCCESS_OK (200), USER_NOT_FOUND (404), o codici errore
     */
    public ApiClientResult deletePlayer(String username) {
        if (username == null || username.trim().isEmpty()) {
             return ApiClientResult.INVALID_INPUT_CLIENT;
        }
        
        WebTarget target = client.target(serverBaseUri).path("players").path(username);
        Response response = null;
        try {
            response = target.request(MediaType.APPLICATION_JSON)
                           .header(HEADER_API_KEY, SHARED_SECRET)
                           .delete();

            return handleResponseStatus(response, "deletePlayer", username, 200, 404);
        } catch (ProcessingException | WebApplicationException e) {
             return ApiClientResult.CONNECTION_ERROR;
        } catch (Exception e) {
             return ApiClientResult.UNKNOWN_ERROR;
        } finally {
             if (response != null) response.close();
        }
    }

    /**
     * Recupera classifica giocatori dal server.
     * In caso di errore restituisce lista vuota invece di eccezioni.
     * 
     * @return Lista RankingEntryDTO ordinata o lista vuota per errori
     */
     public List<RankingEntryDTO> getRanking() {
         WebTarget target = client.target(serverBaseUri).path("players").path("ranking");
         Response response = null;

         try {
              response = target.request(MediaType.APPLICATION_JSON)
                             .header(HEADER_API_KEY, SHARED_SECRET)
                             .get();

              int statusCode = response.getStatus();

              if (statusCode == Response.Status.OK.getStatusCode()) {
                   try {
                        List<RankingEntryDTO> ranking = response.readEntity(new GenericType<List<RankingEntryDTO>>() {});
                        return ranking != null ? ranking : Collections.emptyList();
                   } catch (Exception e) {
                        return Collections.emptyList();
                   }
              } else {
                   String errorBody = safeReadEntity(response);
                   return Collections.emptyList();
              }
         } catch (ProcessingException | WebApplicationException e) {
              return Collections.emptyList();
         } catch (Exception e) {
              return Collections.emptyList();
         } finally {
              if (response != null) {
                   response.close();
              }
         }
     }

    /**
     * Legge corpo della risposta HTTP in modo sicuro per logging/debug.
     * Gestisce eccezioni e limita lunghezza output per evitare log eccessivi.
     * 
     * @param response Risposta da cui leggere il corpo
     * @return Corpo come stringa troncata o placeholder per errori
     */
    private String safeReadEntity(Response response) {
        try {
            if (response != null && response.hasEntity()) {
                String body = response.readEntity(String.class);
                final int MAX_LEN = 500;
                return body.length() > MAX_LEN ? body.substring(0, MAX_LEN) + "..." : body;
            }
        } catch (Exception e) {
             // Ignora eccezioni durante lettura per robustezza
        }
        return "(Nessun corpo o errore lettura)";
    }

     /** Directory per file di log temporanei in fase di test */
     private static final String LOG_TEST_DIRECTORY = "test_logs_client";

     /**
      * Scarica file di log associato a vittoria utente e salva localmente.
      * 
      * @param username Utente di cui scaricare il log
      * @return SUCCESS_OK (200), USER_NOT_FOUND (404), FILE_ERROR per errori I/O, o altri codici
      */
     public ApiClientResult downloadLogFile(String username) {
         String localSavePath = ResourceLoader.LOGS_DW_DIRECTORY.resolve(username + "_log.txt").toString();

         if (username == null || username.trim().isEmpty() || localSavePath == null || localSavePath.trim().isEmpty()) {
              return ApiClientResult.INVALID_INPUT_CLIENT;
         }

         WebTarget target = client.target(serverBaseUri).path("players").path(username).path("log");
         Response response = null;

         try {
              response = target.request(MediaType.APPLICATION_OCTET_STREAM)
                             .header(HEADER_API_KEY, SHARED_SECRET)
                             .get();

              int statusCode = response.getStatus();

              if (statusCode == Response.Status.OK.getStatusCode()) {
                   try (InputStream inputStream = response.readEntity(InputStream.class)) {
                        Path targetPath = Paths.get(localSavePath);
                        Path parentDir = targetPath.getParent();
                        if (parentDir != null) {
                             Files.createDirectories(parentDir);
                        }
                        Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        return ApiClientResult.SUCCESS_OK;
                   } catch (IOException e) {
                        return ApiClientResult.FILE_ERROR;
                   } catch (Exception e) {
                        return ApiClientResult.CONNECTION_ERROR;
                   }

              } else {
                   return handleResponseStatus(response, "downloadLogFile", username, 200, 404);
              }

         } catch (ProcessingException | WebApplicationException e) {
              return ApiClientResult.CONNECTION_ERROR;
         } catch (Exception e) {
              return ApiClientResult.UNKNOWN_ERROR;
         } finally {
              if (response != null) response.close();
         }
     }

     /**
      * Metodo di test per validazione funzionalità client.
      * Crea utenti fittizi, registra vittorie con log e verifica classifica.
      * 
      * WARNING: Modifica dati reali sul server configurato.
      */
     public static void main(String[] args) {
         System.out.println("--- Avvio Test Client PoggioAdventure (Jersey) ---");
         System.out.println("!!! ATTENZIONE: Modificherà dati su " + SERVER_URI + " !!!");

         PoggioClientJersey testClient = new PoggioClientJersey();

         // Configurazione dati di test con score variabili
         List<String> testUsers = Arrays.asList(
             "TesterAlfa", "TesterBeta", "TesterGamma", "TesterDelta", "TesterEpsilon", "TesterZeta"
         );
         
         Map<String, Map<String, Integer>> testData = new HashMap<>();
         testData.put("TesterAlfa", Map.of("durataMs", 50000, "righeLog", 50));
         testData.put("TesterBeta", Map.of("durataMs", 120000, "righeLog", 200));
         testData.put("TesterGamma", Map.of("durataMs", 300000, "righeLog", 100));
         testData.put("TesterDelta", Map.of("durataMs", 90000, "righeLog", 1000));
         testData.put("TesterEpsilon", Map.of("durataMs", 10000, "righeLog", 10));
         testData.put("TesterZeta", Map.of("durataMs", 60000, "righeLog", 150));

         Map<String, String> logFilePaths = new HashMap<>();
         boolean setupSuccess = true;

         try {
             // FASE 1: Creazione file di log fittizi
             System.out.println("\n[FASE 1] Creazione file di log fittizi in ./" + LOG_TEST_DIRECTORY);
             Path logDir = Paths.get(LOG_TEST_DIRECTORY);
             Files.createDirectories(logDir);

             for (String user : testUsers) {
                 int lineCount = testData.get(user).get("righeLog");
                 String fileName = "log_" + user + ".txt";
                 Path filePath = logDir.resolve(fileName);
                 try {
                     createDummyLogFile(filePath, lineCount);
                     logFilePaths.put(user, filePath.toString());
                     System.out.println("  - Creato: " + filePath + " (" + lineCount + " righe)");
                 } catch (IOException e) {
                     System.err.println("ERRORE FASE 1: Impossibile creare file log per " + user + ": " + e.getMessage());
                     setupSuccess = false;
                     break;
                 }
             }

             if (!setupSuccess) throw new RuntimeException("Setup fallito: impossibile creare tutti i file di log.");

             // FASE 2: Verifica/creazione utenti sul server
             System.out.println("\n[FASE 2] Verifica/Creazione utenti sul server...");
             for (String user : testUsers) {
                 System.out.print("  - Utente '" + user + "': ");
                 ApiClientResult checkResult = testClient.checkUserExists(user);

                 if (null == checkResult) {
                    System.out.println("ERRORE verifica (risultato nullo)");
                    setupSuccess = false;
                 } else switch (checkResult) {
                     case USER_NOT_FOUND -> {
                         System.out.print("non trovato, aggiungo... ");
                         ApiClientResult addResult = testClient.addUser(user);
                         if (addResult == ApiClientResult.SUCCESS_CREATED) {
                             System.out.println("OK (Aggiunto)");
                         } else {
                             System.out.println("ERRORE aggiunta (" + addResult + ")");
                             setupSuccess = false;
                         }
                     }
                     case SUCCESS_OK -> System.out.println("OK (Già esistente)");
                     default -> {
                         System.out.println("ERRORE verifica (" + checkResult + ")");
                         setupSuccess = false;
                     }
                 }
                 if (!setupSuccess) break;
             }

             if (!setupSuccess) throw new RuntimeException("Setup fallito: impossibile preparare gli utenti sul server.");

             // FASE 3: Registrazione vittorie con log
             System.out.println("\n[FASE 3] Registrazione vittorie...");
             LocalDate today = LocalDate.now();
             LocalTime now = LocalTime.now();

             for (String user : testUsers) {
                 System.out.print("  - Registrazione vittoria per '" + user + "': ");
                 String dataStr = today.format(DateTimeFormatter.ISO_DATE);
                 String oraStr = now.minusMinutes(testUsers.indexOf(user) * 5L).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                 long durata = testData.get(user).get("durataMs");
                 String logPath = logFilePaths.get(user);

                 ApiClientResult victoryResult = testClient.recordVictoryWithLog(user, dataStr, oraStr, durata, logPath);
                 System.out.println(victoryResult);

                 if (victoryResult != ApiClientResult.SUCCESS_OK) {
                     System.err.println("    -> ATTENZIONE: Registrazione fallita o non ottimale per " + user + " (" + victoryResult + ")");
                 }
                 
                 try { Thread.sleep(50); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
             }

             // FASE 4: Verifica classifica finale
             System.out.println("\n[FASE 4] Recupero classifica finale...");
             List<RankingEntryDTO> ranking = testClient.getRanking();

             if (ranking == null) {
                  System.err.println("ERRORE FASE 4: Impossibile recuperare la classifica (risultato nullo).");
             } else if (ranking.isEmpty()) {
                  System.out.println("  -> Classifica vuota o errore durante il recupero.");
             } else {
                  System.out.println("  -> Classifica attuale:");
                  System.out.printf("     %-4s %-20s %-10s %-12s %-10s%n", "Pos", "Username", "Punteggio", "Data", "Ora");
                  System.out.println("     -------------------------------------------------------------");
                  int pos = 1;
                  for (RankingEntryDTO entry : ranking) {
                        String date = entry.getData() != null ? entry.getData().toString() : "N/D";
                        String time = entry.getOra() != null ? entry.getOra().toString() : "N/D";
                        System.out.printf("     %-4d %-20s %-10d %-12s %-10s%n",
                                          pos++,
                                          entry.getUsername(),
                                          entry.getPunteggio() != null ? entry.getPunteggio() : -1,
                                          date,
                                          time);
                  }
                  System.out.println("     -------------------------------------------------------------");
             }

         } catch (IOException | RuntimeException e) {
             System.err.println("\nERRORE CRITICO DURANTE IL TEST: " + e.getMessage());
             e.printStackTrace(System.err);
         } finally {
             // FASE 5: Pulizia risorse
             System.out.println("\n[FASE 5] Pulizia risorse...");

             System.out.println("  - Eliminazione file di log fittizi:");
             for (String path : logFilePaths.values()) {
                 try {
                     Files.deleteIfExists(Paths.get(path));
                 } catch (IOException e) {
                     System.err.println("    ERRORE: Impossibile eliminare file " + path + ": " + e.getMessage());
                 }
             }
             
             try {
                 Files.deleteIfExists(Paths.get(LOG_TEST_DIRECTORY));
                 System.out.println("  - Eliminata directory: " + LOG_TEST_DIRECTORY);
             } catch (IOException e) {
                 System.err.println("  - ATTENZIONE: Impossibile eliminare directory " + LOG_TEST_DIRECTORY + " (potrebbe non essere vuota o mancano permessi): " + e.getMessage());
             }

             if (testClient != null) {
                 testClient.close();
                 System.out.println("  - Client Jersey chiuso.");
             }
             System.out.println("\n--- Test Terminato ---");
         }
     }

     /**
      * Helper per creazione file di testo fittizio con numero specificato di righe.
      * Utilizzato per generazione log di test.
      * 
      * @param filePath Path del file da creare
      * @param lineCount Numero di righe da scrivere
      * @throws IOException Se si verificano errori durante la scrittura
      */
     private static void createDummyLogFile(Path filePath, int lineCount) throws IOException {
         List<String> lines = new ArrayList<>(lineCount);
         for (int i = 1; i <= lineCount; i++) {
             lines.add("Riga di log fittizia numero " + i + " per " + filePath.getFileName());
         }
         Files.write(filePath, lines, StandardCharsets.UTF_8,
                     StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
     }
}