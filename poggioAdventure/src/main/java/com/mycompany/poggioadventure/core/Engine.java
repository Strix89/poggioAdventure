package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.ApiClientResult;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.core.utils.PoggioClientJersey;
import com.mycompany.poggioadventure.core.utils.StopWatch;
import com.mycompany.poggioadventure.persistence.LoggerInput;
import com.mycompany.poggioadventure.parser.Parser;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.cli.CLIInputHandler;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.ErrorHandler;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.persistence.SaveGame;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.CommandType;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.ui.gui.GUIOutputHandler;
import com.mycompany.poggioadventure.ui.gui.views.UI_Game;
import com.mycompany.poggioadventure.ui.gui.views.UI_Init;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Classe core dell'applicazione che gestisce il ciclo di vita del gioco.
 *
 * <p><b>Responsabilità principali:</b>
 * <ul>
 *   <li>Gestione del ciclo principale di gioco</li>
 *   <li>Elaborazione dei comandi giocatore</li>
 *   <li>Coordinamento tra modello, view e controller</li>
 *   <li>Gestione del tempo di gioco</li>
 *   <li>Salvataggio e caricamento stato gioco</li>
 * </ul>
 *
 * <p><b>Design Pattern utilizzati:</b>
 * <ul>
 *   <li>Facade Pattern: fornisce un'interfaccia semplificata ai sottosistemi</li>
 *   <li>Observer Pattern: notifica cambiamenti di stato alla view</li>
 *   <li>Singleton Pattern: gestione del cronometro di gioco</li>
 * </ul>
 *
 * @version 1.3
 * @author [Autore originale non specificato]
 */
public class Engine {
    /**
     * Istanza del modello di gioco contenente lo stato attuale
     */
    private final GameDescription game;
    
    /**
     * Parser per interpretare i comandi testuali
     */
    private Parser parser;
    
    /**
     * Nome del giocatore corrente
     */
    private String playerName;
    
    /**
     * Gestore dell'output grafico/testuale
     */
    private OutputHandler output;
    
    /**
     * Gestore dell'input da utente
     */
    private InputHandler inputHandler;
    
    /**
     * Gestore centralizzato degli errori
     */
    private ErrorHandler errorHandler;
    
    /**
     * Logger per registrare l'attività di gioco
     */
    private LoggerInput logger;
    
    /**
     * Cronometro per il tempo di sessione
     */
    private final StopWatch gameTime;
    
    /**
     * Buffer temporaneo per i comandi da loggare
     */
    private List<String> logTemp;

    /**
     * GameStateManager per gestione livelli senza dipendenze circolari
     */
    private GameStateManager gameStateManager;

    private GameContext gameContext;
    
    /**
     * Costruttore principale dell'Engine.
     *
     * <p><b>Operazioni eseguite:</b>
     * <ol>
     *   <li>Inizializza i componenti fondamentali</li>
     *   <li>Carica le stopwords per il parser</li>
     *   <li>Visualizza il messaggio di benvenuto</li>
     *   <li>Avvia il cronometro di gioco</li>
     * </ol>
     *
     * @param game Il modello di gioco da gestire
     * @param playerName Nome del giocatore
     * @param output Gestore dell'output
     * @param input Gestore dell'input
     * @param errorHandler Gestore degli errori
     * @param logger Logger per l'attività di gioco
     */
    public Engine(GameDescription game, String playerName, OutputHandler output, InputHandler input, ErrorHandler errorHandler, LoggerInput logger, boolean fromSave) {
        this.game = game;
        this.playerName = playerName;
        this.output = output;
        this.errorHandler = errorHandler;
        this.inputHandler = input;
        this.logger = logger; 
        this.logTemp = Collections.synchronizedList(new ArrayList<>());
        try {
            Set<String> stopWords = ResourceLoader.loadFileListInSet(new File(ResourceLoader.STOPWORDS_PATH.toString()));
            parser = new Parser(stopWords);
        } catch (IOException ex) {
            this.errorHandler.handleFatalError("Caricamento risorse fallito", ex);
            Utils.exitApplication(Utils.EXIT_CODE_RESOURCE_ERROR);
        }
        getGameColoredVersion();
        output.writeln(this.output instanceof GUIOutputHandler ? game.getGUIWelcomeMsg() : game.getCLIWelcomeMsg(), ColorText.WHITE);
        gameTime = StopWatch.getInstance();
        gameTime.start();
        gameContext = new GameContext(input, output, errorHandler, logTemp, gameTime);
        
        try {
            gameStateManager = new GameStateManager(
                game,
                output,
                "[PLAYER]" + playerName + "[/]",
                this::handleGameCompleted, 
                this::handleGameLoss
            );
            if (!fromSave){
                gameStateManager.startGame();
            }
        } catch (Exception e) {
            errorHandler.handleFatalError("Errore durante inizializzazione GameStateManager", e);
            Utils.exitApplication(Utils.EXIT_CODE_INITIALIZATION_ERROR);
        }
        output.write(" \nTi trovi qui: ", ColorText.WHITE);
        output.writeln(game.getCurrentRoom().getName(), ColorText.BRIGHT_YELLOW);
        
        printCursor();
    }

