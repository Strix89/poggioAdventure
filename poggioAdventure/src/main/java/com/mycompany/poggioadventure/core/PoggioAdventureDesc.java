package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.observers.OpenObserver;
import com.mycompany.poggioadventure.observers.InventoryObserver;
import com.mycompany.poggioadventure.observers.MoveObserver;
import com.mycompany.poggioadventure.observers.PushObserver;
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
 * ATTENZIONE: La descrizione del gioco è fatta in modo che qualsiasi gioco
 * debba estendere la classe GameDescription. L'Engine è fatto in modo che possa
 * eseguire qualsiasi gioco che estende GameDescription, in questo modo si
 * possono creare più giochi utilizzando lo stesso Engine.
 *
 * Diverse migliorie possono essere applicate: - la descrizione del gioco
 * potrebbe essere caricate da file o da DBMS in modo da non modificare il
 * codice sorgente - l'utilizzo di file e DBMS non è semplice poiché all'interno
 * del file o del DBMS dovrebbe anche essere codificata la logica del gioco
 * (nextMove) oltre alla descrizione di stanze, oggetti, ecc...
 * 
 * 
 * La classe FireHouseGame estende GameDescription che è una classe astratta che contiene la struttura base del gioco
 * e implementa l'interfaccia GameObservable che permette di notificare gli observer quando avviene un'azione.
 *
 * @author pierpaolo & Strix89
 */
public class PoggioAdventureDesc extends GameDescription implements GameObservable {
    
    private final List<GameObserver> observer = new ArrayList<>();
    private final List<String> messages = new ArrayList<>();

    /**
     * Metodo Init : inizializza il gioco
     * Questo metodo viene chiamato per inizializzare il gioco.
     * Vengono definite le stanze, gli oggetti e i comandi che il giocatore può
     * usare.
     */
    @Override
    public void init() throws Exception {
        messages.clear();
        // Inizializza la mappa del gioco
        this.getGameMap().addElementsToGameDescription(); // Aggiungi tutte le stanze alla mappa del gioco

        // Comandi del gioco
        Command nord = new Command(CommandType.NORD, "nord");
        nord.setAlias(new String[] { "n", "N", "Nord", "NORD", "Su", "su", "sù", "sù", "sopra", "Sopra"});
        getCommands().add(nord);

        Command iventory = new Command(CommandType.INVENTORY, "inventario");
        iventory.setAlias(new String[] { "inv" });
        getCommands().add(iventory);

        Command sud = new Command(CommandType.SOUTH, "sud");
        sud.setAlias(new String[] { "s", "S", "Sud", "SUD", "Giù", "giu", "giù", "giù", "sotto", "Sotto"});
        getCommands().add(sud);

        Command est = new Command(CommandType.EAST, "est");
        est.setAlias(new String[] { "e", "E", "Est", "EST", "Destra", "destra"});
        getCommands().add(est);

        Command ovest = new Command(CommandType.WEST, "ovest");
        ovest.setAlias(new String[] { "o", "O", "Ovest", "OVEST", "Sinistra", "sinistra"});
        getCommands().add(ovest);

        Command end = new Command(CommandType.END, "end");
        end.setAlias(
                new String[] { "end", "fine", "esci", "muori", "ammazzati", "ucciditi", "suicidati", "exit", "basta" });
        getCommands().add(end);

        Command look = new Command(CommandType.LOOK_AT, "osserva");
        look.setAlias(new String[] { "guarda", "vedi", "trova", "cerca", "descrivi" });
        getCommands().add(look);

        Command pickup = new Command(CommandType.PICK_UP, "raccogli");
        pickup.setAlias(new String[] { "prendi" });
        getCommands().add(pickup);

        Command open = new Command(CommandType.OPEN, "apri");
        open.setAlias(new String[] {});
        getCommands().add(open);

        Command push = new Command(CommandType.PUSH, "premi");
        push.setAlias(new String[] { "spingi", "attiva" });
        getCommands().add(push);

        Command use = new Command(CommandType.USE, "usa");
        use.setAlias(new String[] { "utilizza", "combina" });
        getCommands().add(use);

        Command save = new Command(CommandType.SAVE, "salva");
        save.setAlias(new String[]{"salva"});
        getCommands().add(save);

        Command talk = new Command(CommandType.TALK, "parla");
        talk.setAlias(new String[]{"dialoga", "chiedi", "conversa"});
        getCommands().add(talk);
        
        //Observer
        /*
         * Viene utilizzato GameObserver per notificare gli observer quando avviene un'azione.
         * I vari observer sottostanti creano un'istanza di GameObserver e monitorano le azioni del giocatore
         * 
         */
        GameObserver moveObserver = new MoveObserver(); // si uccuperà di gestire il movimento del giocatore
        this.attach(moveObserver);
        GameObserver invObserver = new InventoryObserver(); // si occuperà di gestire l'inventario del giocatore
        this.attach(invObserver);
        GameObserver pushObserver = new PushObserver(); // si occuperà di gestire l'azione di premere
        this.attach(pushObserver);
        GameObserver lookatObserver = new LookAtObserver(); // si occuperà di gestire l'azione di osservare
        this.attach(lookatObserver);
        GameObserver pickupObserver = new PickUpObserver(); // si occuperà di gestire l'azione di raccogliere
        this.attach(pickupObserver);
        GameObserver openObserver = new OpenObserver(); // si occuperà di gestire l'azione di aprire
        this.attach(openObserver);
        GameObserver useObserver = new UseObserver(); // si occuperà di gestire l'azione di usare
        this.attach(useObserver);
        GameObserver talkObserver = new TalkObserver();
        this.attach(talkObserver);

        // Inizializza la stanza iniziale del gioco (ora la stanza iniziale viene
        // recuperata dalla GameMap)
        setCurrentRoom(this.getGameMap().getStartingRoom()); // Imposta la stanza iniziale come la prima stanza del primo piano
    }

