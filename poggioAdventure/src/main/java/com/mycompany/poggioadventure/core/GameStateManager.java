package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.abstracts.GameState;
import com.mycompany.poggioadventure.core.levels.Level1State;
import com.mycompany.poggioadventure.core.utils.TimeManager;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.model.AdvObject;

import java.util.List;

/**
 * Gestore centralizzato per la transizione tra stati/livelli del gioco.
 * 
 * <p>Risolve le dipendenze circolari usando callback pattern e dependency injection.
 * Gestisce il timing dei livelli tramite TimeManager.
 */
public class GameStateManager {
    
    private GameState currentState;
    private GameDescription gameDescription;
    private GameDescription snapshotGameDesc;
    private String playerName;
    private OutputHandler output;
    private TimeManager timeManager;
    private int currentLevelIndex = 0;
    private long levelStartTime;
    
    // Callback per comunicare con Engine senza dipendenza diretta
    private Runnable onGameCompleted;
    private Runnable onGameReset;
    
    // Sequenza predefinita dei livelli
    private final GameState[] levels = {
        new Level1State(Utils.LEVEL_1_TIME_LIMIT, Utils.LEVEL_1_REQUIRED_OBJECTS, Utils.LEVEL_1_FORBIDDEN_OBJECTS),
        //new Level2State(), 
        //new Level3State()
    };
    
    /**
     * Costruttore con dependency injection per evitare dipendenze circolari.
     * 
     * @param gameDescription Stato del gioco
     * @param output Handler per output utente
     * @param timeManager Gestore del tempo di gioco
     * @param onGameCompleted Callback per completamento gioco
     * @param onGameReset Callback per reset gioco
     */
    public GameStateManager(GameDescription gameDescription, OutputHandler output, 
                           TimeManager timeManager, String playerName, Runnable onGameCompleted, 
                           Runnable onGameReset) {
        this.gameDescription = gameDescription;
        this.output = output;
        this.timeManager = timeManager;
        this.playerName = playerName;
        this.onGameCompleted = onGameCompleted;
        this.onGameReset = onGameReset;
        snapshotGameDesc = (GameDescription) gameDescription.clone();
    }
    
    /**
     * Inizializza il primo livello del gioco.
     */
    public void startGame() {
        currentLevelIndex = 0;
        transitionToLevel(levels[currentLevelIndex]);
    }
    
    /**
     * Verifica lo stato corrente dopo l'esecuzione di un comando.
     * Controlla completamento, fallimento e timeout.
     */
    public void checkStateAfterCommand() {
        if (currentState == null) return;
        
        long elapsedTime = System.currentTimeMillis() - levelStartTime;
        
        // Verifica timeout usando TimeManager
        if (timeManager.getTempoRimanente() <= 0 || elapsedTime >= currentState.getTimeLimit()) {
            output.writeln("\n[RED]⏰ TEMPO SCADUTO![/] Ricominciando dall'inizio...");
            currentState.handleFailure(GameState.FailureType.SEVERE, this::resetGame);
            return;
        }
        
        // Verifica completamento tramite oggetti nell'inventario
        if (checkInventoryCompletion()) {
            output.writeln("\n🎉 LIVELLO COMPLETATO!", ColorText.GREEN);
            currentState.handleSuccess(this::advanceToNextLevel);
            return;
        }
        
        // Verifica altre condizioni di completamento
        if (currentState.isCompleted(gameDescription)) {
            currentState.handleSuccess(this::advanceToNextLevel);
            return;
        }
        
        // Verifica condizioni di fallimento
        if (currentState.isFailureConditionMet(gameDescription, elapsedTime)) {
            GameState.FailureType failureType = determinFailureType();
            currentState.handleFailure(failureType, 
                failureType == GameState.FailureType.SEVERE ? this::resetGame : this::resetCurrentLevel);
        }
    }
    
