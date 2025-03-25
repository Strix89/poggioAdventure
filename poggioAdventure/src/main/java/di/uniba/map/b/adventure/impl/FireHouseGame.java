package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.GameDescription;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.Command;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.type.Room;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import di.uniba.map.b.adventure.GameObservable;
import di.uniba.map.b.adventure.GameObserver;
import di.uniba.map.b.adventure.GameMap; // Importa la classe GameMap

/**
 * La classe FireHouseGame rappresenta il gioco, estende GameDescription.
 * La gestione della mappa e dei collegamenti tra stanze è delegata a GameMap.
 */
public class FireHouseGame extends GameDescription implements GameObservable {

    private final List<GameObserver> observer = new ArrayList<>();
    private ParserOutput parserOutput;
    private final List<String> messages = new ArrayList<>();

    // Aggiungi un'istanza di GameMap per gestire le stanze e i loro collegamenti
    private GameMap gameMap;

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
        gameMap = new GameMap(); // Crea un'istanza della GameMap
        gameMap.addRoomsToGameDescription(); // Aggiungi tutte le stanze alla mappa del gioco

        // Comandi del gioco
        Command nord = new Command(CommandType.NORD, "nord");
        nord.setAlias(new String[] { "n", "N", "Nord", "NORD" });
        getCommands().add(nord);

        Command iventory = new Command(CommandType.INVENTORY, "inventario");
        iventory.setAlias(new String[] { "inv" });
        getCommands().add(iventory);

        Command sud = new Command(CommandType.SOUTH, "sud");
        sud.setAlias(new String[] { "s", "S", "Sud", "SUD" });
        getCommands().add(sud);

        Command est = new Command(CommandType.EAST, "est");
        est.setAlias(new String[] { "e", "E", "Est", "EST" });
        getCommands().add(est);

        Command ovest = new Command(CommandType.WEST, "ovest");
        ovest.setAlias(new String[] { "o", "O", "Ovest", "OVEST" });
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
        save.setAlias(new String[] { "salvataggio" });
        getCommands().add(save);

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

        // Inizializza la stanza iniziale del gioco (ora la stanza iniziale viene
        // recuperata dalla GameMap)
        setCurrentRoom(gameMap.getStartingRoom()); // Imposta la stanza iniziale come la prima stanza del primo piano
    }

    /**
     * Metodo che gestisce il comando successivo
     * 
     * @param p
     * @param out
     */
    @Override
    public void nextMove(ParserOutput p, PrintStream out) {
        parserOutput = p;
        messages.clear();

        // Controllo se il comando è valido
        if (p.getCommand() == null) {
            out.println("Non ho capito cosa devo fare! Prova con un altro comando.");
        } else {
            // Salva la stanza corrente prima del movimento
            Room cr = getCurrentRoom();

            // Notifica gli osservatori, incluso il MoveObserver
            notifyObservers();

            // Verifica se la stanza è cambiata (se il giocatore si è spostato)
            boolean move = !cr.equals(getCurrentRoom()) && getCurrentRoom() != null;

            // Se ci sono messaggi generati dagli osservatori, li stampiamo
            if (!messages.isEmpty()) {
                for (String m : messages) {
                    if (m.length() > 0) {
                        out.println(m);
                    }
                }
            }

            // Se c'è stato un movimento, aggiorna e mostra la nuova stanza
            if (move) {
                out.println(getCurrentRoom().getName()); // Nome della stanza corrente
                out.println("================================================");
                out.println(getCurrentRoom().getDescription()); // Descrizione della stanza corrente
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
     */
    @Override
    public void notifyObservers() {
        for (GameObserver o : observer) {
            messages.add(o.update(this, parserOutput));
        }
    }

    @Override
    public String getWelcomeMsg() {
        return ""
                + "====================================================================\n"
                + "* BENVENUTO NEL COLLEGIO TECNOMAGICO DI SAN JOSE MARIA *\n"
                + "====================================================================\n"
                + "\n"
                + "Sei una matricola in cerca di ammissione a questo prestigioso collegio,\n"
                + "dove solo i più brillanti superano le prove iniziatiche.\n"
                + "\n"
                + "Per essere ammesso dovrai affrontare 3 prove a difficoltà crescente:\n"
                + "1. [LOGICA] Risolvi l'enigma della Penna Perduta\n"
                + "2. [ELETTRONICA] Assembla il PC maledetto\n"
                + "3. [ROBOTICA] Domina i robottini impazziti\n"
                + "\n"
                + "Solo allora ti sarà rivelata la PROVA BONUS finale contro Cruel...\n"
                + "\n"
                + "\n"
                + "I tuoi mentori:\n"
                + "- Guido, il tutor saggio ma distratto\n"
                + "- Dottor Burdo, l'eccentrico napoletano\n"
                + "- Cupulino, il guardiano delle prove\n"
                + "\n"
                + "ATTENZIONE: Il tempo scorre inesorabile per ogni prova!\n"
                + "La prima inizia tra 10 secondi... Preparati!\n"
                + "\n"
                + "Premi INVIO quando sei pronto ad affrontare il tuo destino.\n"
                + "====================================================================";
    }
}
