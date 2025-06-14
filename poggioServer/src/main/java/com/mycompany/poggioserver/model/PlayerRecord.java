package com.mycompany.poggioserver.model;

// Import Tipi SQL per Date e Time
import java.sql.Date; // Rappresenta una data SQL (anno, mese, giorno)
import java.sql.Time; // Rappresenta un'ora SQL (ore, minuti, secondi)
// Import per utility Objects (usato in equals, hashCode)
import java.util.Objects;

/**
 * Rappresenta il record completo dei dati di un giocatore (Player) così come
 * memorizzato nel database. Questa classe funge da modello di dati (o entità semplice)
 * per trasportare le informazioni relative a un giocatore tra i vari layer
 * dell'applicazione server (es. tra DAO e Resource).
 * <p>
 * Segue le convenzioni JavaBeans con campi privati, costruttore di default,
 * costruttore con argomenti, e metodi getter/setter pubblici.
 * Include anche implementazioni standard per {@code equals()}, {@code hashCode()}, e {@code toString()}.
 * </p>
 * // Principio OOP: Incapsulamento dei dati del giocatore (Commento originale mantenuto)
 *
 */
public class PlayerRecord {

    // --- Campi ---
    // Corrispondono alle colonne della tabella 'players' nel database.

    /** Lo username univoco del giocatore (chiave primaria della tabella). Non dovrebbe essere null. */
    private String username;
    /** La data dell'ultima vittoria registrata con punteggio. Può essere null se nessuna vittoria è registrata o se il record è appena stato creato. */
    private Date data;
    /** L'ora dell'ultima vittoria registrata con punteggio. Può essere null. */
    private Time ora;
    /** Il percorso (path) completo del file di log associato all'ultima vittoria registrata. Può essere null. */
    private String percorsoFileLog;
    /** La durata della partita (associata all'ultima vittoria) espressa in millisecondi. Può essere null. */
    private Long durataMs; // Usa Long (wrapper) per permettere valori null
    /** Il punteggio ottenuto nell'ultima vittoria registrata. Può essere null. */
    private Integer punteggio; // Usa Integer (wrapper) per permettere valori null

    // --- Costruttori ---

    /**
     * Costruttore di default (senza argomenti).
     * Necessario per alcune tecnologie/framework (es. JAX-B, JPA, alcune librerie JSON)
     * che richiedono un costruttore pubblico senza argomenti per poter istanziare l'oggetto.
     */
    public PlayerRecord() { }

    /**
     * Costruttore completo per creare un'istanza di PlayerRecord con tutti i campi inizializzati.
     * Nota: Include il campo 'punteggio' aggiunto successivamente.
     *
     * @param username Lo username del giocatore.
     * @param data La data della vittoria.
     * @param ora L'ora della vittoria.
     * @param percorsoFileLog Il percorso del file di log.
     * @param durataMs La durata della partita in ms.
     * @param punteggio Il punteggio ottenuto.
     */
    // AGGIORNATO COSTRUTTORE (Commento originale mantenuto)
    public PlayerRecord(String username, Date data, Time ora, String percorsoFileLog, Long durataMs, Integer punteggio) {
        this.username = username;
        this.data = data;
        this.ora = ora;
        this.percorsoFileLog = percorsoFileLog;
        this.durataMs = durataMs;
        this.punteggio = punteggio; // NUOVO (Commento originale mantenuto)
    }

    // --- Getters e Setters ---
    // Metodi pubblici standard per accedere (get) e modificare (set)
    // i valori dei campi privati, seguendo la convenzione JavaBeans.

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Time getOra() {
        return ora;
    }

    public void setOra(Time ora) {
        this.ora = ora;
    }

    public String getPercorsoFileLog() {
        return percorsoFileLog;
    }

    public void setPercorsoFileLog(String percorsoFileLog) {
        this.percorsoFileLog = percorsoFileLog;
    }

    public Long getDurataMs() {
        return durataMs;
    }

    public void setDurataMs(Long durataMs) {
        this.durataMs = durataMs;
    }

    public Integer getPunteggio() {
        return punteggio;
    }

    public void setPunteggio(Integer punteggio) {
        this.punteggio = punteggio;
    }

    // --- Metodi Standard (equals, hashCode, toString) ---
    // Implementazioni standard generate che considerano tutti i campi della classe.
    // Fondamentali per il corretto comportamento in collezioni e per il debugging.
    // equals, hashCode, toString (Buona pratica) (Commento originale mantenuto)

    /**
     * Confronta questo PlayerRecord con un altro oggetto per verificarne l'uguaglianza logica.
     * Due record sono considerati uguali se tutti i loro campi corrispondenti sono uguali
     * (utilizzando {@code Objects.equals} per la gestione corretta dei valori null).
     *
     * @param o L'oggetto da confrontare con questo PlayerRecord.
     * @return {@code true} se gli oggetti sono logicamente uguali, {@code false} altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Ottimizzazione: stesso oggetto in memoria
        if (o == null || getClass() != o.getClass()) return false; // Oggetto nullo o di tipo diverso
        PlayerRecord that = (PlayerRecord) o; // Cast sicuro
        // Confronto campo per campo usando Objects.equals (null-safe)
        return Objects.equals(username, that.username) &&
               Objects.equals(data, that.data) &&
               Objects.equals(ora, that.ora) &&
               Objects.equals(percorsoFileLog, that.percorsoFileLog) &&
               Objects.equals(durataMs, that.durataMs) &&
               Objects.equals(punteggio, that.punteggio); // NUOVO (Commento originale mantenuto)
    }

    /**
     * Calcola un codice hash per questa istanza di PlayerRecord.
     * Il codice hash è basato sui valori di tutti i campi della classe, garantendo che
     * oggetti uguali secondo {@code equals()} abbiano lo stesso {@code hashCode()}.
     * Utilizza il metodo helper {@code Objects.hash()}.
     *
     * @return Il codice hash calcolato per questo oggetto.
     */
    @Override
    public int hashCode() {
        // Calcola l'hash combinando gli hash di tutti i campi
        return Objects.hash(username, data, ora, percorsoFileLog, durataMs, punteggio); // NUOVO (Commento originale mantenuto)
    }

    /**
     * Restituisce una rappresentazione testuale (String) di questo oggetto PlayerRecord.
     * Include i nomi dei campi e i loro valori correnti. Utile per il logging e il debugging.
     *
     * @return Una stringa che descrive lo stato dell'oggetto.
     */
    @Override
    public String toString() {
        return "PlayerRecord{" +
               "username='" + username + '\'' +
               ", data=" + data +
               ", ora=" + ora +
               ", percorsoFileLog='" + percorsoFileLog + '\'' +
               ", durataMs=" + durataMs +
               ", punteggio=" + punteggio + // NUOVO (Commento originale mantenuto)
               '}';
    }
}