package com.mycompany.poggioadventure.core.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Classe singleton per la misurazione del tempo trascorso.
 * Implementa un cronometro con funzionalità di start, stop, reset e
 * visualizzazione del tempo trascorso in formato leggibile.
 * 
 * @author Strix89
 */
public class StopWatch {
    // Tempo di inizio del cronometro
    private Instant startTime;
    
    // Secondi totali accumulati (per pause/riprese)
    private long totalSeconds;
    
    // Flag che indica se il cronometro è in esecuzione
    private boolean isRunning;

    /**
     * Costruttore privato per implementare il pattern Singleton.
     * Blocca anche l'inizializzazione riflessiva.
     * 
     * @throws IllegalStateException se si tenta di creare una seconda istanza
     */
    private StopWatch() {
        if (InstanceHolder.INSTANCE != null) {
            throw new IllegalStateException("Classe già istanziata");
        }
    }

    /**
     * Holder per l'inizializzazione lazy del Singleton.
     * Garantisce thread-safety senza bisogno di sincronizzazione.
     */
    private static class InstanceHolder {
        static final StopWatch INSTANCE = new StopWatch();
    }

    /**
     * Restituisce l'unica istanza del cronometro.
     * 
     * @return Istanza singleton di StopWatch
     */
    public static StopWatch getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Avvia il cronometro se non è già in esecuzione.
     */
    public synchronized void start() {
        if (!isRunning) {
            startTime = Instant.now();
            isRunning = true;
        }
    }

    /**
     * Avvia il cronometro con un valore iniziale predefinito.
     * 
     * @param initialSeconds Valore iniziale in secondi da cui partire
     */
    public synchronized void startFrom(long initialSeconds) {
        stop();
        totalSeconds = initialSeconds;
        start();
    }

    /**
     * Ferma il cronometro se è in esecuzione.
     * Il tempo trascorso viene accumulato per eventuali riprese.
     */
    public synchronized void stop() {
        if (isRunning) {
            totalSeconds += Duration.between(startTime, Instant.now()).getSeconds();
            isRunning = false;
        }
    }

    /**
     * Resetta il cronometro azzerando il tempo accumulato.
     */
    public synchronized void reset() {
        stop();
        totalSeconds = 0;
    }

    /**
     * Restituisce il tempo totale trascorso in secondi.
     * Include sia il tempo accumulato che quello in corso se il cronometro è attivo.
     * 
     * @return Tempo totale trascorso in secondi
     */
    public synchronized long getElapsedSeconds() {
        long current = totalSeconds;
        if (isRunning) {
            current += Duration.between(startTime, Instant.now()).getSeconds();
        }
        return current;
    }

    /**
     * Restituisce il tempo trascorso formattato in ore:minuti:secondi (HH:MM:SS).
     * 
     * @return Stringa formattata con il tempo trascorso
     */
    public String getFormattedTime() {
        long seconds = getElapsedSeconds();
        return String.format("%02d:%02d:%02d",
            TimeUnit.SECONDS.toHours(seconds),
            TimeUnit.SECONDS.toMinutes(seconds) % 60,
            seconds % 60);
    }

    // Metodi di sicurezza per il Singleton

    /**
     * Impedisce la clonazione dell'istanza.
     * 
     * @throws CloneNotSupportedException sempre
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Garantisce che durante la deserializzazione venga restituita l'istanza singleton.
     * 
     * @return L'istanza singleton
     */
    protected Object readResolve() {
        return getInstance();
    }
}