package com.mycompany.poggioadventure.core.utils;

import java.io.Serializable;

/**
 * Gestore temporizzazione per countdown di gioco con esecuzione thread-safe.
 * 
 * <p>Implementa countdown configurabile con thread separato per non bloccare
 * il flusso principale dell'applicazione. Supporta serializzazione per
 * salvataggio dello stato e operazioni thread-safe per ambienti concorrenti.
 * 
 * <p><b>Caratteristiche:</b>
 * <ul>
 *   <li>Esecuzione non bloccante in thread dedicato</li>
 *   <li>Configurazione flessibile di durata e intervalli</li>
 *   <li>Operazioni thread-safe con sincronizzazione</li>
 *   <li>Gestione robusta interruzioni thread</li>
 *   <li>Supporto serializzazione per persistenza stato</li>
 * </ul>
 */
public class TimeManager implements Runnable, Serializable {

    /** Durata countdown predefinita: 10 minuti */
    private static final long TEMPO_TOTALE_DEFAULT = 600_000;
    
    /** Intervallo aggiornamento predefinito: 1 secondo */
    private static final long INTERVALLO_DEFAULT = 1_000;

    /** Durata totale countdown in millisecondi */
    private long tempoTotale;
    
    /** Intervallo di aggiornamento in millisecondi */
    private long intervallo;
    
    /** Tempo trascorso dall'avvio in millisecondi */
    private long tempoTrascorso;
    
    /** Flag stato esecuzione timer */
    private boolean inEsecuzione;
    
    /** Thread dedicato per countdown (transient per serializzazione) */
    private transient Thread thread;

    /** Costruttore con valori predefiniti (10 min, 1 sec intervallo) */
    public TimeManager() {
        this(TEMPO_TOTALE_DEFAULT, INTERVALLO_DEFAULT);
    }

    /**
     * Costruttore con configurazione completa di tempistiche.
     * 
     * @param tempoTotale Durata totale timer in millisecondi
     * @param intervallo Intervallo aggiornamento in millisecondi
     */
    public TimeManager(long tempoTotale, long intervallo) {
        this.tempoTotale = tempoTotale;
        this.intervallo = intervallo;
        this.tempoTrascorso = 0;
        this.inEsecuzione = false;
    }

    /**
     * Costruttore con durata personalizzata e intervallo predefinito.
     * 
     * @param tempoTotale Durata totale timer in millisecondi
     */
    public TimeManager(long tempoTotale) {
        this.tempoTotale = tempoTotale;
        this.intervallo = INTERVALLO_DEFAULT;
        this.tempoTrascorso = 0;
        this.inEsecuzione = false;
    }

    /**
     * Avvia countdown in thread separato con protezione thread-safe.
     * Previene avvii multipli simultanei.
     */
    public synchronized void start() {
        if (!inEsecuzione) {
            inEsecuzione = true;
            thread = new Thread(this, "TimeManagerThread");
            thread.start();
        }
    }

    /**
     * Ferma countdown e interrompe thread di esecuzione.
     * Operazione thread-safe per terminazione pulita.
     */
    public synchronized void stop() {
        inEsecuzione = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    /** Riavvia countdown azzerando tempo trascorso */
    public synchronized void restart() {
        stop();
        tempoTrascorso = 0;
        start();
    }

    /**
     * Configura durata totale timer convertendo da secondi.
     * 
     * @param tempoTotaleSecondi Durata in secondi
     */
    public synchronized void setTempoTotale(int tempoTotaleSecondi) {
        this.tempoTotale = tempoTotaleSecondi * 1000L;
    }

    /**
     * Calcola tempo rimanente evitando valori negativi.
     * 
     * @return Tempo rimanente in secondi (min 0)
     */
    public synchronized long getTempoRimanente() {
        long rimanente = (tempoTotale - tempoTrascorso) / 1000;
        return Math.max(0, rimanente);
    }

    /**
     * Restituisce tempo trascorso dall'avvio.
     * 
     * @return Tempo trascorso in secondi
     */
    public synchronized long getTempoTrascorso() {
        return tempoTrascorso / 1000;
    }

    /** Verifica se timer è attualmente in esecuzione */
    public synchronized boolean isRunning() {
        return inEsecuzione;
    }

    /**
     * Loop principale countdown eseguito in thread dedicato.
     * Gestisce interruzioni per terminazione pulita e aggiorna
     * progressione temporale ad intervalli configurati.
     */
    @Override
    public void run() {
        while (inEsecuzione && tempoTrascorso < tempoTotale) {
            try {
                Thread.sleep(intervallo);
            } catch (InterruptedException e) {
                break; // Terminazione richiesta
            }
            tempoTrascorso += intervallo;
        }
        inEsecuzione = false;
    }
}