package com.mycompany.poggioadventure.ui.cli;

// Import utility e classi di base
import com.mycompany.poggioadventure.core.utils.Utils; // Utility generiche (es. per uscire)
import com.mycompany.poggioadventure.ui.ColorText; // Utility per output colorato su console

// Import classi di persistenza e stato gioco
import com.mycompany.poggioadventure.persistence.SaveGame; // Gestore salvataggi (statico)
import com.mycompany.poggioadventure.core.Engine; // Motore principale del gioco
import com.mycompany.poggioadventure.core.utils.ApiClientResult;
import com.mycompany.poggioadventure.core.utils.EngineFactory; // Factory per creare istanze di Engine
import com.mycompany.poggioadventure.core.abstracts.MenuManager; // Interfaccia che questa classe implementa
import com.mycompany.poggioadventure.core.utils.PoggioClientJersey; // Client per l'API del server
import com.mycompany.poggioadventure.persistence.LoggerInput; // Gestore/Logger per input/eventi
import com.mycompany.poggioadventure.persistence.RankingEntryDTO; // DTO per i dati della classifica

// Import interfacce/classi UI e gestione errori
import com.mycompany.poggioadventure.ui.InputHandler; // Interfaccia per gestione input
import com.mycompany.poggioadventure.ui.OutputHandler; // Interfaccia per gestione output
import com.mycompany.poggioadventure.ui.ErrorHandler; // Interfaccia per gestione errori

// Import standard Java
import java.util.List; // Per gestire liste (es. lista salvataggi, classifica)

/**
 * Implementazione per interfaccia a riga di comando (Command Line Interface - CLI)
 * del gestore dei menu del gioco ({@link MenuManager}).
 * Gestisce tutte le interazioni utente relative ai menu tramite la console,
 * presentando opzioni testuali e leggendo input da tastiera.
 * Include la gestione del menu principale, la creazione di una nuova partita,
 * il caricamento/eliminazione di partite salvate e la visualizzazione della classifica.
 *
 * @author Strix89 // Autore originale
 */
public class CLIMenu implements MenuManager {

    /**
     * Handler responsabile della scrittura dell'output sulla console.
     * Utilizza l'implementazione specifica {@link CLIOutputHandler}. Final perché inizializzato nel costruttore.
     */
    private final OutputHandler output;

    /**
     * Handler responsabile della lettura dell'input utente dalla console.
     * Utilizza l'implementazione specifica {@link CLIInputHandler}. Final perché inizializzato nel costruttore.
     */
    private final InputHandler scanner;

    /**
     * Handler responsabile della gestione e visualizzazione degli errori nella console.
     * Utilizza l'implementazione specifica {@link CLIErrorHandler}. Final perché inizializzato nel costruttore.
     */
    private final ErrorHandler errorHan;

    /**
     * Costruttore di CLIMenu.
     * Inizializza gli handler finali per l'input, l'output e la gestione degli errori
     * con le implementazioni concrete specifiche per l'ambiente a riga di comando.
     */
    public CLIMenu() {
        // Istanzia gli handler specifici per la CLI
        this.output = new CLIOutputHandler();
        this.scanner = new CLIInputHandler();
        this.errorHan = new CLIErrorHandler();
    }

    /**
     * Mostra il menu principale del gioco all'utente sulla console.
     * Presenta le opzioni disponibili: Nuova Partita, Carica Partita, Classifica, Esci.
     * Il menu rimane attivo (ciclo `while(true)`) finché l'utente non sceglie
     * l'opzione per uscire (che chiama {@link #exit()}).
     */
    @Override
    public void showMainMenu() {
        // Stampa il titolo del gioco una volta all'avvio del menu
        output.writeln("\n===== POGGIO ADVENTURE =====", ColorText.NAVY);

        // Loop infinito per mostrare ripetutamente il menu principale
        while(true) {
            // Stampa le opzioni del menu
            output.writeln("\nMenu Principale:", ColorText.WHITE);
            output.writeln("1) Nuova Partita", ColorText.WHITE);
            output.writeln("2) Carica Partita", ColorText.WHITE);
            output.writeln("3) Classifica", ColorText.WHITE);
            output.writeln("4) Esci", ColorText.WHITE);
            output.write(" \nSeleziona un'opzione: ", ColorText.WHITE); // Prompt per l'utente

            // Legge la scelta dell'utente
            String choice = scanner.getInput();

            // Esegue l'azione corrispondente alla scelta
            switch(choice) {
                case "1": // Nuova Partita
                    showNewGame();
                    break; // Torna al loop del menu dopo che showNewGame termina
                case "2": // Carica Partita
                    showLoadGame();
                    break; // Torna al loop del menu dopo che showLoadGame termina
                case "3": // Classifica
                    showRanking();
                    break; // Torna al loop del menu dopo che showRanking termina
                case "4": // Esci
                    exit(); // Chiama il metodo per terminare l'applicazione
                    // Nota: exit() probabilmente non ritorna, quindi il loop termina de facto.
                    break;
                default: // Scelta non valida
                    output.writeln("Opzione non valida. Riprova.", ColorText.ERROR);
                    // Il loop continua, mostrando di nuovo il menu
                    break;
            }
        }
    }

