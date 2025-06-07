package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.abstracts.GameState;
import com.mycompany.poggioadventure.core.levels.Level1State;
import com.mycompany.poggioadventure.core.levels.Level2State;
import com.mycompany.poggioadventure.core.utils.TimeManager;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.gui.GUIOutputHandler;

import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.Room;

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
    private GameMap levelMapSnapshot; // Snapshot della mappa per reset del livello
    private List<AdvObject> levelInventorySnapshot; // Snapshot dell'inventario per reset del livello
    private Room levelStartingRoomSnapshot; // Snapshot della stanza iniziale del livello per reset
    private String playerName;
    private OutputHandler output;
    private TimeManager timeManager;
    private int currentLevelIndex = 0;
    private long levelStartTime;
    
    // Callback per comunicare con Engine senza dipendenza diretta
    private Runnable onGameCompleted;
    private Runnable onGameLoss;
    private Runnable onSaveGame;
    
    // Sequenza predefinita dei livelli
    private final GameState[] levels = {
        new Level1State(Utils.LEVEL_1_TIME_LIMIT, Utils.LEVEL_1_REQUIRED_OBJECTS, Utils.LEVEL_1_FORBIDDEN_OBJECTS),
        new Level2State(Utils.LEVEL_2_TIME_LIMIT, Utils.LEVEL_2_REQUIRED_OBJECTS, Utils.LEVEL_2_FORBIDDEN_OBJECTS) 
        //new Level3State()
    };
    
    /**
     * Costruttore con dependency injection per evitare dipendenze circolari.
     * 
     * @param gameDescription Stato del gioco
     * @param output Handler per output utente
     * @param onGameCompleted Callback per completamento gioco
     * @param onGameLoss Callback per perdita gioco
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
    
    /**
     * Inizializza il primo livello del gioco.
     */
    public void startGame() {
        currentLevelIndex = 0;
        // Primo livello - crea snapshot
        transitionToLevel(levels[currentLevelIndex], true);
    }
    
    /**
     * Verifica lo stato corrente dopo l'esecuzione di un comando.
     * Controlla completamento, fallimento e timeout.
     */
    public void checkStateAfterCommand() {
        if (currentState == null) return;
        
        // Verifica timeout usando TimeManager
        if (timeManager.getTempoRimanente() <= 0) {
            output.writeln("\n[RED]⏰ TEMPO SCADUTO![/] Ricominciando dal checkpoint del livello...");
            resetCurrentLevel();
            return;
        }

        // Verifica condizioni di fallimento - reset del livello
        if (currentState.isFailureConditionMet(gameDescription)) {
            currentState.handleFailure(this::handleGameLoss);
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
    }
    
    /**
     * Gestisce la perdita totale del gioco.
     * Mostra messaggi di game over e delega a Engine per la gestione finale.
     */
    private void handleGameLoss() {
        // Ferma il TimeManager
        if (timeManager != null) {
            timeManager.stop();
        }
        // Per GUI, esegui il callback in un thread separato per evitare di bloccare l'EDT
        if (output instanceof GUIOutputHandler) {
            // Esegui il callback in un thread separato
            if (onGameLoss != null) {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // Pausa per permettere la lettura dei messaggi
                        onGameLoss.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, "GameLossHandler").start();
            }
        } else {
            // Per CLI, esegui direttamente
            if (onGameLoss != null) {
                onGameLoss.run();
            }
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
            // Avanza al livello successivo - crea nuovo snapshot
            transitionToLevel(levels[currentLevelIndex], true);
        }
    }
    
    /**
     * Reset del livello corrente al checkpoint salvato.
     */
    public void resetCurrentLevel() {
        if (currentState != null && levelMapSnapshot != null && levelInventorySnapshot != null) {
            output.writeln("\n🔄 Resetting livello: " + currentState.getLevelName(), ColorText.YELLOW);

            // Ferma il TimeManager
            if (timeManager != null) {
                timeManager.stop();
            }
            
            // CAMBIATO: Ripristina inventario e mappa usando i metodi di GameDescription
            gameDescription.setInventory(Utils.cloneList(levelInventorySnapshot));
            gameDescription.setGameMap((GameMap) Utils.deepClone(levelMapSnapshot));
            
            // CAMBIATO: Ripristina la stanza corrente usando lo snapshot della stanza iniziale
            if (levelStartingRoomSnapshot != null) {
                // Trova la stanza corrispondente nella mappa ripristinata
                Room restoredRoom = gameDescription.getGameMap().findRoomById(levelStartingRoomSnapshot.getId());
                if (restoredRoom != null) {
                    gameDescription.setCurrentRoom(restoredRoom);
                } else {
                    // Fallback: clona la stanza iniziale snapshot se non trovata nella mappa
                    Room clonedStartingRoom = (Room) Utils.deepClone(levelStartingRoomSnapshot);
                    gameDescription.setCurrentRoom(clonedStartingRoom);
                }
            } else {
                // Fallback: usa la stanza corrente del GameState se non c'è snapshot
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
            
            // Reset del TimeManager al tempo limite del livello
            long levelTimeMillis = currentState.getTimeLimit();
            timeManager = new TimeManager(levelTimeMillis);
            timeManager.start();

            // AGGIUNTO: Salva il gioco prima del reset
            if (onSaveGame != null) {
                output.writeln("Vedi che sto salvando.. t piacess a essere il 1° in classifica", ColorText.BRIGHT_YELLOW);
                onSaveGame.run();
            }
            
            // Registra il nuovo inizio del livello
            levelStartTime = System.currentTimeMillis();
            
            // Mostra informazioni di reset
            long timeInMinutes = timeManager.getTempoRimanente() / 60;
            
            // Mostra la descrizione del livello dopo il reset
            output.writeln("=".repeat(50), ColorText.YELLOW);
            currentState.getLevelDescription(output, playerName, String.valueOf(timeInMinutes));
            output.writeln("=".repeat(50), ColorText.YELLOW);
            
            // Mostra la stanza corrente
            if (gameDescription.getCurrentRoom() != null) {
                output.writeln("\nTi trovi qui: [YELLOW]" + gameDescription.getCurrentRoom().getName() + "[/]", ColorText.WHITE);
                output.writeln("================================================", ColorText.WHITE);
                output.writeln(gameDescription.getCurrentRoom().getDescription(), ColorText.WHITE);
            }
        }
    }
    
    /**
     * Gestisce la transizione verso un nuovo livello.
     * 
     * @param newState Il nuovo stato/livello
     * @param createSnapshot Se true, crea un nuovo snapshot per il reset
     */
    private void transitionToLevel(GameState newState, boolean createSnapshot) {
        // Imposta il nuovo stato
        currentState = newState;
        
        // Inizializza il nuovo livello tramite GameState
        currentState.enter(gameDescription, output, playerName);
        
        // Imposta la stanza iniziale del livello
        if (currentState.getStartingRoom() != null) {
            gameDescription.setCurrentRoom(currentState.getStartingRoom());
        }
        
        // CAMBIATO: Crea snapshot separati SOLO se è un nuovo livello
        if (createSnapshot) {
            levelMapSnapshot = (GameMap) Utils.deepClone(gameDescription.getGameMap());
            levelInventorySnapshot = Utils.cloneList(gameDescription.getInventory());
            levelStartingRoomSnapshot = (Room) Utils.deepClone(currentState.getStartingRoom());
        }
        
        // Registra l'inizio del livello
        levelStartTime = System.currentTimeMillis();

        long levelTimeMillis = currentState.getTimeLimit();

       // Ferma il timer precedente se esiste
        if (timeManager != null) {
            timeManager.stop();
        }
        
        // Crea sempre un nuovo TimeManager per garantire reset completo
        timeManager = new TimeManager(levelTimeMillis);
        currentState.getLevelDescription(output, playerName, String.valueOf(timeManager.getTempoRimanente() / 60));
        timeManager.start();
    }
    
    /**
     * Gestisce il completamento dell'intero gioco.
     */
    private void handleGameCompletion() {
        // Ferma il TimeManager prima di mostrare i messaggi
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
        
        // Per GUI, esegui il callback in un thread separato per evitare di bloccare l'EDT
        if (output instanceof GUIOutputHandler) {
            // Esegui il callback in un thread separato
            if (onGameCompleted != null) {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // Pausa per permettere la lettura dei messaggi
                        onGameCompleted.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }, "GameCompletionHandler").start();
            }
        } else {
            if (onGameCompleted != null) {
                onGameCompleted.run();
            }
        }
    }

    public void restoreFromSave(int savedLevelIndex, long savedLevelElapsedTime, GameMap savedLevelMapSnapshot, List<AdvObject> savedLevelInventorySnapshot, Room savedLevelStartingRoomSnapshot) {
        this.currentLevelIndex = savedLevelIndex;
        // CAMBIATO: Ripristina snapshot separati invece di GameDescription completo
        this.levelMapSnapshot = (GameMap) Utils.deepClone(savedLevelMapSnapshot); 
        this.levelInventorySnapshot = Utils.cloneList(savedLevelInventorySnapshot);
        this.levelStartingRoomSnapshot = (Room) Utils.deepClone(savedLevelStartingRoomSnapshot);
        
        // Assicurati che l'indice sia valido
        if (currentLevelIndex >= 0 && currentLevelIndex < levels.length) {
            currentState = levels[currentLevelIndex];
            
            // Ripristina il tempo di inizio del livello
            levelStartTime = System.currentTimeMillis() - savedLevelElapsedTime;
            
            // Configura il TimeManager con il tempo rimanente corretto
            long levelTimeLimit = currentState.getTimeLimit(); // in millisecondi
            long remainingTime = levelTimeLimit - savedLevelElapsedTime;
            
            if (remainingTime > 0) {
                // C'è ancora tempo rimanente
                if (timeManager != null) {
                    timeManager.stop(); // Ferma il thread del timer precedente, se esistente
                }
                // Crea una nuova istanza di TimeManager.
                // Il costruttore TimeManager(long) imposta tempoTrascorso a 0 e tempoTotale a remainingTime.
                // remainingTime è già in millisecondi, come richiesto dal costruttore.
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
                // Il tempo è scaduto, gestisci timeout
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
    
    /**
     * Restituisce il tempo rimanente del TimeManager in secondi (per UI).
     */
    public long getCurrentLevelRemainingTimeSeconds() {
        if (timeManager == null) return 0;
        return timeManager.getTempoRimanente(); // getTempoRimanente() già restituisce secondi
    }

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