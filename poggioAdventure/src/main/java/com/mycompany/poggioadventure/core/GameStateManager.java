package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.abstracts.GameState;
import com.mycompany.poggioadventure.core.levels.Level1State;
import com.mycompany.poggioadventure.core.levels.Level2State;
import com.mycompany.poggioadventure.core.levels.Level3State;
import com.mycompany.poggioadventure.core.utils.TimeManager;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.gui.GUIOutputHandler;

import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.Room;

import java.util.List;

/**
 * Gestore centralizzato per la progressione e gestione dei livelli di gioco.
 * 
 * <p>Fornisce funzionalità per:
 * <ul>
 *   <li>Transizione automatica tra livelli</li>
 *   <li>Sistema di checkpoint e reset per timeout</li>
 *   <li>Monitoraggio condizioni di completamento e fallimento</li>
 *   <li>Gestione del timing per ogni livello</li>
 * </ul>
 * 
 * <p>Utilizza callback per comunicare eventi critici all'Engine senza creare
 * dipendenze circolari. Mantiene snapshot dello stato per reset affidabili.
 */
public class GameStateManager {
    
    private GameState currentState;
    private GameDescription gameDescription;
    
    /** Snapshot completo della mappa per ripristino checkpoint */
    private GameMap levelMapSnapshot;
    
    /** Snapshot dell'inventario al checkpoint */
    private List<AdvObject> levelInventorySnapshot;
    
    /** Snapshot della stanza iniziale del livello */
    private Room levelStartingRoomSnapshot;
    
    private String playerName;
    private OutputHandler output;
    private TimeManager timeManager;
    private int currentLevelIndex = 0;
    private long levelStartTime;
    
    /** Callback per eventi di gioco - evitano dipendenze circolari */
    private Runnable onGameCompleted;
    private Runnable onGameLoss;
    private Runnable onSaveGame;
    
    /** Configurazione livelli con limiti di tempo e oggetti richiesti */
    private final GameState[] levels = {
        new Level1State(Utils.LEVEL_1_TIME_LIMIT, Utils.LEVEL_1_REQUIRED_OBJECTS, Utils.LEVEL_1_FORBIDDEN_OBJECTS),
        new Level2State(Utils.LEVEL_2_TIME_LIMIT, Utils.LEVEL_2_REQUIRED_OBJECTS, Utils.LEVEL_2_FORBIDDEN_OBJECTS),
        new Level3State(Utils.LEVEL_3_TIME_LIMIT, Utils.LEVEL_3_REQUIRED_OBJECTS, Utils.LEVEL_3_FORBIDDEN_OBJECTS)
    };
    
    /**
     * Inizializza il gestore con dependency injection per evitare accoppiamenti.
     * 
     * @param gameDescription Stato principale del mondo di gioco
     * @param output Handler per comunicazione con l'utente
     * @param playerName Identificativo del giocatore
     * @param onGameCompleted Callback per vittoria completa
     * @param onGameLoss Callback per sconfitta definitiva
     * @param onSaveGame Callback per salvataggio automatico
     */
    public GameStateManager(GameDescription gameDescription, OutputHandler output, 
                           String playerName, Runnable onGameCompleted, 
                           Runnable onGameLoss, Runnable onSaveGame) {
        this.gameDescription = gameDescription;
        this.output = output;
        this.playerName = playerName;
        this.onGameCompleted = onGameCompleted;
        this.onGameLoss = onGameLoss;
        this.onSaveGame = onSaveGame;
        this.levelMapSnapshot = null;
        this.levelInventorySnapshot = null;
    }
    
    public void startGame() {
        currentLevelIndex = 0;
        transitionToLevel(levels[currentLevelIndex], true);
    }
    
    /**
     * Valuta lo stato del gioco dopo ogni comando eseguito.
     * Controlla timeout, condizioni di fallimento e completamento in sequenza.
     */
    public void checkStateAfterCommand() {
        if (currentState == null) return;
        
        // Verifica scadenza tempo - priorità massima
        if (timeManager.getTempoRimanente() <= 0) {
            output.writeln("\n[RED]⏰ TEMPO SCADUTO![/] Ricominciando dal checkpoint del livello...");
            resetCurrentLevel();
            return;
        }

        // Verifica condizioni di fallimento specifiche del livello
        if (currentState.isFailureConditionMet(gameDescription)) {
            currentState.handleFailure(this::handleGameLoss, gameDescription);
        }
        
        // Verifica altre condizioni di completamento
        if (currentState.isCompleted(gameDescription)) {
            currentState.handleSuccess(this::advanceToNextLevel, gameDescription);
            return;
        }
    }
    
