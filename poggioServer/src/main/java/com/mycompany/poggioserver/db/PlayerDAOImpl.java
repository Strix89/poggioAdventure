package com.mycompany.poggioserver.db;

// Import del modello e DTO
import com.mycompany.poggioserver.model.PlayerRecord; // Oggetto (Record?) che rappresenta i dati completi di un giocatore
import com.mycompany.poggioserver.resources.RankingEntryDTO; // DTO per i dati specifici della classifica

// Import standard JDBC - Usiamo l'import generico come richiesto
import java.sql.*;

// Import Collections e Logging
import java.util.ArrayList; // Per creare la lista dei risultati della classifica
import java.util.List; // Interfaccia per le liste
import org.slf4j.Logger; // Interfaccia per il logging (SLF4J)
import org.slf4j.LoggerFactory; // Factory per ottenere istanze di Logger

/**
 * Implementazione concreta dell'interfaccia {@link PlayerDAO} (interfaccia non fornita, ma dedotta).
 * Questa classe gestisce tutte le operazioni di accesso ai dati (Data Access Object - DAO)
 * per l'entità 'Player' (giocatore) interagendo con il database relazionale sottostante
 * tramite JDBC. Utilizza il {@link DatabaseManager} per ottenere connessioni dal pool.
 *
 * Responsabilità:
 * - Inserire nuovi giocatori nel database.
 * - Recuperare i dati di un giocatore esistente.
 * - Aggiornare i dati di un giocatore (es. registrare una vittoria).
 * - Eliminare un giocatore dal database.
 * - Recuperare la classifica dei giocatori (ordinata per punteggio).
 *
 * @author Strix89 // Autore originale
 */
public class PlayerDAOImpl implements PlayerDAO { // Assumendo che implementi PlayerDAO

    // Logger SLF4J specifico per questa classe
    private static final Logger logger = LoggerFactory.getLogger(PlayerDAOImpl.class);

    /**
     * Aggiunge un nuovo giocatore al database, inserendo solo il suo username.
     * Gli altri campi (data, ora, punteggio, etc.) verranno popolati successivamente
     * (es. tramite {@code recordVictory}).
     *
     * @param username Lo username univoco del giocatore da aggiungere.
     * @throws IllegalArgumentException Se lo username è nullo o vuoto.
     * @throws SQLException Se si verifica un errore durante l'interazione con il database,
     * inclusa una specifica eccezione se lo username esiste già (duplicate key).
     */
    @Override
    public void addPlayer(String username) throws SQLException {
        // Validazione preliminare dell'input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto.");
        }

        // Query SQL per l'inserimento del nuovo giocatore
        String sql = "INSERT INTO players (username) VALUES (?)";