    public Engine(GameDescription game, String playerName, OutputHandler output, InputHandler input, ErrorHandler errorHandler, LoggerInput logger) {
        this(game, playerName, output, input, errorHandler, logger, false);
    }

    /**
     * Elabora un comando ricevuto dal giocatore.
     *
     * <p><b>Flusso operativo:</b>
     * <ol>
     *   <li>Registra il comando nel buffer dei log</li>
     *   <li>Analizza il comando con il parser</li>
     *   <li>Esegue l'azione corrispondente</li>
     *   <li>Gestisce condizioni di vittoria/fine gioco</li>
     *   <li>Aggiorna l'interfaccia utente</li>
     * </ol>
     *
     * @param command Stringa contenente il comando da elaborare
     */
    public void processCommand(String command) {
        logTemp.add(command);
        Room previousRoom = game.getCurrentRoom();
        List<AdvObject> previousInventory = new ArrayList<>(game.getInventory());
        List<AdvObject> previousObjInRoom = new ArrayList<>(game.getCurrentRoom().getObjects());

        // Supporto per comandi multipli separati da punti e virgola
        List<ParserOutput> outputs = parser.parseMultiple(command, game.getCommands(), 
            game.getCurrentRoom().getObjects(), game.getInventory());

        if (outputs.isEmpty()) {
            output.writeln("\nNon capisco quello che mi vuoi dire.", ColorText.ERROR);
        } else {
            boolean commandExecuted = false;
            for (ParserOutput p : outputs) {
                if (p == null || p.getCommand() == null) continue;

                commandExecuted = true;

                if (p.getCommand().getType() == CommandType.SAVE) {
                    saveGame();
                } else {
                    game.nextMove(List.of(p), gameContext);
                }

                // Gestione uscita dal gioco
                if (p.getCommand().getType() == CommandType.END) {
                    output.writeln("Sei un fifone, addio!", ColorText.ERROR);
                    Utils.exitApplication(Utils.EXIT_CODE_SUCCESS);
                } else if (game.getCurrentRoom() == null) {
                    output.writeln("La tua avventura termina qui! Complimenti!", ColorText.NEON_ORANGE);
                    Utils.exitApplication(Utils.EXIT_CODE_SUCCESS);
                }
            }

            if (!commandExecuted) {
                output.writeln("Non capisco quello che mi vuoi dire.", ColorText.ERROR);
            }

            if (gameStateManager != null) {
                gameStateManager.checkStateAfterCommand();
            }
        }
        printCursor();
    }

