package com.mycompany.poggioadventure.core.utils;

import java.io.Serializable;

/**
 * La classe TimeManager gestisce il countdown del tempo di gioco utilizzando un thread separato.
 * Implementa Runnable per eseguire la logica di temporizzazione in un thread indipendente.
 * La sua struttura è scalabile perché:
 *  - I valori predefiniti (durata totale ed intervallo) sono definiti come costanti, permettendo di modificarli facilmente.
 *  - La logica di temporizzazione è incapsulata all'interno della classe, rendendo possibile l'estensione per diverse modalità di output
 *    (ad esempio, console, GUI, log su file, ecc.) tramite l'astrazione del meccanismo di aggiornamento.
 */
public class TimeManager implements Runnable, Serializable {

    // Valore di default: 10 minuti in millisecondi (600.000 ms)
    private static final long TEMPO_TOTALE_DEFAULT = 600_000;
    // Valore di default: aggiornamento ogni 1 secondo (1.000 ms)
    private static final long INTERVALLO_DEFAULT = 1_000;

    // Durata totale del countdown (in millisecondi)
    private long tempoTotale;
    // Intervallo di aggiornamento del countdown (in millisecondi)
    private long intervallo;
    // Tempo già trascorso dall'avvio del timer (in millisecondi)
    private long tempoTrascorso;
    // Flag che indica se il timer è attualmente in esecuzione
    private boolean inEsecuzione;
    // Riferimento al thread che esegue il countdown
    private transient Thread thread;

    /**
     * Costruttore di default: utilizza i valori predefiniti per tempo totale e intervallo.
     * Questo rende la classe facilmente riutilizzabile senza dover specificare parametri ad ogni istanza.
     */
    public TimeManager() {
        this(TEMPO_TOTALE_DEFAULT, INTERVALLO_DEFAULT);
    }

    /**
     * Costruttore parametrizzato: permette di specificare la durata totale e l'intervallo di aggiornamento.
     * Questo favorisce la scalabilità, in quanto è possibile creare istanze con tempistiche diverse a seconda delle esigenze.
     *
     * @param tempoTotale durata totale del timer in millisecondi
     * @param intervallo intervallo di aggiornamento in millisecondi
     */
    public TimeManager(long tempoTotale, long intervallo) {
        this.tempoTotale = tempoTotale;
        this.intervallo = intervallo;
        this.tempoTrascorso = 0;
        this.inEsecuzione = false;
    }

    /**
     * Costruttore che accetta solo il tempo totale, utilizzando l'intervallo predefinito.
     * Questo è utile per scenari in cui si desidera un countdown standard senza specificare l'intervallo.
     *
     * @param tempoTotale durata totale del timer in millisecondi
     */
    public TimeManager(long tempoTotale) {
        this.tempoTotale = tempoTotale;
        this.intervallo = INTERVALLO_DEFAULT; // Usa l'intervallo predefinito
        this.tempoTrascorso = 0;
        this.inEsecuzione = false;
    }

    /**
     * Avvia il timer in un thread separato.
     * Il metodo synchronized garantisce che l'avvio del timer sia thread-safe.
     * L'uso di un thread separato permette di non bloccare il thread principale,
     * rendendo la classe scalabile anche in ambienti multi-thread (ad es. in GUI).
     */
    public synchronized void start() {
        if (!inEsecuzione) {
            inEsecuzione = true;
            thread = new Thread(this, "TimeManagerThread");
            thread.start();
        }
    }

    /**
     * Ferma il timer e interrompe il thread.
     * Anche questo metodo è synchronized per garantire la corretta gestione della concorrenza.
     */
    public synchronized void stop() {
        inEsecuzione = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * Riavvia il timer resettando il tempo trascorso.
     */
    public synchronized void restart() {
        stop();
        tempoTrascorso = 0;
        start();
    }

    /**
     * Imposta il tempo totale del timer.
     * @param tempoTotaleSecondi Tempo totale in secondi
     */
    public synchronized void setTempoTotale(int tempoTotaleSecondi) {
        this.tempoTotale = tempoTotaleSecondi * 1000L; // Converte in millisecondi
    }

    /**
     * Restituisce il tempo rimanente in secondi.
     * @return Tempo rimanente in secondi
     */
    public synchronized long getTempoRimanente() {
        long rimanente = (tempoTotale - tempoTrascorso) / 1000;
        return Math.max(0, rimanente);
    }

    /**
     * Restituisce il tempo trascorso in secondi.
     * @return Tempo trascorso in secondi
     */
    public synchronized long getTempoTrascorso() {
        return tempoTrascorso / 1000;
    }

    /**
     * Verifica se il timer è in esecuzione.
     * @return true se il timer è attivo
     */
    public synchronized boolean isRunning() {
        return inEsecuzione;
    }

    /**
     * Metodo eseguito dal thread: aggiorna il countdown ogni intervallo.
     * Utilizza sequenze ANSI per riscrivere la stessa riga, consentendo di mostrare l'aggiornamento in tempo reale
     * senza creare nuove righe di output, sebbene questo metodo sia pensato principalmente per output su console.
     *
     * La struttura di questo metodo è scalabile perché la logica del countdown è completamente incapsulata,
     * rendendo semplice l'eventuale override o l'adattamento della visualizzazione (ad es. per una GUI si potrebbe chiamare
     * un listener anziché stampare direttamente).
     */
    @Override
    public void run() {
        while (inEsecuzione && tempoTrascorso < tempoTotale) {
            try {
                // Attende per l'intervallo specificato
                Thread.sleep(intervallo);
            } catch (InterruptedException e) {
                break;
            }
            tempoTrascorso += intervallo;
        }
        inEsecuzione = false;
    }
}