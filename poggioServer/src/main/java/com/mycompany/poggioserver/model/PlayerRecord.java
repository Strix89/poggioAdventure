package com.mycompany.poggioserver.model;

import java.sql.Date;
import java.sql.Time;
import java.util.Objects;

/**
 * Entità che rappresenta i dati di un giocatore memorizzati nel database.
 * Utilizzata per il trasferimento dati tra i layer dell'applicazione.
 * 
 * La classe mantiene informazioni sulla migliore performance di un giocatore
 * inclusi punteggio, durata della partita e riferimento al log di gioco.
 * 
 * @author Strix89
 */
public class PlayerRecord {

    // Identificatore univoco del giocatore (chiave primaria)
    private String username;
    
    // Timestamp dell'ultima vittoria registrata
    private Date data;
    private Time ora;
    
    // Riferimento al file di log della partita migliore
    private String percorsoFileLog;
    
    // Metriche di performance della migliore partita
    private Long durataMs;
    private Integer punteggio;

    /**
     * Costruttore di default richiesto dai framework di serializzazione.
     */
    public PlayerRecord() { }

    /**
     * Costruttore per inizializzazione completa del record giocatore.
     * 
     * @param username identificatore univoco del giocatore
     * @param data data della vittoria registrata
     * @param ora orario della vittoria
     * @param percorsoFileLog path del file di log associato
     * @param durataMs durata della partita in millisecondi
     * @param punteggio punteggio ottenuto nella partita
     */
    public PlayerRecord(String username, Date data, Time ora, String percorsoFileLog, Long durataMs, Integer punteggio) {
        this.username = username;
        this.data = data;
        this.ora = ora;
        this.percorsoFileLog = percorsoFileLog;
        this.durataMs = durataMs;
        this.punteggio = punteggio;
    }

    // Getter e Setter standard per accesso ai campi privati

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

    /**
     * Confronta due record player per uguaglianza logica basata su tutti i campi.
     * Gestisce correttamente i valori null attraverso Objects.equals().
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerRecord that = (PlayerRecord) o;
        return Objects.equals(username, that.username) &&
               Objects.equals(data, that.data) &&
               Objects.equals(ora, that.ora) &&
               Objects.equals(percorsoFileLog, that.percorsoFileLog) &&
               Objects.equals(durataMs, that.durataMs) &&
               Objects.equals(punteggio, that.punteggio);
    }

    /**
     * Calcola hash code consistente con equals() per corretto funzionamento 
     * nelle collezioni hash-based.
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, data, ora, percorsoFileLog, durataMs, punteggio);
    }

    /**
     * Rappresentazione testuale dell'oggetto per debugging e logging.
     */
    @Override
    public String toString() {
        return "PlayerRecord{" +
               "username='" + username + '\'' +
               ", data=" + data +
               ", ora=" + ora +
               ", percorsoFileLog='" + percorsoFileLog + '\'' +
               ", durataMs=" + durataMs +
               ", punteggio=" + punteggio +
               '}';
    }
}