    /**
     * Avvia il ciclo principale di gioco.
     *
     * <p>Continua a elaborare comandi finché:
     * <ul>
     *   <li>Il giocatore non esce volontariamente</li>
     *   <li>Non viene raggiunta una condizione di vittoria</li>
     *   <li>Non si verifica un errore irreversibile</li>
     * </ul>
     */
    public void startGameLoop() {
        try {
            while (game.getCurrentRoom() != null && !Thread.currentThread().isInterrupted()) {
                try {
                    String command = inputHandler.getInput();
                    processCommand(command);
                } catch (RuntimeException e) {
                    if (e.getMessage().contains("Input interrupted") || 
                        e.getMessage().contains("Input stream closed")) {
                        output.writeln("\n\nGioco interrotto dall'utente.", ColorText.YELLOW);
                        break;
                    }
                    throw e; // Re-lancia se è un errore diverso
                }
            }
        } catch (Exception e) {
            if (!Thread.currentThread().isInterrupted()) {
                throw e; // Solo se non è un'interruzione volontaria
            }
        }
    }

    /**
     * Restituisce il gestore dell'output corrente.
     * @return Istanza di OutputHandler
     */
    public OutputHandler getOutput() {
        return output;
    }

    /**
     * Stampa il prompt dei comandi nell'interfaccia CLI.
     *
     * <p>Viene visualizzato solo quando l'input handler è di tipo CLI.
     */
    private void printCursor(){
        if (inputHandler instanceof CLIInputHandler) {
            output.write("\n[NEON_ORANGE]?>[/] ", ColorText.WHITE);
        }
    }

    /**
     * Visualizza la versione colorata del titolo del gioco.
     *
     * <p>Applica uno schema di colori predefinito:
     * <ul>
     *   <li>Prima e ultima riga: blu navy</li>
     *   <li>Seconda riga: rosso</li>
     *   <li>Altre righe: giallo</li>
     * </ul>
     */
    private void getGameColoredVersion() {
        String version = this.output instanceof GUIOutputHandler ? game.getGUIGameVersion() : game.getCLIGameVersion();

        String[] lines = Arrays.stream(version.split("\n"))
                               .filter(line -> !line.trim().isEmpty())
                               .toArray(String[]::new);

        IntStream.range(0, lines.length).forEach(idx -> {
            String line = lines[idx];
            ColorText color;

            if (idx == 0 || idx == lines.length - 1) {
                color = ColorText.NAVY;
            } else if (idx == 1) {
                color = ColorText.RED;
            } else {
                color = ColorText.YELLOW;
            }

            output.writeln(line, color);
        });
        output.writeln();
    }

    /**
     * Salva lo stato corrente del gioco.
     *
     * <p><b>Operazioni eseguite:</b>
     * <ol>
     *   <li>Ferma temporaneamente il cronometro</li>
     *   <li>Scrive i log pendenti</li>
     *   <li>Serializza lo stato di gioco</li>
     *   <li>Pulisce il buffer dei log</li>
     *   <li>Riavvia il cronometro</li>
     * </ol>
     */
    public void saveGame() {
        gameTime.stop();
        try {
            if (logger != null) {
                logger.logInput(logTemp);
            } 

            SaveGame.saveGame(this, output);

        } catch (IOException ex) {
            errorHandler.handleRecoverableError("Salvataggio log fallito: " + ex);
        } finally {
            logTemp.clear();
            gameTime.start();
        }
    }
    
    /**
     * Restituisce il parser dei comandi.
     * @return Istanza del parser
     */
    public Parser getParser() {
        return parser;
    }

    /**
     * Restituisce il nome del giocatore corrente.
     * @return Nome del giocatore
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Restituisce il modello di gioco corrente.
     * @return Istanza di GameDescription
     */
    public GameDescription getGame(){
        return game;
    }
    
    /**
     * Imposta il gestore dell'output.
     * @param output Nuova istanza di OutputHandler
     */
    public void setOutputHandler(OutputHandler output) {
        this.output = output;
    }
    
    /**
     * Imposta il gestore dell'input.
     * @param input Nuova istanza di InputHandler
     */
    public void setInputHandler(InputHandler input) {
        this.inputHandler = input;
    }
    
    /**
     * Imposta il gestore degli errori.
     * @param error Nuova istanza di ErrorHandler
     */
    public void setErrorHandler(ErrorHandler error) {
        this.errorHandler = error;
    }

    /**
     * Restituisce il logger di gioco.
     * @return Istanza di LoggerInput
     */
    public LoggerInput getLogger() {
        return logger;
    }
    
