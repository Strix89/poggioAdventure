package com.mycompany.poggioadventure.core.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Implementazione thread-safe di un cronometro ad alta precisione.
 * 
 * Questa classe singleton fornisce funzionalità per tracciare il tempo di gioco,
 * supportando operazioni di avvio, pausa, ripresa e reset. Utilizza java.time
 * per misurazioni precise e gestisce correttamente le pause accumulando il tempo.
 * 
 * Pattern implementati:
 * - Singleton (thread-safe con inizializzazione lazy)
 */
public class StopWatch {
    /** Timestamp dell'ultimo avvio/ripresa del cronometro */
    private Instant startTime;
    
    /** Tempo totale accumulato durante le sessioni precedenti (in secondi) */
    private long totalSeconds;
    
    /** Flag che indica se il cronometro è attualmente in esecuzione */
    private boolean isRunning;

    /**
     * Costruttore privato per garantire singleton.
     * 
     * Include una protezione aggiuntiva contro la creazione di istanze multiple
     * tramite reflection, verificando che l'holder non contenga già un'istanza.
     */
    private StopWatch() {
        if (InstanceHolder.INSTANCE != null) {
            throw new IllegalStateException("Classe già istanziata");
        }
    }

    /**
     * Classe interna statica per inizializzazione lazy e thread-safe del singleton.
     * 
     * Questo approccio sfrutta il meccanismo di class loading di Java per garantire
     * thread-safety senza costi di sincronizzazione esplicita.
     */
    private static class InstanceHolder {
        /** Unica istanza della classe, creata al primo accesso a InstanceHolder */
        static final StopWatch INSTANCE = new StopWatch();
    }

    /**
     * Ottiene l'istanza singleton del cronometro.
     * 
     * @return Istanza condivisa di StopWatch, creata solo al primo accesso
     */
    public static StopWatch getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Avvia il cronometro se non è già in esecuzione.
     * 
     * Salva il timestamp corrente come punto di partenza per il calcolo
     * del tempo trascorso nella sessione attuale.
     */
    public synchronized void start() {
        if (!isRunning) {
            startTime = Instant.now();
            isRunning = true;
        }
    }

    /**
     * Avvia il cronometro partendo da un valore predefinito.
     * 
     * Utile per ripristinare lo stato da un salvataggio o per
     * iniziare il conteggio da un offset specifico.
     * 
     * @param initialSeconds Secondi iniziali da cui partire
     */
    public synchronized void startFrom(long initialSeconds) {
        stop();
        totalSeconds = initialSeconds;
        start();
    }

    /**
     * Ferma il cronometro, accumulando il tempo trascorso.
     * 
     * Quando il cronometro viene fermato, il tempo trascorso dall'ultimo avvio
     * viene calcolato e aggiunto al totale accumulato, preservando la misurazione
     * per future riprese.
     */
    public synchronized void stop() {
        if (isRunning) {
            totalSeconds += Duration.between(startTime, Instant.now()).getSeconds();
            isRunning = false;
        }
    }

    /**
     * Azzera il cronometro, eliminando tutto il tempo accumulato.
     * 
     * Ferma il cronometro se è in esecuzione e resetta il contatore a zero.
     * Utilizzato tipicamente all'inizio di un nuovo gioco o livello.
     */
    public synchronized void reset() {
        stop();
        totalSeconds = 0;
    }

    /**
     * Calcola il tempo totale trascorso in secondi.
     * 
     * Se il cronometro è in esecuzione, aggiunge al totale accumulato
     * anche il tempo trascorso dall'ultimo avvio fino ad ora.
     * 
     * @return Numero totale di secondi trascorsi
     */
    public synchronized long getElapsedSeconds() {
        long current = totalSeconds;
        if (isRunning) {
            current += Duration.between(startTime, Instant.now()).getSeconds();
        }
        return current;
    }

    /**
     * Formatta il tempo trascorso in una stringa leggibile (HH:MM:SS).
     * 
     * Converte i secondi totali in ore, minuti e secondi, formattando
     * ciascun componente con due cifre e separatori.
     * 
     * @return Tempo formattato come stringa "ore:minuti:secondi"
     */
    public String getFormattedTime() {
        long seconds = getElapsedSeconds();
        return String.format("%02d:%02d:%02d",
            TimeUnit.SECONDS.toHours(seconds),
            TimeUnit.SECONDS.toMinutes(seconds) % 60,
            seconds % 60);
    }

    /**
     * Impedisce la clonazione dell'istanza singleton.
     * 
     * Parte della difesa contro la violazione del pattern singleton,
     * garantisce che non sia possibile duplicare l'istanza.
     * 
     * @throws CloneNotSupportedException sempre, impedendo la clonazione
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Gestisce correttamente la deserializzazione per mantenere il singleton.
     * 
     * Se l'oggetto viene deserializzato, questo metodo garantisce che venga
     * restituita l'istanza singleton invece di creare un nuovo oggetto.
     * 
     * @return L'istanza singleton esistente
     */
    protected Object readResolve() {
        return getInstance();
    }
}