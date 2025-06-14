package com.mycompany.poggioserver.resources;

import java.sql.Date;
import java.sql.Time;
import java.util.Objects;

/**
 * DTO per una voce della classifica giocatori.
 * Contiene solo i campi necessari per la visualizzazione.
 * 
 * @author Strix89
 */
public class RankingEntryDTO {
    private String username;
    private Date data;      // Data ultima vittoria
    private Time ora;       // Ora ultima vittoria
    private Integer punteggio;

    // Costruttore vuoto per serializzazione JSON
    public RankingEntryDTO() {
    }

    public RankingEntryDTO(String username, Date data, Time ora, Integer punteggio) {
        this.username = username;
        this.data = data;
        this.ora = ora;
        this.punteggio = punteggio;
    }

    // Getters e Setters per serializzazione JSON
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }
    public Time getOra() { return ora; }
    public void setOra(Time ora) { this.ora = ora; }
    public Integer getPunteggio() { return punteggio; }
    public void setPunteggio(Integer punteggio) { this.punteggio = punteggio; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankingEntryDTO that = (RankingEntryDTO) o;
        return Objects.equals(username, that.username) && Objects.equals(data, that.data) && Objects.equals(ora, that.ora) && Objects.equals(punteggio, that.punteggio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, data, ora, punteggio);
    }

    @Override
    public String toString() {
        return "RankingEntryDTO{" +
               "username='" + username + '\'' +
               ", data=" + data +
               ", ora=" + ora +
               ", punteggio=" + punteggio +
               '}';
    }
}