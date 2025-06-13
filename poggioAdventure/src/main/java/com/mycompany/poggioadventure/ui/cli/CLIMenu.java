package com.mycompany.poggioadventure.ui.cli;

import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.ui.ColorText;

import com.mycompany.poggioadventure.persistence.SaveGame;
import com.mycompany.poggioadventure.core.Engine;
import com.mycompany.poggioadventure.core.utils.ApiClientResult;
import com.mycompany.poggioadventure.core.utils.EngineFactory;
import com.mycompany.poggioadventure.core.abstracts.MenuManager;
import com.mycompany.poggioadventure.core.utils.PoggioClientJersey;
import com.mycompany.poggioadventure.persistence.LoggerInput;
import com.mycompany.poggioadventure.persistence.RankingEntryDTO;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.ErrorHandler;

import java.io.IOException;
import java.util.List;

/**
 * Implementazione CLI del sistema menu con gestione completa UI console.
 * 
 * <p>Gestisce navigazione menu principale, creazione/caricamento partite,
 * visualizzazione classifica e download log con interfaccia testuale colorata.
 * Integra validazioni server per user management e gestione graceful errori.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Menu principale con ASCII art e navigazione interattiva</li>
 *   <li>Creazione nuove partite con validazione user esistenza</li>
 *   <li>Sistema caricamento/eliminazione salvataggi con conferme</li>
 *   <li>Classifica online con download log giocatori</li>
 *   <li>Gestione shutdown hook per cleanup risorse</li>
 * </ul>
 * 
 * <p><b>Pattern implementati:</b>
 * <ul>
 *   <li>Strategy: implementazione CLI di MenuManager</li>
 *   <li>Facade: semplificazione accesso a Engine/SaveGame/API</li>
 *   <li>Observer: gestione eventi shutdown per cleanup</li>
 * </ul>
 */
public class CLIMenu implements MenuManager {

    /** Handler output console con supporto colori */
    private final OutputHandler output;

    /** Handler input console per acquisizione comandi */
    private final InputHandler scanner;

    /** Handler errori centralizzato per logging e recovery */
    private final ErrorHandler errorHan;

    /**
     * Inizializza handlers CLI e carica risorse necessarie.
     * Configura ambiente console con gestione errori fatali.
     */
    public CLIMenu() {
        this.output = new CLIOutputHandler();
        this.scanner = new CLIInputHandler();
        this.errorHan = new CLIErrorHandler();

        try {
            ResourceLoader.loadResourcesForCLI();
        } catch (IOException ex) {
            errorHan.handleFatalError("ERRORE: Inizializzazione risorse CLI fallita", ex);
            Utils.exitApplication(Utils.EXIT_CODE_RESOURCE_ERROR);
        }
    }

