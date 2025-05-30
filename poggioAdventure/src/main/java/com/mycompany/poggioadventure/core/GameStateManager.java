package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.core.Engine;
import com.mycompany.poggioadventure.core.abstracts.GameState;
//import com.mycompany.poggioadventure.core.levels.LogicTestState;
//import com.mycompany.poggioadventure.core.levels.PCAssemblyState;
//import com.mycompany.poggioadventure.core.levels.RobotRevolutionState;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.core.utils.StopWatch;

/**
 * Gestore centralizzato per la transizione tra stati/livelli del gioco.
 * 
 * <p>Responsabilità:
 * <ul>
 *   <li>Mantiene lo stato corrente del gioco</li>
 *   <li>Gestisce le transizioni tra livelli</li>
 *   <li>Monitora i timer di ogni livello</li>
 *   <li>Coordina successi e fallimenti</li>
 * </ul>
 */
public class GameStateManager {
    
    private GameState currentState;
    private Engine engine;
    private StopWatch levelTimer;
    private int currentLevelIndex = 0;
    
    // Sequenza predefinita dei livelli
    private final GameState[] levels = {
        //new LogicTestState(),
        //new PCAssemblyState(),
        //new RobotRevolutionState()
    };
    
    public GameStateManager(Engine engine) {
        this.engine = engine;
        this.levelTimer = StopWatch.getInstance();
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
        
        long elapsedTime = levelTimer.getElapsedSeconds();
        
        // Verifica timeout
        if (elapsedTime >= currentState.getTimeLimit()) {
            engine.getOutput().writeln("\n⏰ TEMPO SCADUTO! Il livello verrà resettato.", ColorText.RED);
            currentState.handleFailure(engine, GameState.FailureType.LIGHT);
            return;
        }
        
        // Verifica completamento
        if (currentState.isCompleted(engine.getGame())) {
            currentState.handleSuccess(engine);
            return;
        }
        
        // Verifica condizioni di fallimento
        if (currentState.isFailureConditionMet(engine.getGame(), elapsedTime)) {
            // Determina il tipo di fallimento basandosi sulla logica del livello
            GameState.FailureType failureType = determinFailureType();
            currentState.handleFailure(engine, failureType);
        }
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
            engine.getOutput().writeln("\n🔄 Resetting livello: " + currentState.getLevelName(), ColorText.YELLOW);
            transitionToLevel(currentState);
        }
    }
    
    /**
     * Reset completo del gioco.
     */
    public void resetGame() {
        engine.getOutput().writeln("\n💀 GAME OVER! Ricominciando dall'inizio...", ColorText.RED);
        currentLevelIndex = 0;
        transitionToLevel(levels[currentLevelIndex]);
    }
    
    /**
     * Gestisce la transizione verso un nuovo livello.
     */
    private void transitionToLevel(GameState newState) {
        // Ferma il timer precedente
        levelTimer.stop();
        
        // Imposta il nuovo stato
        currentState = newState;
        
        // Inizializza il nuovo livello
        currentState.enter(engine);
        
        // Avvia il timer per il nuovo livello
        levelTimer.reset();
        levelTimer.start();
        
        // Notifica il cambio di livello
        engine.getOutput().writeln("\n🎮 LIVELLO: " + currentState.getLevelName(), ColorText.NEON_ORANGE);
        engine.getOutput().writeln("⏱️ Tempo limite: " + formatTime(currentState.getTimeLimit()), ColorText.CYAN);
    }
    
    /**
     * Determina il tipo di fallimento basandosi sul contesto.
     */
    private GameState.FailureType determinFailureType() {
        // Implementa logica specifica per determinare se il fallimento è grave o lieve
        // Per ora, defaultiamo a LIGHT (reset livello)
        return GameState.FailureType.LIGHT;
    }
    
    /**
     * Gestisce il completamento dell'intero gioco.
     */
    private void handleGameCompletion() {
        levelTimer.stop();
        engine.getOutput().writeln("\n🎉 CONGRATULAZIONI! Hai completato tutti i livelli!", ColorText.GREEN);
        engine.getOutput().writeln("🏆 Sei riuscito a superare tutte le prove di Poggio Adventure!", ColorText.GOLD);
        
        // Potresti aggiungere qui statistiche finali, salvataggio punteggio, etc.
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
    public long getCurrentLevelElapsedTime() { return levelTimer.getElapsedSeconds(); }
    public long getCurrentLevelRemainingTime() { 
        return Math.max(0, currentState.getTimeLimit() - levelTimer.getElapsedSeconds()); 
    }
    public int getCurrentLevelIndex() { return currentLevelIndex; }
}