    /**
     * Verifica se il giocatore ha gli oggetti necessari per completare il livello.
     */
    private boolean checkInventoryCompletion() {
        if (currentState == null) return false;
        
        List<Integer> requiredObjects = currentState.getRequiredIDObjects();
        if (requiredObjects.isEmpty()) return false;
        
        List<AdvObject> inventory = gameDescription.getInventory();
        
        // Verifica che tutti gli oggetti richiesti siano presenti
        for (Integer requiredId : requiredObjects) {
            boolean found = inventory.stream()
                .anyMatch(obj -> obj.getId() == requiredId);
            if (!found) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Transizione al livello successivo.
     */
    public void advanceToNextLevel() {
        currentLevelIndex++;
        
        if (currentLevelIndex >= levels.length) {
            // Gioco completato!
            handleGameCompletion();
        } else {
            transitionToLevel(levels[currentLevelIndex]);
        }
    }
    
    /**
     * Reset del livello corrente.
     */
    public void resetCurrentLevel() {
        if (currentState != null) {
            output.writeln("\n🔄 Resetting livello: " + currentState.getLevelName(), ColorText.YELLOW);
            transitionToLevel(currentState);
        }
    }
    
    /**
     * Reset completo del gioco.
     */
    public void resetGame() {
        output.writeln("\n💀 GAME OVER! Ricominciando dall'inizio...", ColorText.RED);
        currentLevelIndex = 0;
        
        // Reset del TimeManager usando il nuovo metodo
        timeManager.restart();
        
        transitionToLevel(levels[currentLevelIndex]);
        
        // Notifica Engine per reset completo
        if (onGameReset != null) {
            onGameReset.run();
        }
    }
    
    /**
     * Gestisce la transizione verso un nuovo livello.
     */
    private void transitionToLevel(GameState newState) {
        // Imposta il nuovo stato
        currentState = newState;
        
        // Inizializza il nuovo livello tramite GameState
        currentState.enter(gameDescription, output, playerName);
        
        // Imposta la stanza iniziale del livello
        if (currentState.getStartingRoom() != null) {
            gameDescription.setCurrentRoom(currentState.getStartingRoom());
        }
        
        // Registra l'inizio del livello
        levelStartTime = System.currentTimeMillis();
        
        // Configura TimeManager per questo livello (ora con metodo corretto)
        long levelTimeSeconds = currentState.getTimeLimit() / 1000;
        timeManager.stop(); // Ferma il timer corrente
        timeManager.setTempoTotale((int) levelTimeSeconds);
        timeManager.start(); // Riavvia con il nuovo tempo
        
        currentState.getLevelDescription(output, playerName);
    }
    
    /**
     * Determina il tipo di fallimento basandosi sul contesto.
     */
    private GameState.FailureType determinFailureType() {
        // Logica per determinare se il fallimento è grave o lieve
        // Per timeout defaultiamo a SEVERE (reset completo)
        long elapsedTime = System.currentTimeMillis() - levelStartTime;
        
        if (elapsedTime >= currentState.getTimeLimit() * 0.9) {
            return GameState.FailureType.SEVERE; // Vicino al timeout
        }
        
        return GameState.FailureType.LIGHT; // Fallimento recoverable
    }
    
    /**
     * Gestisce il completamento dell'intero gioco.
     */
    private void handleGameCompletion() {
        output.writeln("\n🎉 CONGRATULAZIONI! Hai completato tutti i livelli!", ColorText.GREEN);
        output.writeln("🏆 Sei riuscito a superare tutte le prove di Poggio Adventure!", ColorText.GOLD);
        
        // Ferma il TimeManager
        timeManager.stop();
        
        // Statistiche finali usando TimeManager (metodo corretto)
        output.writeln("⏱️ Tempo totale: " + formatTime(timeManager.getTempoTrascorso() * 1000), ColorText.CYAN);
        
        // Notifica Engine per completamento
        if (onGameCompleted != null) {
            onGameCompleted.run();
        }
    }
    
    /**
     * Formatta il tempo in formato leggibile.
     */
    private String formatTime(long milliseconds) {
        long minutes = milliseconds / 60000;
        long seconds = (milliseconds % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    // Getters
    public GameState getCurrentState() { return currentState; }
    
    public long getCurrentLevelElapsedTime() { 
        return System.currentTimeMillis() - levelStartTime; 
    }
    
    public long getCurrentLevelRemainingTime() { 
        if (currentState == null) return 0;
        return Math.max(0, currentState.getTimeLimit() - getCurrentLevelElapsedTime()); 
    }
    
    public int getCurrentLevelIndex() { return currentLevelIndex; }
    
    /**
     * Restituisce il tempo rimanente del TimeManager in millisecondi.
     */
    public long getTimeManagerRemainingTime() {
        return timeManager.getTempoRimanente() * 1000L;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public GameDescription getGameDescription() {
        return gameDescription;
    }

    public void setGameDescription(GameDescription gameDescription) {
        this.gameDescription = gameDescription;
        this.snapshotGameDesc = (GameDescription) gameDescription.clone();
    }

    public GameDescription getOldGameDescription() {
        return snapshotGameDesc;
    }
}