    /**
     * Gestisce il flusso per iniziare una nuova partita.
     * Chiede all'utente di inserire un nome giocatore, verifica tramite API
     * che l'utente non esista già sul server, e se non esiste, crea una nuova
     * istanza del gioco tramite {@link EngineFactory} e avvia il loop principale del gioco.
     * Gestisce gli errori di comunicazione con il server o il caso in cui l'utente esista già.
     */
    @Override
    public void showNewGame() {
        output.write("\nInserisci nome giocatore: ", ColorText.WHITE); // Prompt per il nome
        String name = scanner.getInput(); // Legge il nome inserito

        // Validazione base: il nome non dovrebbe essere vuoto
        if (name == null || name.trim().isEmpty()) {
            errorHan.handleRecoverableError("Errore: Il nome giocatore non può essere vuoto.");
            return; // Torna al menu precedente (main menu)
        }

        PoggioClientJersey gameClient = null; // Dichiarato qui per poterlo chiudere nel blocco switch/finally implicito
        try {
            // Verifica se l'utente esiste già sul server
            gameClient = new PoggioClientJersey();
            ApiClientResult result = gameClient.checkUserExists(name);
            gameClient.close(); // Chiude il client subito dopo la chiamata

            // Gestisce l'esito della verifica
            switch(result){
                case USER_NOT_FOUND:
                    // L'utente NON esiste sul server: OK per creare nuova partita
                    output.writeln("Creazione nuova partita per " + name + "...", ColorText.GREEN);
                    try {
                        // Crea una nuova istanza dell'Engine tramite la factory
                        Engine engine = EngineFactory.createNewGame(
                            name,           // Nome giocatore
                            output,         // Handler output
                            scanner,        // Handler input
                            errorHan,       // Handler errori
                            new LoggerInput(errorHan) // Nuovo logger per la partita
                        );
                        // Avvia il loop principale del gioco (questa chiamata bloccherà finché la partita non finisce)
                        engine.startGameLoop();
                    } catch (Exception ex) {
                        // Gestisce errori critici durante la creazione dell'engine o l'avvio del gioco
                        errorHan.handleFatalError("Errore critico durante l'inizializzazione della nuova partita", ex);
                        // Potrebbe essere opportuno uscire dall'applicazione qui
                        // Utils.exitApplication(Utils.EXIT_CODE_ERROR);
                    }
                    break; // Fine caso USER_NOT_FOUND

                case SUCCESS_OK:
                    // L'utente esiste GIA' sul server: non si può creare una nuova partita con questo nome
                    errorHan.handleRecoverableError("Errore: L'utente '" + name + "' esiste già. Prova a caricare una partita o scegli un nome diverso.");
                    break; // Torna al menu principale

                case CONNECTION_ERROR:
                    // Errore di connessione durante la verifica
                    errorHan.handleRecoverableError("Errore: Impossibile comunicare con il server per verificare l'utente.");
                    break; // Torna al menu principale

                default:
                    // Altro errore (es. UNKNOWN_ERROR, UNAUTHORIZED) restituito dal client
                    errorHan.handleRecoverableError("Errore sconosciuto durante la comunicazione con il server (" + result + ").");
                    break; // Torna al menu principale
            }
        } catch (Exception e) {
            // Cattura eccezioni impreviste durante l'interazione con il client API
            errorHan.handleRecoverableError("Errore imprevisto durante la verifica utente: " + e.getMessage());
            if (gameClient != null) {
                try { gameClient.close(); } catch (Exception ce) { /* ignora errore chiusura */ }
            }
        }
    }

