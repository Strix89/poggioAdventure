package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.observers.OpenObserver;
import com.mycompany.poggioadventure.observers.InventoryObserver;
import com.mycompany.poggioadventure.observers.MoveObserver;
import com.mycompany.poggioadventure.observers.PushObserver;
import com.mycompany.poggioadventure.observers.PutObserver;
import com.mycompany.poggioadventure.observers.TalkObserver;
import com.mycompany.poggioadventure.observers.PickUpObserver;
import com.mycompany.poggioadventure.observers.LookAtObserver;
import com.mycompany.poggioadventure.observers.UseObserver;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.parser.Command;
import com.mycompany.poggioadventure.parser.CommandType;
import com.mycompany.poggioadventure.model.Room;
import java.util.ArrayList;
import java.util.List;
import com.mycompany.poggioadventure.core.abstracts.GameObservable;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.observers.GameObserver;

/** 
 * Implementazione concreta del motore di gioco per PoggioAdventure.
 * 
 * <p>Estende {@link GameDescription} fornendo la logica specifica del gioco
 * e implementa {@link GameObservable} per il sistema di notifiche basato su Observer.
 * 
 * <p><b>Responsabilità principali:</b>
 * <ul>
 *   <li>Configurazione comandi e alias supportati</li>
 *   <li>Gestione del ciclo di elaborazione comandi</li>
 *   <li>Coordinamento degli observer per azioni specifiche</li>
 *   <li>Generazione di output formattatoper GUI e CLI</li>
 * </ul>
 * 
 * <p>Utilizza un sistema di messaggistica centralizzato per raccogliere
 * le risposte degli observer e presentarle in modo coerente all'utente.
 */
public class PoggioAdventureDesc extends GameDescription implements GameObservable {
    
    /** Registry degli observer registrati per le notifiche */
    private final List<GameObserver> observer = new ArrayList<>();
    
    /** Buffer per messaggi di risposta dagli observer */
    private final List<String> messages = new ArrayList<>();

    /**
     * Inizializza il sistema di gioco con comandi, alias e observer.
     * Configura tutti i CommandType supportati e registra gli observer specifici.
     * 
     * @throws Exception se l'inizializzazione fallisce
     */
    @Override
    public void init() throws Exception {
        messages.clear();

        // Configurazione comandi di movimento
        Command nord = new Command(CommandType.NORD, "nord");
        nord.setAlias(new String[] { "n", "su", "sopra"});
        getCommands().add(nord);

        Command sud = new Command(CommandType.SOUTH, "sud");
        sud.setAlias(new String[] { "s", "giù", "giu", "sotto"});
        getCommands().add(sud);

        Command est = new Command(CommandType.EAST, "est");
        est.setAlias(new String[] { "e", "destra"});
        getCommands().add(est);

        Command ovest = new Command(CommandType.WEST, "ovest");
        ovest.setAlias(new String[] { "o", "sinistra"});
        getCommands().add(ovest);

        // Comandi di sistema
        Command end = new Command(CommandType.END, "end");
        end.setAlias(
                new String[] { "fine", "esci", "muori", "ammazzati", "ucciditi", "suicidati", "exit", "basta" });
        getCommands().add(end);

        // Comandi di interazione
        Command look = new Command(CommandType.LOOK_AT, "osserva");
        look.setAlias(new String[] { "guarda", "vedi", "trova", "cerca", "descrivi" });
        getCommands().add(look);

        Command pickup = new Command(CommandType.PICK_UP, "raccogli");
        pickup.setAlias(new String[] { "prendi" , "pick"});
        getCommands().add(pickup);

        Command open = new Command(CommandType.OPEN, "apri");
        open.setAlias(new String[] {"open"});
        getCommands().add(open);

        Command push = new Command(CommandType.PUSH, "premi");
        push.setAlias(new String[] { "spingi", "attiva", "push"});
        getCommands().add(push);

        Command use = new Command(CommandType.USE, "usa");
        use.setAlias(new String[] { "utilizza", "combina", "accendi", "attiva", "use", "activate" });
        getCommands().add(use);

        Command save = new Command(CommandType.SAVE, "salva");
        save.setAlias(new String[]{"save", "salva-gioco", "save-game"});
        getCommands().add(save);

        Command talk = new Command(CommandType.TALK, "parla");
        talk.setAlias(new String[]{"dialoga", "chiedi", "conversa", "ask"});
        getCommands().add(talk);

        Command put = new Command(CommandType.PUT, "metti");
        put.setAlias(new String[]{"metti", "inserisci", "put", "place", "posiziona", "monta"});
        getCommands().add(put);

        Command iventory = new Command(CommandType.INVENTORY, "inventario");
        iventory.setAlias(new String[] { "inv", "inventory", "i"});
        getCommands().add(iventory);
        
        // Registrazione observer specializzati per ogni tipo di azione
        registerObservers();
    }