        // Utilizza try-with-resources per garantire la chiusura automatica
        // della connessione (conn) e del PreparedStatement (pstmt).
        try (Connection conn = DatabaseManager.getConnection(); // Ottiene connessione dal pool
             PreparedStatement pstmt = conn.prepareStatement(sql)) { // Prepara lo statement SQL

            // Imposta il parametro (?) nella query SQL con lo username fornito
            pstmt.setString(1, username);
            // Esegue l'operazione di inserimento (INSERT)
            int affectedRows = pstmt.executeUpdate();

            // Controlla se l'inserimento ha effettivamente modificato qualche riga
            if (affectedRows == 0) {
                // Se nessuna riga è stata aggiunta, qualcosa è andato storto a livello DB
                throw new SQLException("Creazione giocatore fallita, nessuna riga aggiunta (username: " + username + ").");
            }
            // Logga il successo dell'operazione
            logger.info("Giocatore '{}' aggiunto con successo al database.", username);

        } catch (SQLException e) {
            // Gestione specifica degli errori SQL
            // Controlla se l'errore è dovuto a una violazione di chiave duplicata (username già esistente)
            // Il codice SQLState "23505" è comune per questo errore (es. H2, PostgreSQL),
            // ma potrebbe variare per altri database.
            if ("23505".equals(e.getSQLState())) {
                // Logga un avviso per il tentativo di duplicato
                 logger.warn("Tentativo di inserire username duplicato: {}", username);
                // Rilancia una nuova SQLException con un messaggio più specifico e l'errore originale come causa
                 throw new SQLException("Username '" + username + "' esiste già.", e.getSQLState(), e);
            } else {
                // Per tutti gli altri errori SQL (problemi di connessione, sintassi SQL errata, etc.)
                 logger.error("Errore SQL durante l'aggiunta del giocatore '{}': {}", username, e.getMessage(), e);
                // Rilancia l'eccezione originale per farla gestire ai livelli superiori
                 throw e;
            }
        } // conn e pstmt vengono chiusi qui automaticamente dal try-with-resources
    }

    /**
     * Recupera i dati completi di un giocatore dal database dato il suo username.
     *
     * @param username Lo username del giocatore da cercare.
     * @return Un oggetto {@link PlayerRecord} contenente tutti i dati del giocatore
     * se trovato, altrimenti {@code null} se nessun giocatore corrisponde allo username.
     * @throws IllegalArgumentException Se lo username è nullo o vuoto.
     * @throws SQLException Se si verifica un errore durante l'interazione con il database.
     */
    @Override
    public PlayerRecord getPlayer(String username) throws SQLException {
        // Validazione input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto per la ricerca.");
        }

        // Query SQL per selezionare tutte le colonne per un dato username
        String sql = "SELECT username, data, ora, percorso_file_log, durata_ms, punteggio FROM players WHERE username = ?";
        PlayerRecord player = null; // Inizializza a null (valore restituito se non trovato)

        // Try-with-resources nidificato per gestire Connection, PreparedStatement e ResultSet
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta lo username come parametro nella clausola WHERE
            pstmt.setString(1, username);

            // Esegue la query di selezione e ottiene un ResultSet
            // Anche ResultSet è AutoCloseable e viene gestito dal try-with-resources interno
            try (ResultSet rs = pstmt.executeQuery()) {

                // Controlla se esiste almeno una riga nel ResultSet
                if (rs.next()) {
                    // Estrae i valori dalle colonne del ResultSet
                    Date data = rs.getDate("data"); // Ottiene la data (può essere null)
                    Time ora = rs.getTime("ora");   // Ottiene l'ora (può essere null)
                    String logPath = rs.getString("percorso_file_log"); // Ottiene il path del log (può essere null)

                    // Gestione specifica per tipi numerici che possono essere NULL nel DB
                    long durataValue = rs.getLong("durata_ms"); // Legge come primitivo
                    Long durataMs = rs.wasNull() ? null : durataValue; // Controlla se l'ultimo valore letto era NULL

                    int punteggioValue = rs.getInt("punteggio"); // Legge come primitivo
                    Integer punteggio = rs.wasNull() ? null : punteggioValue; // Controlla se era NULL

                    // Crea l'oggetto PlayerRecord con i dati estratti
                    player = new PlayerRecord(
                        rs.getString("username"), // Username (non null per definizione PK)
                        data,
                        ora,
                        logPath,
                        durataMs,
                        punteggio
                    );
                    // Log a livello debug con i dettagli del giocatore trovato
                    logger.debug("Giocatore trovato nel database: {}", player);
                } else {
                    // Se rs.next() è false, significa che la query non ha restituito righe
                    logger.debug("Giocatore con username '{}' non trovato nel database.", username);
                    // player rimane null
                }
            } // ResultSet 'rs' viene chiuso qui

        } catch (SQLException e) {
            // Gestione errori SQL durante la query
            logger.error("Errore SQL durante il recupero del giocatore '{}': {}", username, e.getMessage(), e);
            throw e; // Rilancia l'eccezione
        } // Connection 'conn' e PreparedStatement 'pstmt' vengono chiusi qui

        // Restituisce l'oggetto PlayerRecord trovato o null se non trovato
        return player;
    }

    /**
     * Aggiorna i dati di un giocatore esistente nel database per registrare una vittoria.
     * Imposta o sovrascrive data, ora, percorso del log, durata e punteggio per lo username specificato.
     *
     * @param username Lo username del giocatore da aggiornare.
     * @param data La data della vittoria (può essere null?).
     * @param ora L'ora della vittoria (può essere null?).
     * @param logFilePath Il percorso del file di log associato (può essere null?).
     * @param durataMs La durata della partita in millisecondi (può essere null).
     * @param punteggio Il punteggio ottenuto (può essere null).
     * @return {@code true} se l'aggiornamento ha modificato almeno una riga (cioè il giocatore esisteva),
     * {@code false} altrimenti (giocatore non trovato).
     * @throws IllegalArgumentException Se lo username è nullo o vuoto.
     * @throws SQLException Se si verifica un errore durante l'interazione con il database.
     */
    @Override
    public boolean recordVictory(String username, Date data, Time ora, String logFilePath, Long durataMs, Integer punteggio) throws SQLException {
        // Validazione input
         if (username == null || username.trim().isEmpty()) {
             throw new IllegalArgumentException("Username non può essere vuoto per registrare la vittoria.");
         }

        // Query SQL per aggiornare i campi di un giocatore specifico
         String sql = "UPDATE players SET data = ?, ora = ?, percorso_file_log = ?, durata_ms = ?, punteggio = ? WHERE username = ?";
         boolean updated = false; // Flag per indicare se l'update ha avuto successo

        // Try-with-resources per Connection e PreparedStatement
         try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta i parametri della query UPDATE nell'ordine corretto
             pstmt.setDate(1, data);        // Imposta il parametro 1 (data)
             pstmt.setTime(2, ora);         // Imposta il parametro 2 (ora)
             pstmt.setString(3, logFilePath); // Imposta il parametro 3 (percorso_file_log)
             // Usa setObject per i tipi Long e Integer nullable, permette al driver JDBC di gestire il tipo e il valore null
             pstmt.setObject(4, durataMs);    // Imposta il parametro 4 (durata_ms)
             pstmt.setObject(5, punteggio);   // Imposta il parametro 5 (punteggio)
             pstmt.setString(6, username);    // Imposta il parametro 6 (username nella clausola WHERE)

            // Esegue l'operazione di aggiornamento (UPDATE)
             int affectedRows = pstmt.executeUpdate();
            // Controlla se almeno una riga è stata modificata
             updated = (affectedRows > 0);

            // Logga l'esito dell'operazione
             if (updated) {
                 logger.info("Vittoria registrata/aggiornata con successo nel DB per l'utente '{}'.", username);
             } else {
                 // Se affectedRows è 0, significa che la clausola WHERE (username = ?) non ha trovato corrispondenze
                 logger.warn("Nessun utente trovato con username '{}' nel DB per registrare la vittoria.", username);
             }

         } catch (SQLException e) {
            // Gestione errori SQL durante l'aggiornamento
             logger.error("Errore SQL durante la registrazione della vittoria per '{}': {}", username, e.getMessage(), e);
             throw e; // Rilancia l'eccezione
         } // conn e pstmt vengono chiusi qui

         // Restituisce true se l'update è andato a buon fine (riga modificata), false altrimenti
         return updated;
    }

    /**
     * Elimina un giocatore dal database dato il suo username.
     *
     * @param username Lo username del giocatore da eliminare.
     * @return {@code true} se il giocatore è stato trovato ed eliminato (almeno una riga eliminata),
     * {@code false} altrimenti (giocatore non trovato).
     * @throws IllegalArgumentException Se lo username è nullo o vuoto.
     * @throws SQLException Se si verifica un errore durante l'interazione con il database.
     */
    @Override
    public boolean deletePlayer(String username) throws SQLException {
        // Validazione input
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto per l'eliminazione.");
        }

        // Query SQL per eliminare un giocatore
        String sql = "DELETE FROM players WHERE username = ?";
        boolean deleted = false; // Flag per indicare se l'eliminazione ha avuto successo

        // Try-with-resources per Connection e PreparedStatement
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Imposta lo username come parametro nella clausola WHERE
            pstmt.setString(1, username);
            // Esegue l'operazione di eliminazione (DELETE)
            int affectedRows = pstmt.executeUpdate();
            // Controlla se almeno una riga è stata eliminata
            deleted = (affectedRows > 0);

            // Logga l'esito
            if (deleted) {
                 logger.info("Giocatore '{}' eliminato con successo dal database.", username);
            } else {
                // Se affectedRows è 0, il giocatore non esisteva nel database
                 logger.warn("Nessun giocatore trovato nel database con username '{}' da eliminare.", username);
            }
        } catch (SQLException e) {
            // Gestione errori SQL durante l'eliminazione
            logger.error("Errore SQL durante l'eliminazione del giocatore '{}': {}", username, e.getMessage(), e);
            throw e; // Rilancia l'eccezione
        } // conn e pstmt vengono chiusi qui

        // Restituisce true se l'eliminazione è andata a buon fine, false altrimenti
        return deleted;
    }

    /**
     * Recupera la classifica dei giocatori dal database.
     * Seleziona solo i giocatori che hanno un punteggio registrato (diverso da NULL),
     * ordinandoli per punteggio in ordine decrescente (dal più alto al più basso).
     *
     * @return Una {@code List<RankingEntryDTO>} contenente le voci della classifica.
     * La lista sarà vuota se non ci sono giocatori con un punteggio o in caso di errore SQL
     * (anche se l'eccezione viene rilanciata).
     * @throws SQLException Se si verifica un errore durante l'interazione con il database.
     */
    @Override
    public List<RankingEntryDTO> getRanking() throws SQLException {
        List<RankingEntryDTO> ranking = new ArrayList<>(); // Inizializza la lista dei risultati
        // Query SQL per ottenere la classifica:
        // - Seleziona solo i campi necessari per il DTO RankingEntryDTO.
        // - Filtra per includere solo righe dove 'punteggio' non è nullo.
        // - Ordina i risultati in base al 'punteggio' in modo decrescente.
        String sql = "SELECT username, data, ora, punteggio FROM players " +
                     "WHERE punteggio IS NOT NULL " +
                     "ORDER BY punteggio DESC";
        // Nota: Si potrebbe aggiungere un "LIMIT N" alla fine per limitare la classifica ai primi N giocatori.

        // Usa try-with-resources per Connection, Statement e ResultSet.
        // Viene usato Statement semplice perché la query non ha parametri.
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) { // Esegue la query

            // Itera su tutte le righe restituite dal ResultSet
            while (rs.next()) {
                // Estrae i dati dalla riga corrente
                String username = rs.getString("username");
                Date data = rs.getDate("data"); // Dato che punteggio IS NOT NULL, ci aspettiamo che anche data e ora non siano nulli
                Time ora = rs.getTime("ora");   // se vengono impostati atomicamente con recordVictory.
                int punteggioValue = rs.getInt("punteggio");
                // Non è necessario rs.wasNull() per punteggio grazie alla clausola WHERE
                Integer punteggio = punteggioValue;

                // Crea un nuovo DTO con i dati estratti
                RankingEntryDTO entry = new RankingEntryDTO(username, data, ora, punteggio);
                // Aggiunge il DTO alla lista dei risultati
                ranking.add(entry);
            }
            // Log a livello debug sul numero di voci recuperate
            logger.debug("Recuperata classifica dal DB con {} voci.", ranking.size());

        } catch (SQLException e) {
            // Gestione errori SQL durante il recupero della classifica
            logger.error("Errore SQL durante il recupero della classifica: {}", e.getMessage(), e);
            throw e; // Rilancia l'eccezione
        } // conn, stmt, e rs vengono chiusi qui automaticamente

        // Restituisce la lista (può essere vuota se la query non ha trovato righe)
        return ranking;
    }
}