    /**
     * Mostra menu principale con loop interattivo e ASCII art.
     * Configura shutdown hook per cleanup automatico risorse e gestisce
     * navigazione tra opzioni con error recovery.
     */
    @Override
    public void showMainMenu() {
        Thread mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            output.writeln("\n\nUscita dal gioco...\n", ColorText.CRIMSON);
            ResourceLoader.cleanOrphanedLogs();
            mainThread.interrupt();
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }));
        
        // ASCII Art rendering
        output.writeln("", ColorText.WHITE);
        output.writeln("                ██████╗  ██████╗  ██████╗  ██████╗ ██╗ ██████╗               ", ColorText.BRIGHT_RED);
        output.writeln("                ██╔══██╗██╔═══██╗██╔════╝ ██╔════╝ ██║██╔═══██╗              ", ColorText.BRIGHT_RED);
        output.writeln("                ██████╔╝██║   ██║██║  ███╗██║  ███╗██║██║   ██║              ", ColorText.BRIGHT_RED);
        output.writeln("                ██╔═══╝ ██║   ██║██║   ██║██║   ██║██║██║   ██║              ", ColorText.BRIGHT_RED);
        output.writeln("                ██║     ╚██████╔╝╚██████╔╝╚██████╔╝██║╚██████╔╝              ", ColorText.BRIGHT_RED);
        output.writeln("                ╚═╝      ╚═════╝  ╚═════╝  ╚═════╝ ╚═╝ ╚═════╝               ", ColorText.BRIGHT_RED);
        output.writeln("                                                                             ", ColorText.WHITE);
        output.writeln(" █████╗ ██████╗ ██╗   ██╗███████╗███╗   ██╗████████╗██╗   ██╗██████╗ ███████╗", ColorText.BRIGHT_BLUE);
        output.writeln("██╔══██╗██╔══██╗██║   ██║██╔════╝████╗  ██║╚══██╔══╝██║   ██║██╔══██╗██╔════╝", ColorText.BRIGHT_BLUE);
        output.writeln("███████║██║  ██║██║   ██║█████╗  ██╔██╗ ██║   ██║   ██║   ██║██████╔╝█████╗  ", ColorText.BRIGHT_BLUE);
        output.writeln("██╔══██║██║  ██║╚██╗ ██╔╝██╔══╝  ██║╚██╗██║   ██║   ██║   ██║██╔══██╗██╔══╝  ", ColorText.BRIGHT_BLUE);
        output.writeln("██║  ██║██████╔╝ ╚████╔╝ ███████╗██║ ╚████║   ██║   ╚██████╔╝██║  ██║███████╗", ColorText.BRIGHT_BLUE);
        output.writeln("╚═╝  ╚═╝╚═════╝   ╚═══╝  ╚══════╝╚═╝  ╚═══╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝╚══════╝", ColorText.BRIGHT_BLUE);
        output.writeln("                                                                             ", ColorText.WHITE);
        
        output.write("\n[BRIGHT_RED]=================================[/] [BRIGHT_BLUE]TEXT[/][WHITE] -[/]", ColorText.NAVY);
        output.write(" [BRIGHT_RED]GAME[/]", ColorText.NAVY);
        output.writeln(" [BRIGHT_RED]================================[/]", ColorText.NAVY);

        // Loop navigazione menu principale
        try{
            while(true) {
                output.writeln("\nMenu Principale:", ColorText.WHITE);
                output.writeln("[YELLOW]1)[/] Nuova Partita", ColorText.WHITE);
                output.writeln("[YELLOW]2)[/] Carica Partita", ColorText.WHITE);
                output.writeln("[YELLOW]3)[/] Classifica", ColorText.WHITE);
                output.writeln("[BRIGHT_YELLOW]4)[/] Esci", ColorText.WHITE);
                output.write(" \nSeleziona un'opzione: ", ColorText.WHITE);

                String choice = scanner.getInput();
                switch(choice) {
                case "1":
                    showNewGame();
                    break;
                case "2":
                    showLoadGame();
                    break;
                case "3":
                    showRanking();
                    break;
                case "4":
                    exit();
                    break;
                default:
                    output.writeln("Opzione non valida. Riprova.", ColorText.ERROR);
                    break;
                }
            }
        } catch (Exception e) {
            return;
        }
    }

    /**
     * Gestisce creazione nuova partita con validazione server user esistenza.
     * Verifica tramite API che username non esista, crea Engine via Factory
     * e avvia game loop con gestione interruzioni thread.
     */
    @Override
    public void showNewGame() {
        output.write("\nInserisci nome giocatore: ", ColorText.WHITE);
        String name = scanner.getInput();

        if (name == null || name.trim().isEmpty()) {
            errorHan.handleRecoverableError("Errore: Il nome giocatore non può essere vuoto.");
            return;
        }

        PoggioClientJersey gameClient = null;
        try {
            // Validazione esistenza user via API
            gameClient = new PoggioClientJersey();
            ApiClientResult result = gameClient.checkUserExists(name);
            gameClient.close();

            switch(result){
                case USER_NOT_FOUND:
                    output.writeln("Creazione nuova partita per " + name + "...", ColorText.GREEN);
                    try {
                        Engine engine = EngineFactory.createNewGame(
                            name, output, scanner, errorHan, new LoggerInput(errorHan)
                        );
                        engine.startGameLoop();
                        
                        if (Thread.currentThread().isInterrupted()) {
                            Thread.interrupted();
                        }
                    } catch (Exception ex) {
                        errorHan.handleFatalError("Errore o uscita: ", ex);
                    }
                    break;

                case SUCCESS_OK:
                    errorHan.handleRecoverableError("Errore: L'utente '" + name + "' esiste già. Prova a caricare una partita o scegli un nome diverso.");
                    break;

                case CONNECTION_ERROR:
                    errorHan.handleRecoverableError("Errore: Impossibile comunicare con il server per verificare l'utente.");
                    break;

                default:
                    errorHan.handleRecoverableError("Errore sconosciuto durante la comunicazione con il server (" + result + ").");
                    break;
            }
        } catch (Exception e) {
            if (e.getCause() instanceof InterruptedException || 
                Thread.currentThread().isInterrupted()) {
                Thread.interrupted();
            }
            errorHan.handleRecoverableError("Errore imprevisto durante la verifica utente: " + e.getMessage());
            if (gameClient != null) {
                try { gameClient.close(); } catch (Exception ce) { /* ignora errore chiusura */ }
            }
        }
    }

    /**
     * Gestisce caricamento/eliminazione salvataggi con interfaccia numerata.
     * Supporta syntax '!numero' per eliminazione con conferma utente.
     * Utilizza callback pattern per gestione successo/errore loading.
     */
    @Override
    public void showLoadGame() {
        List<String> saves = SaveGame.getSaveList();

        if (saves.isEmpty()) {
            output.writeln("\nNessun salvataggio disponibile.", ColorText.ORANGE);
             output.write("\nPremi Invio per continuare...", ColorText.WHITE);
             scanner.getInput();
            return;
        }

        // Rendering lista salvataggi numerata
        output.writeln(" \n=== SALVATAGGI DISPONIBILI ===", ColorText.YELLOW);
        for (int i = 0; i < saves.size(); i++) {
            output.writeln((i + 1) + ") " + saves.get(i), ColorText.WHITE);
        }

        output.write(" \nSeleziona il numero del salvataggio da caricare (es: 2)\n" +
                     "oppure inserisci '!' seguito dal numero per ELIMINARE (es: !2): ", ColorText.WHITE);
        String input = scanner.getInput().trim();

        try {
            if (input.startsWith("!")) {
                // Modalità eliminazione con conferma
                int selectedIndex = Integer.parseInt(input.substring(1)) - 1;

                if (selectedIndex >= 0 && selectedIndex < saves.size()) {
                    String saveName = saves.get(selectedIndex);
                    output.write("Sei sicuro di voler eliminare '" + saveName + "'? (s/N): ", ColorText.RED);
                    String confirm = scanner.getInput().trim().toLowerCase();

                    if (confirm.equals("s")) {
                        if (SaveGame.deleteSave(saveName, errorHan, true)) {
                            output.writeln("Salvataggio '" + saveName + "' eliminato con successo.", ColorText.GREEN);
                            return;
                        } else {
                            output.writeln("Eliminazione fallita per '" + saveName + "'. Controlla i log per dettagli.", ColorText.ERROR);
                        }
                    } else {
                        output.writeln("Eliminazione annullata.", ColorText.YELLOW);
                    }
                } else {
                    output.writeln("Numero di salvataggio non valido.", ColorText.ERROR);
                }
            } else {
                // Modalità caricamento con callback success/error
                int selectedIndex = Integer.parseInt(input) - 1;

                if (selectedIndex >= 0 && selectedIndex < saves.size()) {
                    String saveToLoad = saves.get(selectedIndex);
                    output.writeln("\nCaricamento di '" + saveToLoad + "'...", ColorText.CYAN);
                    
                    SaveGame.loadSave(
                        saveToLoad,
                        engine -> {
                            try {
                                engine.startGameLoop();
                                
                                if (Thread.currentThread().isInterrupted()) {
                                    Thread.interrupted();
                                }
                                
                            } catch (Exception ex) {
                                if (ex.getCause() instanceof InterruptedException || 
                                    Thread.currentThread().isInterrupted()) {
                                    Thread.interrupted();
                                } else {
                                    errorHan.handleRecoverableError("Errore durante il gioco: " + ex.getMessage());
                                }
                            }
                        },
                        error -> {
                            errorHan.handleRecoverableError("Errore durante il caricamento: " + error);
                        },
                        errorHan, scanner, output
                    );
                } else {
                    output.writeln("Numero di salvataggio non valido.", ColorText.ERROR);
                }
            }
        } catch (NumberFormatException e) {
            output.writeln("Input non valido. Inserisci un numero o '!numero'.", ColorText.ERROR);
        } catch (Exception e) {
             errorHan.handleRecoverableError("Errore imprevisto nel menu di caricamento: " + e.getMessage());
        }
    }

    /**
     * Mostra classifica online con opzione download log giocatori.
     * Recupera dati via API REST e formatta tabella con gestione errori.
     */
    @Override
    public void showRanking() {
        output.writeln("\n=== CLASSIFICA GIOCATORI ===", ColorText.YELLOW);
        output.writeln("Recupero dati dal server...", ColorText.CYAN);

        PoggioClientJersey gameClient = null;
        List<RankingEntryDTO> ranking = null;

        try {
            gameClient = new PoggioClientJersey();
            ranking = gameClient.getRanking();
            gameClient.close();
        } catch (Exception e) {
            errorHan.handleRecoverableError("Errore imprevisto durante comunicazione con server per classifica: " + e.getMessage());
            if (gameClient != null) {
                try { gameClient.close(); } catch(Exception ce) {/* ignora */}
            }
        }

        if (ranking == null) {
            output.writeln("\nErrore critico durante il recupero della classifica.", ColorText.ERROR);
        } else if (ranking.isEmpty()) {
            output.writeln("\nNessun dato disponibile per la classifica.", ColorText.ORANGE);
            output.writeln("(Potrebbe non esserci alcun punteggio registrato o esserci stato un problema temporaneo con il server).", ColorText.ORANGE);
        } else {
            displayRankingTable(ranking);
            offerLogDownloadOption(ranking);
        }

        output.write("\nPremi Invio per tornare al menu principale...", ColorText.WHITE);
        scanner.getInput();
    }

    /**
     * Renderizza tabella classifica formattata con headers e alignment.
     * 
     * @param ranking Lista entries classifica da visualizzare
     */
    private void displayRankingTable(List<RankingEntryDTO> ranking) {
        output.writeln(String.format("\n%-4s %-20s %-12s %-10s %s", "#", "Username", "Data", "Ora", "Punteggio"), ColorText.CYAN);
        output.writeln("-----------------------------------------------------------------", ColorText.CYAN);

        int position = 1;
        for (RankingEntryDTO entry : ranking) {
            String dateStr = (entry.getData() != null) ? entry.getData().toString() : "N/D";
            String timeStr = (entry.getOra() != null) ? entry.getOra().toString() : "N/D";
            String scoreStr = (entry.getPunteggio() != null) ? entry.getPunteggio().toString() : "N/A";

            output.writeln(String.format("%-4d %-20s %-12s %-10s %s",
                position++, entry.getUsername(), dateStr, timeStr, scoreStr
            ), ColorText.WHITE);
        }
        output.writeln("-----------------------------------------------------------------", ColorText.CYAN);
    }

    /**
     * Offre opzione download log con input validation.
     * 
     * @param ranking Lista players per selezione download
     */
    private void offerLogDownloadOption(List<RankingEntryDTO> ranking) {
        output.writeln("\n=== SCARICAMENTO LOG ===", ColorText.YELLOW);
        output.writeln("Vuoi scaricare il log di qualche giocatore? (s/n)", ColorText.WHITE);
        
        String response = scanner.getInput().trim().toLowerCase();
        
        if ("s".equals(response) || "si".equals(response) || "yes".equals(response) || "y".equals(response)) {
            showDownloadMenu(ranking);
        }
    }

    /**
     * Menu selezione player per download log con navigazione numerata.
     * 
     * @param ranking Lista players disponibili
     */
    private void showDownloadMenu(List<RankingEntryDTO> ranking) {
        while (true) {
            output.writeln("\n=== SELEZIONE GIOCATORE ===", ColorText.CYAN);
            output.writeln("Seleziona un giocatore (inserisci il numero di posizione):", ColorText.WHITE);
            
            // Mostra lista numerata
            for (int i = 0; i < ranking.size(); i++) {
                RankingEntryDTO entry = ranking.get(i);
                output.writeln(String.format("%d. %s", i + 1, entry.getUsername()), ColorText.YELLOW);
            }
            
            output.writeln("0. Torna alla classifica", ColorText.GRAY);
            output.write("\nScelta: ", ColorText.WHITE);
            
            String input = scanner.getInput().trim();
            
            if ("0".equals(input)) {
                break; // Torna al menu precedente
            }
            
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= ranking.size()) {
                    RankingEntryDTO selectedEntry = ranking.get(choice - 1);
                    downloadUserLog(selectedEntry.getUsername());
                } else {
                    output.writeln("Selezione non valida. Riprova.", ColorText.ERROR);
                }
            } catch (NumberFormatException e) {
                output.writeln("Inserisci un numero valido.", ColorText.ERROR);
            }
        }
    }

    /**
     * Esegue download log specifico user con gestione completa risultati API.
     * 
     * @param username Target user per download log
     */
    private void downloadUserLog(String username) {
        output.writeln("\nScaricamento log per: " + username, ColorText.CYAN);
        
        PoggioClientJersey gameClient = null;
        try {
            gameClient = new PoggioClientJersey();
            ApiClientResult result = gameClient.downloadLogFile(username);
            
            switch (result) {
                case SUCCESS_OK -> {
                    output.writeln("Log scaricato con successo!", ColorText.GREEN);
                    output.writeln("Salvato in: " + ResourceLoader.LOGS_DW_DIRECTORY.resolve(username + "_log.txt"), ColorText.YELLOW);
                }
                case USER_NOT_FOUND -> {
                    output.writeln("Utente non trovato sul server.", ColorText.ERROR);
                }
                case FILE_ERROR -> {
                    output.writeln("Errore durante il salvataggio del file.", ColorText.ERROR);
                }
                case CONNECTION_ERROR -> {
                    output.writeln("Errore di connessione al server.", ColorText.ERROR);
                }
                case UNAUTHORIZED -> {
                    output.writeln("Non autorizzato. Verifica configurazione server.", ColorText.ERROR);
                }
                default -> {
                    output.writeln("Errore sconosciuto: " + result, ColorText.ERROR);
                }
            }
            
        } catch (Exception e) {
            output.writeln("Errore imprevisto durante scaricamento:", ColorText.ERROR);
            output.writeln(e.getMessage(), ColorText.ERROR);
            errorHan.handleRecoverableError("Errore scaricamento log per " + username + ": " + e.getMessage());
        } finally {
            if (gameClient != null) {
                try {
                    gameClient.close();
                } catch (Exception e) {
                    // Ignora errori di chiusura
                }
            }
        }
        
        output.write("\nPremi Invio per continuare...", ColorText.WHITE);
        scanner.getInput();
    }

    /** Termina applicazione con messaggio farewell */
    @Override
    public void exit() {
        output.write("\nGrazie per aver giocato a PoggioAdventure!", ColorText.GREEN);
        Utils.exitApplication();
    }
}