    /**
     * Registra tutti gli observer specializzati per la gestione delle azioni.
     * Ogni observer gestisce un tipo specifico di comando del giocatore.
     */
    private void registerObservers() {
        attach(new MoveObserver());
        attach(new InventoryObserver());
        attach(new PushObserver());
        attach(new LookAtObserver());
        attach(new PickUpObserver());
        attach(new OpenObserver());
        attach(new UseObserver());
        attach(new TalkObserver());
        attach(new PutObserver());
    }

    /**
     * Elabora una lista di comandi parsati in sequenza.
     * Notifica gli observer e gestisce l'output di risposta e i cambiamenti di stanza.
     * 
     * @param list Lista di comandi parsati da elaborare
     * @param gameContext Contesto di gioco con handler I/O
     */
    @Override
    public void nextMove(List<ParserOutput> list, GameContext gameContext) {
        for (ParserOutput p : list) {
            if (p.getCommand() == null) {
                gameContext.getOutputHandler().writeln("Non ho capito cosa devo fare! Prova con un altro comando.", ColorText.RED);
                continue;
            }
            
            Room currentRoomBefore = getCurrentRoom();
            notifyObservers(p, gameContext);
            
            // Verifica se c'è stato un cambio di stanza
            boolean hasMovedRoom = !currentRoomBefore.equals(getCurrentRoom()) && getCurrentRoom() != null;
            
            // Output dei messaggi raccolti dagli observer
            flushObserverMessages(gameContext);
            
            // Descrizione della nuova stanza se c'è stato movimento
            if (hasMovedRoom) {
                displayRoomInfo(gameContext);
            }
        }
    }

    /**
     * Invia all'output tutti i messaggi raccolti dagli observer e svuota il buffer.
     */
    private void flushObserverMessages(GameContext gameContext) {
        if (!messages.isEmpty()) {
            for (String message : messages) {
                if (!message.trim().isEmpty()) {
                    gameContext.getOutputHandler().writeln(message, ColorText.WHITE);
                }
            }
            messages.clear();
        }
    }

    /**
     * Visualizza le informazioni della stanza corrente dopo un movimento.
     */
    private void displayRoomInfo(GameContext gameContext) {
        gameContext.getOutputHandler().writeln("\n" + getCurrentRoom().getName(), ColorText.YELLOW);
        gameContext.getOutputHandler().writeln("================================================", ColorText.WHITE);
        gameContext.getOutputHandler().writeln(getCurrentRoom().getDescription(), ColorText.WHITE);
    }

    @Override
    public void attach(GameObserver o) {
        if (!observer.contains(o)) {
            observer.add(o);
        }
    }

    @Override
    public void detach(GameObserver o) {
        observer.remove(o);
    }

    /**
     * Notifica tutti gli observer registrati dell'azione corrente.
     * Raccoglie le risposte nel buffer dei messaggi per output successivo.
     */
    @Override
    public void notifyObservers(ParserOutput parserOutput, GameContext gameContext) {
        for (GameObserver o : observer) {
            messages.add(o.update(this, parserOutput, gameContext));
        }
    }

    @Override
    public String getGUIWelcomeMsg() {
        return ""
                + "====================================================================================\n"
                + "Sei una matricola in cerca di ammissione a questo prestigioso collegio,\n"
                + "dove solo con l'aiuto di Dio e dei santi potrai uscirne residente, o forse no...\n"
                + "====================================================================================";
    }
    
    @Override
    public String getCLIWelcomeMsg() {
        return ""
                + "====================================================================================\n"
                + "Sei una matricola in cerca di ammissione a questo prestigioso collegio,\n"
                + "dove solo con l'aiuto di Dio e dei santi potrai uscirne residente, o forse no...\n"
                + "====================================================================================";
    }

    @Override
    public String getGUIGameVersion() {
        return 
        "==============================================================================\n" +
        "\t                                    PoggioAdventure .v0.1 - 2024-2025           \n" +
        "\t                                              developed by:                      \n" +
        "\t                                   Strix89 | MikeRvsso | Elia-Valenza26         \n" +
        "==============================================================================\n";
    } 
    
    @Override
    public String getCLIGameVersion() {
        return 
        "==========================================================================\n" +
        "                     PoggioAdventure .v0.1 - 2024-2025                 \n" +
        "                               developed by:                           \n" +
        "                    Strix89 | MikeRvsso | Elia-Valenza26               \n" +
        "==========================================================================\n";
    } 
}