    /**
     * Imposta il tempo di gioco iniziale.
     * @param t Tempo iniziale in millisecondi
     */
    public void setGameTime(long t){
        this.gameTime.startFrom(t);
    }
    
    /**
     * Restituisce il tempo di gioco formattato.
     * @return Stringa formattata (hh:mm:ss)
     */
    public String getFormattedGameTime(){
        return this.gameTime.getFormattedTime();
    }
    
    /**
     * Restituisce il tempo di gioco in secondi.
     * @return Tempo trascorso in secondi
     */
    public long getLongGameTime(){
        return this.gameTime.getElapsedSeconds();
    }

    /**
     * Callback per gestire il completamento del gioco.
     */
    private void handleGameCompleted() {
        // Ferma il cronometro di gioco
        gameTime.stop();
        saveGame();
        

        output.writeln("\nGIOCO TERMINATO", ColorText.EMERALD);

        // Pausa di 3 secondi prima di chiudere
        try {
            Thread.sleep(2000); // 3000ms = 3 secondi
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
        }

        // Invio dati vittoria al server
        sendVictoryDataToServer();

        // Pausa di 1 secondo prima delle operazioni di pulizia
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        output.writeln("Eliminazione salvataggi... [RED]Non ti servono[/]!!", ColorText.YELLOW);
        deleteCurrentSaveEndGame();
        LoggerInput.deleteLogFile(logger.getPathFile());

        // Pausa di 2 secondi prima di chiudere
        try {
            Thread.sleep(2000); // 2000ms = 2 secondi
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
        }

        output.writeln("Tempo di gioco: " + getFormattedGameTime(), ColorText.WHITE);
        output.writeln("Grazie per aver giocato a poggioAdventure!", ColorText.GREEN);

        // Pausa di 2 secondi prima di chiudere
        try {
            Thread.sleep(2000); // 2000ms = 2 secondi
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Ripristina lo stato di interruzione
        }

        returnToAppropriateMenu();
    }

    /**
     * Metodo che determina automaticamente a quale menu tornare in base al tipo di OutputHandler.
     */
    private void returnToAppropriateMenu() {
        if (output instanceof GUIOutputHandler) {
            returnToGUIMenu();
        } else {
            returnToCLIMenu();
        }
    }