    /**
     * Mostra la lista dei salvataggi disponibili e permette all'utente di
     * selezionarne uno da caricare o da eliminare.
     * Utilizza {@link SaveGame} per ottenere la lista e per eseguire le operazioni
     * di caricamento o eliminazione.
     */
    @Override
    public void showLoadGame() {
        // Ottiene la lista dei nomi dei salvataggi (già ordinati dal più recente)
        List<String> saves = SaveGame.getSaveList();

        // Controlla se ci sono salvataggi
        if (saves.isEmpty()) {
            output.writeln("\nNessun salvataggio disponibile.", ColorText.ORANGE);
            // Breve pausa prima di tornare al menu principale
             output.write("\nPremi Invio per continuare...", ColorText.WHITE);
             scanner.getInput();
            return; // Torna al menu principale
        }

        // Mostra l'elenco numerato dei salvataggi
        output.writeln(" \n=== SALVATAGGI DISPONIBILI ===", ColorText.YELLOW);
        for (int i = 0; i < saves.size(); i++) {
            // Mostra numero indice (partendo da 1) e nome del salvataggio
            output.writeln((i + 1) + ") " + saves.get(i), ColorText.WHITE);
        }

        // Chiede all'utente di scegliere un'azione (caricare o eliminare)
        output.write(" \nSeleziona il numero del salvataggio da caricare (es: 2)\n" +
                     "oppure inserisci '!' seguito dal numero per ELIMINARE (es: !2): ", ColorText.WHITE);
        String input = scanner.getInput().trim(); // Legge l'input e rimuove spazi extra

        try {
            if (input.startsWith("!")) {
                // --- Modalità Eliminazione ---
                // Estrae il numero dopo il '!'
                int selectedIndex = Integer.parseInt(input.substring(1)) - 1; // Converte in indice base 0

                // Verifica che l'indice sia valido
                if (selectedIndex >= 0 && selectedIndex < saves.size()) {
                    String saveName = saves.get(selectedIndex); // Ottiene il nome del salvataggio selezionato
                    output.write("Sei sicuro di voler eliminare '" + saveName + "'? (s/N): ", ColorText.RED);
                    String confirm = scanner.getInput().trim().toLowerCase();

                    if (confirm.equals("s")) {
                        // Chiama il metodo di eliminazione in SaveGame
                        if (SaveGame.deleteSave(saveName, errorHan)) {
                            output.writeln("Salvataggio '" + saveName + "' eliminato con successo.", ColorText.GREEN);
                            // Ricarica e mostra di nuovo la lista aggiornata dei salvataggi (ricorsione)
                            return;
                        } else {
                            // deleteSave ha restituito false (errore durante eliminazione)
                            // l'errore specifico dovrebbe essere stato già loggato da deleteSave tramite errorHan
                            output.writeln("Eliminazione fallita per '" + saveName + "'. Controlla i log per dettagli.", ColorText.ERROR);
                        }
                    } else {
                        output.writeln("Eliminazione annullata.", ColorText.YELLOW);
                    }
                } else {
                    // L'indice inserito non è valido
                    output.writeln("Numero di salvataggio non valido.", ColorText.ERROR);
                }
            } else {
                // --- Modalità Caricamento ---
                // Converte l'input numerico in indice base 0
                int selectedIndex = Integer.parseInt(input) - 1;

                // Verifica che l'indice sia valido
                if (selectedIndex >= 0 && selectedIndex < saves.size()) {
                    String saveToLoad = saves.get(selectedIndex);
                    output.writeln("\nCaricamento di '" + saveToLoad + "'...", ColorText.CYAN);
                    // Chiama il metodo di caricamento in SaveGame, passando i callback
                    SaveGame.loadSave(
                        saveToLoad, // Nome del salvataggio da caricare
                        // Callback onSuccess: eseguito se il caricamento riesce
                        engine -> {
                            output.writeln("Caricamento completato!", ColorText.GREEN);
                            engine.startGameLoop(); // Avvia il loop del gioco con l'engine caricato
                        },
                        // Callback onError: eseguito se il caricamento fallisce
                        error -> {
                            // Mostra il messaggio di errore fornito da loadSave
                            errorHan.handleRecoverableError("Errore durante il caricamento: " + error);
                        },
                        // Passa gli handler necessari per ricostruire l'engine
                        errorHan, // Usa l'istanza corrente di ErrorHandler
                        scanner,
                        output
                    );
                } else {
                    // L'indice inserito non è valido
                    output.writeln("Numero di salvataggio non valido.", ColorText.ERROR);
                }
            }
        } catch (NumberFormatException e) {
            // L'input non era un numero valido (né un numero preceduto da '!')
            output.writeln("Input non valido. Inserisci un numero o '!numero'.", ColorText.ERROR);
        } catch (Exception e) {
             // Cattura altre eccezioni impreviste durante il processo di load/delete
             errorHan.handleRecoverableError("Errore imprevisto nel menu di caricamento: " + e.getMessage());
        }
    }

