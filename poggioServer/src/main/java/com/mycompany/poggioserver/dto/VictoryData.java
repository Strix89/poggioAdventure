package com.mycompany.poggioserver.dto;

// DTO per ricevere i dati della vittoria via JSON
/**
 *
 * @author Strix89
 */
public class VictoryData {
    private String data; // Formato atteso: YYYY-MM-DD
    private String ora;  // Formato atteso: HH:MM:SS
    private String percorsoFileLog;

    // Costruttore default per JSON
    public VictoryData() {}

    // Getters e Setters
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    public String getOra() { return ora; }
    public void setOra(String ora) { this.ora = ora; }
    public String getPercorsoFileLog() { return percorsoFileLog; }
    public void setPercorsoFileLog(String percorsoFileLog) { this.percorsoFileLog = percorsoFileLog; }
}