    /**
     * Gestisce il ritorno al menu GUI.
     */
    private void returnToGUIMenu() {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                // Chiudi la finestra di gioco corrente
                for (java.awt.Window window : java.awt.Window.getWindows()) {
                    if (window instanceof UI_Game && window.isVisible()) {
                        window.dispose();
                        break;
                    }
                }
                
                // Apri il menu principale GUI
                new UI_Init().setVisible(true);
                
            } catch (Exception e) {
                errorHandler.handleRecoverableError("Errore durante ritorno al menu GUI: " + e.getMessage());
                // Fallback: esci dall'applicazione
                Utils.exitApplication(Utils.EXIT_CODE_SUCCESS);
            }
        });
    }

    /**
     * Gestisce il ritorno al menu CLI.
     */
    private void returnToCLIMenu() {
        output.writeln("\nTornando al menu principale...", ColorText.CYAN);
        
        // Per CLI, interrompi semplicemente il thread corrente
        // Il controllo tornerà automaticamente al CLIMenu che ha chiamato startGameLoop()
        Thread.currentThread().interrupt();
    }


    /**
     * Elimina il salvataggio corrente associato a questo Engine.
     * 
     * <p>Il salvataggio viene identificato tramite il nome del giocatore.
     * Gestisce tutti i possibili scenari di errore durante l'eliminazione.
     * 
     * @return true se l'eliminazione ha avuto successo completo, false altrimenti
     */
    public boolean deleteCurrentSaveEndGame() {
        if (playerName == null || playerName.trim().isEmpty()) {
            errorHandler.handleRecoverableError("Impossibile eliminare: nome giocatore non valido");
            return false;
        }
        
        try {
            // Cerca il salvataggio corrispondente al nome del giocatore
            List<String> saveList = SaveGame.getSaveList();
            
            // Verifica se la lista dei salvataggi è stata recuperata correttamente
            if (saveList.isEmpty()) {
                output.writeln("Nessun salvataggio disponibile da eliminare.", ColorText.YELLOW);
                return true; // Tecnicamente successo, non c'è nulla da eliminare
            }
            
            String targetSave = null;
            
            for (String saveName : saveList) {
                try {
                    String saveUsername = SaveGame.getUsernameFromSave(saveName);
                    if (saveUsername != null && playerName.equals(saveUsername)) {
                        targetSave = saveName;
                        break;
                    }
                } catch (Exception e) {
                    // Log dell'errore ma continua a cercare negli altri salvataggi
                    errorHandler.handleRecoverableError("Errore lettura salvataggio '" + saveName + "': " + e.getMessage());
                    continue;
                }
            }
            
            if (targetSave == null) {
                output.writeln("Nessun salvataggio trovato per il giocatore: " + playerName, ColorText.YELLOW);
                return true; // Tecnicamente successo, non c'è nulla da eliminare
            }
            
            // Tenta l'eliminazione con gestione dettagliata degli errori
            try {
                boolean deleted = SaveGame.deleteSave(targetSave, errorHandler, false);
                
                if (deleted) {
                    output.writeln("Salvataggio '" + targetSave + "' eliminato con successo!", ColorText.YELLOW);
                    return true;
                } else {
                    // deleteSave ha restituito false - gli errori specifici sono già stati
                    // gestiti tramite errorHandler all'interno di deleteSave
                    output.writeln("Eliminazione del salvataggio '" + targetSave + "' non completata.", ColorText.ERROR);
                    return false;
                }
                
            } catch (Exception deleteException) {
                // Cattura eventuali eccezioni non gestite da deleteSave
                String errorMsg = "Errore imprevisto durante eliminazione di '" + targetSave + "': " + deleteException.getMessage();
                errorHandler.handleRecoverableError(errorMsg);
                output.writeln("Eliminazione fallita per errore imprevisto.", ColorText.ERROR);
                return false;
            }
            
        } catch (Exception generalException) {
            // Cattura errori durante il recupero della lista salvataggi o altre operazioni
            String errorMsg = "Errore durante l'accesso ai salvataggi: " + generalException.getMessage();
            errorHandler.handleRecoverableError(errorMsg);
            output.writeln("Impossibile accedere ai salvataggi per l'eliminazione.", ColorText.ERROR);
            return false;
        }
    }

    /**
     * Invia i dati della vittoria al server tramite PoggioClientJersey.
     * 
     * <p>Gestisce l'invio del punteggio e del file di log associato alla partita vincente.
     * In caso di errore, mostra un messaggio all'utente ma non interrompe il flusso.
     */
    private void sendVictoryDataToServer() {
        PoggioClientJersey gameClient = null;
        java.nio.file.Path tempLogFile = null;
        
        try {
            output.writeln("Invio dati vittoria al server...", ColorText.CYAN);
            
            // Prepara i dati della vittoria
            String currentDate = java.time.LocalDate.now().toString(); // Formato: YYYY-MM-DD
            String currentTime = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
            ); // Formato: HH:mm:ss
            long gameDurationMs = gameTime.getElapsedSeconds() * 1000; // Converti in millisecondi
            
            // Crea un file di log temporaneo decrittato per l'invio al server
            String originalLogPath = logger.getPathFile().toString();
            tempLogFile = LoggerInput.createDecryptedTempLogFile(originalLogPath, playerName);
            
            // Crea il client e invia i dati con il file decrittato
            gameClient = new PoggioClientJersey();
            ApiClientResult result = gameClient.recordVictoryWithLog(
                playerName, 
                currentDate, 
                currentTime, 
                gameDurationMs, 
                tempLogFile.toString()
            );

            // Gestisci il risultato
            switch (result) {
                case SUCCESS_OK -> {
                    output.writeln("✅ Vittoria registrata con successo sul server!", ColorText.GREEN);
                    output.writeln("Il tuo punteggio è stato aggiunto alla classifica.", ColorText.YELLOW);
                }
                case USER_NOT_FOUND -> {
                    output.writeln("Utente non trovato sul server. Dati non registrati.", ColorText.ORANGE);
                }
                case LOG_ALREADY_EXISTS -> {
                    output.writeln("Log già esistente per questa vittoria.", ColorText.ORANGE);
                }
                case FILE_ERROR -> {
                    output.writeln("Errore file di log. Impossibile inviare i dati.", ColorText.ERROR);
                }
                case CONNECTION_ERROR -> {
                    output.writeln("Errore di connessione al server.", ColorText.ERROR);
                    output.writeln("I tuoi dati di vittoria non sono stati registrati online.", ColorText.YELLOW);
                }
                case UNAUTHORIZED -> {
                    output.writeln("Non autorizzato. Verifica configurazione server.", ColorText.ERROR);
                }
                default -> {
                    output.writeln("Errore sconosciuto durante registrazione vittoria: " + result, ColorText.ERROR);
                }
            }
        } catch (Exception e) {
            errorHandler.handleRecoverableError("Errore invio dati vittoria: " + e.getMessage());       
        } finally {
            // Elimina il file temporaneo decrittato
            if (tempLogFile != null) {
                try {
                    java.nio.file.Files.deleteIfExists(tempLogFile);
                } catch (Exception e) {
                    // Ignora errori di eliminazione del file temporaneo
                }
            }
            
            // Assicura sempre la chiusura del client
            if (gameClient != null) {
                try {
                    gameClient.close();
                } catch (Exception e) {
                    // Ignora errori di chiusura
                }
            }
        }
    }

    /**
     * Gestisce la perdita totale del gioco.
     * Salva il gioco, elimina i salvataggi e torna al menu principale.
     */
    private void handleGameLoss() {
        // Ferma il cronometro di gioco
        gameTime.stop();

        saveGame();
        
        // Pausa per permettere la lettura dei messaggi
        try {
            Thread.sleep(2000); // 3 secondi
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        output.writeln("Eliminazione salvataggi... [RED]Non li meriti[/]!!", ColorText.YELLOW);
        deleteCurrentSaveEndGame();
        deletePlayerFromServer();
        try {
            Thread.sleep(2000); // 3 secondi
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        LoggerInput.deleteLogFile(logger.getPathFile());
        
        // Pausa finale prima di tornare al menu
        try {
            Thread.sleep(3000); // 2 secondi
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        returnToAppropriateMenu();
    }

    /**
     * Elimina il giocatore dal database del server.
     * Questo metodo viene chiamato quando il giocatore perde definitivamente.
     */
    private void deletePlayerFromServer() {
        PoggioClientJersey gameClient = null;
        
        try {
            output.writeln("Eliminazione dal database del server...", ColorText.YELLOW);
            
            gameClient = new PoggioClientJersey();
            ApiClientResult result = gameClient.deletePlayer(playerName);
            
            // Gestisci il risultato dell'eliminazione
            switch (result) {
                case SUCCESS_OK -> {}
                case USER_NOT_FOUND -> {}
                case CONNECTION_ERROR -> {
                    output.writeln("Errore di connessione al server durante eliminazione.", ColorText.ERROR);
                    output.writeln("Il giocatore potrebbe essere ancora presente nel database online.", ColorText.YELLOW);
                }
                case UNAUTHORIZED -> {
                    output.writeln("Non autorizzato per eliminazione dal server.", ColorText.ERROR);
                }
                default -> {
                    output.writeln("Errore sconosciuto durante eliminazione dal server: " + result, ColorText.ERROR);
                }
            }
            } catch (Exception e) {
            // Gestisce eccezioni impreviste
            errorHandler.handleRecoverableError("Errore eliminazione giocatore dal server: " + e.getMessage());
            
        } finally {
            // Assicura sempre la chiusura del client
            if (gameClient != null) {
                try {
                    gameClient.close();
                } catch (Exception e) {
                    // Ignora errori di chiusura
                }
            }
        }
    }

    /**
     * Restituisce il GameStateManager.
     * @return Istanza del GameStateManager
     */
    public GameStateManager getGameStateManager() {
        return gameStateManager;
    }
}