    /**
     * Metodo che gestisce il comando successivo
     * 
     * @param out
     */
    @Override
    public void nextMove(List<ParserOutput> list, GameContext gameContext) {
        for (ParserOutput p : list) {
            if (p.getCommand() == null) {
                gameContext.getOutputHandler().writeln("Non ho capito cosa devo fare! Prova con un altro comando.", ColorText.RED);
                continue;
            }
            Room cr = getCurrentRoom();
            notifyObservers(p, gameContext);
            boolean move = !cr.equals(getCurrentRoom()) && getCurrentRoom() != null;
            if (!messages.isEmpty()) {
                for (String m : messages) {
                    if (!m.trim().isEmpty()) {
                        gameContext.getOutputHandler().writeln(m, ColorText.WHITE);
                    }
                }
                messages.clear();
            }
            if (move) {
                gameContext.getOutputHandler().writeln("\n" + getCurrentRoom().getName(), ColorText.YELLOW);
                gameContext.getOutputHandler().writeln("================================================", ColorText.WHITE);
                gameContext.getOutputHandler().writeln(getCurrentRoom().getDescription(), ColorText.WHITE);
            }
        }
    }

    /**
     * Metodo che permette di aggiungere un observer
     * 
     * @param o
     */
    @Override
    public void attach(GameObserver o) {
        if (!observer.contains(o)) {
            observer.add(o);
        }
    }

    /**
     * Metodo che permette di rimuovere un observer
     * 
     * @param o
     */
    @Override
    public void detach(GameObserver o) {
        observer.remove(o);
    }

    /**
     * Metodo che permette di notificare gli observer
     * @param output
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
                + "==============================================================================\n"
                + "                             BENVENUTO NEL COLLEGIO TECNOMAGICO DI SAN JOSE MARIA \n"
                + "==============================================================================\n"
                + "Sei una matricola in cerca di ammissione a questo prestigioso collegio,\n"
                + "dove solo i più brillanti superano le prove.\n"
                + "==============================================================================";
    }
    
    @Override
    public String getCLIWelcomeMsg() {
        return ""
                + "========================================================================\n"
                + "       BENVENUTO NEL COLLEGIO TECNOMAGICO DI SAN JOSE MARIA \n"
                + "========================================================================\n"
                + "Sei una matricola in cerca di ammissione a questo prestigioso collegio,\n"
                + "dove solo i più brillanti superano le prove.\n"
                + "========================================================================";
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
