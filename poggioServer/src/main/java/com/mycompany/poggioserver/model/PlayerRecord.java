package com.mycompany.poggioserver.model;

import java.sql.Date;
import java.sql.Time;
import java.util.Objects;

// Principio OOP: Incapsulamento dei dati del giocatore
/**
 *
 * @author Strix89
 */
public class PlayerRecord {
    private String username; // Chiave primaria, non null
    private Date data;       // Data vittoria, può essere null
    private Time ora;        // Ora vittoria, può essere null
    private String percorsoFileLog; // Path log, può essere null
    private Long durataMs;

    // Costruttore di default necessario per alcune librerie JSON/Framework
    public PlayerRecord() { }

    public PlayerRecord(String username, Date data, Time ora, String percorsoFileLog, Long durataMs) {
        this.username = username;
        this.data = data;
        this.ora = ora;
        this.percorsoFileLog = percorsoFileLog;
        this.durataMs = durataMs; // NUOVO
    }

    // Getters e Setters
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

    // equals, hashCode, toString (Buona pratica)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerRecord that = (PlayerRecord) o;
        return Objects.equals(username, that.username) &&
               Objects.equals(data, that.data) &&
               Objects.equals(ora, that.ora) &&
               Objects.equals(percorsoFileLog, that.percorsoFileLog) &&
               Objects.equals(durataMs, that.durataMs); // NUOVO
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, data, ora, percorsoFileLog, durataMs); // NUOVO
    }

    @Override
    public String toString() {
        return "PlayerRecord{" +
               "username='" + username + '\'' +
               ", data=" + data +
               ", ora=" + ora +
               ", percorsoFileLog='" + percorsoFileLog + '\'' +
               ", durataMs=" + durataMs + // NUOVO
               '}';
    }
}
