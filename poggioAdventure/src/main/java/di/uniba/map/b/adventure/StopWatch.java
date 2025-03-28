package di.uniba.map.b.adventure;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author Strix89
 */
public class StopWatch {
    private Instant startTime;
    private long totalSeconds;
    private boolean isRunning;

    private StopWatch() {
        // Blocca l'inizializzazione riflessiva
        if (InstanceHolder.INSTANCE != null) {
            throw new IllegalStateException("Classe già istanziata");
        }
    }

    private static class InstanceHolder {
        static final StopWatch INSTANCE = new StopWatch();
    }

    public static StopWatch getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public synchronized void start() {
        if (!isRunning) {
            startTime = Instant.now();
            isRunning = true;
        }
    }

    public synchronized void startFrom(long initialSeconds) {
        stop();
        totalSeconds = initialSeconds;
        start();
    }

    public synchronized void stop() {
        if (isRunning) {
            totalSeconds += Duration.between(startTime, Instant.now()).getSeconds();
            isRunning = false;
        }
    }

    public synchronized void reset() {
        stop();
        totalSeconds = 0;
    }

    public synchronized long getElapsedSeconds() {
        long current = totalSeconds;
        if (isRunning) {
            current += Duration.between(startTime, Instant.now()).getSeconds();
        }
        return current;
    }

    public String getFormattedTime() {
        long seconds = getElapsedSeconds();
        return String.format("%02d:%02d:%02d",
            TimeUnit.SECONDS.toHours(seconds),
            TimeUnit.SECONDS.toMinutes(seconds) % 60,
            seconds % 60);
    }

    // Metodi di sicurezza
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected Object readResolve() {
        return getInstance();
    }
}