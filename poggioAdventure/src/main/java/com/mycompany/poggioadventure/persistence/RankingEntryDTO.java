package com.mycompany.poggioadventure.persistence;

import java.sql.Date; // Tipo SQL per rappresentare solo la data (giorno, mese, anno)
import java.sql.Time; // Tipo SQL per rappresentare solo l'ora (ore, minuti, secondi)
import java.util.Objects; // Utility per operazioni comuni su oggetti (es. equals, hashCode, toString)

/**
 * DTO (Data Transfer Object) per rappresentare una singola voce nella classifica dei giocatori.
 * Questa classe è un semplice POJO (Plain Old Java Object) che incapsula i dati
 * di una riga della classifica, facilitando il trasferimento di questi dati
 * tra diversi layer dell'applicazione (es. dal backend al frontend) o per la
 * (de)serializzazione (es. JSON).
 * <p>
 * Contiene solo i campi essenziali per visualizzare una voce nella classifica.
 * Segue le convenzioni JavaBeans (campi privati, costruttore di default, getter/setter pubblici).
 */
public class RankingEntryDTO {

    // --- Campi ---
    private String username;    // Lo username univoco del giocatore.
    private Date data;        // La data (solo giorno, mese, anno) dell'ultima vittoria significativa registrata per questo utente.
    private Time ora;         // L'ora (solo ore, minuti, secondi) dell'ultima vittoria significativa registrata per questo utente.
    private Integer punteggio;  // Il punteggio ottenuto in quella specifica vittoria. Usare Integer permette valori nulli se il punteggio non fosse disponibile.

    // --- Costruttori ---

    /**
     * Costruttore vuoto (no-argument).
     * Necessario per molte librerie e framework (es. JAX-RS per la deserializzazione JSON,
     * JPA, alcuni framework di data binding) che richiedono la possibilità di istanziare
     * l'oggetto senza argomenti prima di popolarne i campi (tramite setter o reflection).
     */
    public RankingEntryDTO() {
    }

    /**
     * Costruttore completo per inizializzare tutti i campi.
     * Utile per creare istanze del DTO con tutti i dati già disponibili.
     *
     * @param username Lo username del giocatore.
     * @param data La data della vittoria.
     * @param ora L'ora della vittoria.
     * @param punteggio Il punteggio ottenuto.
     */
    public RankingEntryDTO(String username, Date data, Time ora, Integer punteggio) {
        this.username = username;
        this.data = data;
        this.ora = ora;
        this.punteggio = punteggio;
    }

    // --- Getters e Setters ---
    // Metodi standard pubblici per accedere e modificare i campi privati.
    // Fondamentali per la conformità allo standard JavaBeans e richiesti da
    // molte librerie (es. Jackson per JSON binding, framework UI per data binding).

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public Time getOra() { return ora; }
    public void setOra(Time ora) { this.ora = ora; }

    public Integer getPunteggio() { return punteggio; }
    public void setPunteggio(Integer punteggio) { this.punteggio = punteggio; }

    // --- Metodi Standard (equals, hashCode, toString) ---
    // Sovrascritti per fornire implementazioni corrette e utili.
    // - equals/hashCode: essenziali per il corretto funzionamento dell'oggetto
    //   all'interno di collezioni basate su hash (HashMap, HashSet) e per confronti logici.
    // - toString: utile per il debugging e il logging, fornisce una rappresentazione testuale dell'oggetto.

    /**
     * Confronta questo oggetto con un altro per verificarne l'uguaglianza.
     * Due istanze sono considerate uguali se tutti i loro campi (username, data, ora, punteggio)
     * sono uguali (usando Objects.equals per gestire correttamente i null).
     *
     * @param o L'oggetto da confrontare.
     * @return true se gli oggetti sono uguali, false altrimenti.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Stessa istanza in memoria
        if (o == null || getClass() != o.getClass()) return false; // Oggetto nullo o di classe diversa
        RankingEntryDTO that = (RankingEntryDTO) o; // Cast sicuro dopo controllo getClass()
        // Confronta tutti i campi usando Objects.equals (null-safe)
        return Objects.equals(username, that.username) &&
               Objects.equals(data, that.data) &&
               Objects.equals(ora, that.ora) &&
               Objects.equals(punteggio, that.punteggio);
    }

    /**
     * Calcola un codice hash per l'oggetto basato sui valori dei suoi campi.
     * Coerente con il metodo equals: oggetti uguali secondo equals() devono avere lo stesso hashCode().
     * Utilizza Objects.hash per combinare gli hash dei singoli campi in modo standard.
     *
     * @return Il codice hash calcolato per l'istanza.
     */
    @Override
    public int hashCode() {
        // Calcola l'hash combinando gli hash di tutti i campi
        return Objects.hash(username, data, ora, punteggio);
    }

    /**
     * Restituisce una rappresentazione testuale (String) dell'oggetto.
     * Utile per il logging e il debugging, mostra i nomi dei campi e i loro valori.
     *
     * @return Una stringa che rappresenta lo stato dell'oggetto.
     */
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