    /**
     * Mostra la classifica dei giocatori recuperandola dal server tramite
     * {@link PoggioClientJersey}. Formatta e stampa la classifica sulla console.
     * Attende un input dall'utente prima di tornare al menu principale.
     */
    @Override
    public void showRanking() {
        output.writeln("\n=== CLASSIFICA GIOCATORI ===", ColorText.YELLOW);
        output.writeln("Recupero dati dal server...", ColorText.CYAN);

        PoggioClientJersey gameClient = null; // Dichiarato fuori per chiusura in finally (implicito qui)
        List<RankingEntryDTO> ranking = null; // Inizializza a null

        try {
            // --- Chiama il client per ottenere la classifica ---
            gameClient = new PoggioClientJersey();
            ranking = gameClient.getRanking(); // Questo metodo è progettato per restituire lista vuota in caso di errore, non lanciare eccezioni qui
            gameClient.close(); // Chiude il client
        } catch (Exception e) {
            // Cattura eccezioni impreviste durante creazione/chiusura client o (meno probabile) chiamata getRanking
             errorHan.handleRecoverableError("Errore imprevisto durante comunicazione con server per classifica: " + e.getMessage());
            if (gameClient != null) {
                try { gameClient.close(); } catch(Exception ce) {/* ignora */}
            }
            // Lascia ranking a null per indicare errore grave
        }


        // --- Gestisci e Stampa il risultato ---
        if (ranking == null) {
             // Errore grave non gestito dal client (es. eccezione imprevista sopra)
              output.writeln("\nErrore critico durante il recupero della classifica.", ColorText.ERROR);
        } else if (ranking.isEmpty()) {
            // Lista vuota: può significare "nessun punteggio" oppure errore recuperabile gestito dal client getRanking.
            output.writeln("\nNessun dato disponibile per la classifica.", ColorText.ORANGE);
            output.writeln("(Potrebbe non esserci alcun punteggio registrato o esserci stato un problema temporaneo con il server).", ColorText.ORANGE);
        } else {
            // La classifica contiene dati: stampala formattata
            // Intestazione della tabella
            output.writeln(String.format("\n%-4s %-20s %-12s %-10s %s", "#", "Username", "Data", "Ora", "Punteggio"), ColorText.CYAN);
            output.writeln("-----------------------------------------------------------------", ColorText.CYAN);

            // Itera sulla lista ricevuta e stampa ogni voce
            int position = 1; // Contatore per la posizione in classifica
            for (RankingEntryDTO entry : ranking) {
                // Gestione sicura di valori potenzialmente nulli dal DTO
                String dateStr = (entry.getData() != null) ? entry.getData().toString() : "N/D";
                String timeStr = (entry.getOra() != null) ? entry.getOra().toString() : "N/D";
                String scoreStr = (entry.getPunteggio() != null) ? entry.getPunteggio().toString() : "N/A";

                // Stampa la riga formattata
                output.writeln(String.format("%-4d %-20s %-12s %-10s %s",
                    position++,          // Posizione
                    entry.getUsername(), // Username
                    dateStr,             // Data
                    timeStr,             // Ora
                    scoreStr             // Punteggio
                ), ColorText.WHITE);
            }
            // Riga di chiusura tabella
            output.writeln("-----------------------------------------------------------------", ColorText.CYAN);
        }

        // Pausa: attende che l'utente prema Invio per tornare al menu principale
        output.write("\nPremi Invio per tornare al menu principale...", ColorText.WHITE);
        scanner.getInput(); // Consuma l'input (attende Invio)
    }

    /**
     * Termina l'applicazione PoggioAdventure.
     * Delega l'operazione alla utility {@link Utils#exitApplication()}.
     */
    @Override
    public void exit() {
        output.writeln("\nGrazie per aver giocato a PoggioAdventure! Uscita...", ColorText.GREEN);
        Utils.exitApplication(); // Chiama il metodo statico per uscire (probabilmente fa System.exit)
    }
}