    /**
     * Gestisce sconfitta definitiva con thread separato per GUI per evitare deadlock.
     */
    private void handleGameLoss() {
        if (timeManager != null) {
            timeManager.stop();
        }
        
        if (output instanceof GUIOutputHandler) {
            // Thread separato per evitare blocco EDT
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    onGameLoss.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "GameLossHandler").start();
        } else {
            if (onGameLoss != null) {
                onGameLoss.run();
            }
        }
    }
    
    public void advanceToNextLevel() {
        currentLevelIndex++;
        
        if (currentLevelIndex >= levels.length) {
            handleGameCompletion();
        } else {
            transitionToLevel(levels[currentLevelIndex], true);
        }
    }
    
    /**
     * Ripristina il livello corrente allo stato del checkpoint.
     * Esegue deep clone di tutti gli snapshot per garantire isolamento completo.
     */
    public void resetCurrentLevel() {
        if (currentState != null && levelMapSnapshot != null && levelInventorySnapshot != null) {
            output.writeln("\n🔄 Resetting livello: " + currentState.getLevelName(), ColorText.YELLOW);

            if (timeManager != null) {
                timeManager.stop();
            }
            
            // Ripristino completo dello stato dal checkpoint
            gameDescription.setInventory(Utils.cloneList(levelInventorySnapshot));
            gameDescription.setGameMap((GameMap) Utils.deepClone(levelMapSnapshot));
            
            // Ripristino stanza corrente con fallback robusti
            if (levelStartingRoomSnapshot != null) {
                Room restoredRoom = gameDescription.getGameMap().findRoomById(levelStartingRoomSnapshot.getId());
                if (restoredRoom != null) {
                    gameDescription.setCurrentRoom(restoredRoom);
                } else {
                    Room clonedStartingRoom = (Room) Utils.deepClone(levelStartingRoomSnapshot);
                    gameDescription.setCurrentRoom(clonedStartingRoom);
                }
            } else {
                // Fallback: usa configurazione del GameState
                Room startingRoom = currentState.getStartingRoom();
                if (startingRoom != null) {
                    Room restoredRoom = gameDescription.getGameMap().findRoomById(startingRoom.getId());
                    if (restoredRoom != null) {
                        gameDescription.setCurrentRoom(restoredRoom);
                    } else {
                        gameDescription.setCurrentRoom(startingRoom);
                    }
                }
            }
            
            // Reset timer al limite originale del livello
            long levelTimeMillis = currentState.getTimeLimit();
            timeManager = new TimeManager(levelTimeMillis);
            timeManager.start();

            // Salvataggio automatico pre-reset
            if (onSaveGame != null) {
                output.writeln("Vedi che sto salvando.. t piacess a essere il 1° in classifica", ColorText.BRIGHT_YELLOW);
                onSaveGame.run();
            }
            
            levelStartTime = System.currentTimeMillis();
            
            long timeInMinutes = timeManager.getTempoRimanente() / 60;
            
            output.writeln("=".repeat(50), ColorText.YELLOW);
            currentState.getLevelDescription(output, playerName, String.valueOf(timeInMinutes));
            output.writeln("=".repeat(50), ColorText.YELLOW);
            
            if (gameDescription.getCurrentRoom() != null) {
                output.writeln("\nTi trovi qui: [YELLOW]" + gameDescription.getCurrentRoom().getName() + "[/]", ColorText.WHITE);
                output.writeln("================================================", ColorText.WHITE);
                output.writeln(gameDescription.getCurrentRoom().getDescription(), ColorText.WHITE);
            }
        }
    }
    
    /**
     * Gestisce transizione tra livelli con creazione opzionale di checkpoint.
     * 
     * @param newState Nuovo livello da attivare
     * @param createSnapshot Se true, crea snapshot per reset futuro
     */
    private void transitionToLevel(GameState newState, boolean createSnapshot) {
        currentState = newState;
        
        currentState.enter(gameDescription, output, playerName);
        
        if (currentState.getStartingRoom() != null) {
            gameDescription.setCurrentRoom(currentState.getStartingRoom());
        }
        
        // Creazione snapshot solo per nuovi livelli
        if (createSnapshot) {
            levelMapSnapshot = (GameMap) Utils.deepClone(gameDescription.getGameMap());
            levelInventorySnapshot = Utils.cloneList(gameDescription.getInventory());
            levelStartingRoomSnapshot = (Room) Utils.deepClone(currentState.getStartingRoom());
        }
        
        levelStartTime = System.currentTimeMillis();
        long levelTimeMillis = currentState.getTimeLimit();

        if (timeManager != null) {
            timeManager.stop();
        }
        
        // Nuovo TimeManager per reset completo del timing
        timeManager = new TimeManager(levelTimeMillis);
        currentState.getLevelDescription(output, playerName, String.valueOf(timeManager.getTempoRimanente() / 60));
        timeManager.start();
    }
    
    /**
     * Gestisce completamento dell'intero gioco con thread separato per GUI.
     */
    private void handleGameCompletion() {
        if (timeManager != null) {
            timeManager.stop();
        }
        
        output.writeln("\n" + "=".repeat(60), ColorText.GOLD);
        output.writeln("🎉 CONGRATULAZIONI! HAI COMPLETATO POGGIO ADVENTURE! 🎉", ColorText.GREEN);
        output.writeln("=".repeat(60), ColorText.GOLD);
        output.writeln("");
        output.writeln("🏆 Sei riuscito a superare tutte le prove di Poggiolevante!", ColorText.CYAN);
        output.writeln("💎 Hai dimostrato di avere le competenze necessarie per entrare nel collegio!", ColorText.YELLOW);
        output.writeln("");
        output.writeln("=".repeat(60), ColorText.GOLD);
        
        if (output instanceof GUIOutputHandler) {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    onGameCompleted.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "GameCompletionHandler").start();
        } else {
            if (onGameCompleted != null) {
                onGameCompleted.run();
            }
        }
    }

    /**
     * Ripristina stato da salvataggio con calcolo preciso del tempo rimanente.
     * Gestisce timeout durante il caricamento con reset automatico.
     */
    public void restoreFromSave(int savedLevelIndex, long savedLevelElapsedTime, GameMap savedLevelMapSnapshot, List<AdvObject> savedLevelInventorySnapshot, Room savedLevelStartingRoomSnapshot) {
        this.currentLevelIndex = savedLevelIndex;
        this.levelMapSnapshot = (GameMap) Utils.deepClone(savedLevelMapSnapshot); 
        this.levelInventorySnapshot = Utils.cloneList(savedLevelInventorySnapshot);
        this.levelStartingRoomSnapshot = (Room) Utils.deepClone(savedLevelStartingRoomSnapshot);
        
        if (currentLevelIndex >= 0 && currentLevelIndex < levels.length) {
            currentState = levels[currentLevelIndex];
            
            levelStartTime = System.currentTimeMillis() - savedLevelElapsedTime;
            
            // Calcolo tempo rimanente con validazione
            long levelTimeLimit = currentState.getTimeLimit();
            long remainingTime = levelTimeLimit - savedLevelElapsedTime;
            
            if (remainingTime > 0) {
                if (timeManager != null) {
                    timeManager.stop();
                }
                
                timeManager = new TimeManager(remainingTime);
                timeManager.start();

                output.writeln();

                String remainingTimeFormatted = String.format("%02d:%02d", 
                    timeManager.getTempoRimanente() / 60, 
                    timeManager.getTempoRimanente() % 60);
                currentState.getLevelDescription(output, playerName, String.valueOf(remainingTimeFormatted));
                output.write(" \nTi trovi qui: ", ColorText.WHITE);
                output.writeln(gameDescription.getCurrentRoom().getName(), ColorText.BRIGHT_YELLOW);
            } else {
                output.writeln("\n[RED]⏰ TEMPO SCADUTO durante il caricamento![/] Resettando il livello...", ColorText.RED);
                resetCurrentLevel();
            }
        }
    }
    
    // Getters
    public GameState getCurrentState() { return currentState; }
    
    public long getCurrentLevelElapsedTime() { 
        return System.currentTimeMillis() - levelStartTime; 
    }
    
    public int getCurrentLevelIndex() { return currentLevelIndex; }
    
    /** Tempo rimanente in secondi per display UI */
    public long getCurrentLevelRemainingTimeSeconds() {
        if (timeManager == null) return 0;
        return timeManager.getTempoRimanente();
    }

    /** Tempo rimanente in millisecondi per calcoli interni */
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

    public GameMap getLevelMapSnapshot() {
        return levelMapSnapshot;
    }

    public List<AdvObject> getLevelInventorySnapshot() {
        return levelInventorySnapshot;
    }

    public Room getLevelStartingRoomSnapshot() {
        return levelStartingRoomSnapshot;
    }
}