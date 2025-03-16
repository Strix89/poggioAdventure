/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.GameDescription;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.AdvObjectContainer;
import di.uniba.map.b.adventure.type.Command;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.type.Room;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import di.uniba.map.b.adventure.GameObservable;
import di.uniba.map.b.adventure.GameObserver;

/**
 * ATTENZIONE: La descrizione del gioco è fatta in modo che qualsiasi gioco
 * debba estendere la classe GameDescription. L'Engine è fatto in modo che possa
 * eseguire qualsiasi gioco che estende GameDescription, in questo modo si
 * possono creare più gioci utilizzando lo stesso Engine.
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
 * @author pierpaolo
 */
public class FireHouseGame extends GameDescription implements GameObservable {

    private final List<GameObserver> observer = new ArrayList<>();

    private ParserOutput parserOutput;

    private final List<String> messages = new ArrayList<>();

    /**Metodo Init : inizializza il gioco
     * Questo metodo viene chiamato per inizializzare il gioco. 
     * Vengono definite le stanze, gli oggetti e i comandi che il giocatore può usare
     *
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        messages.clear();
        //Commands
        /*
         * Command è un attributo della classe GameDescription. Ogni comando ha un tipo e un nome.
         * Permette di definire i comandi che il giocatore può usare.
         */
        Command nord = new Command(CommandType.NORD, "nord");
        nord.setAlias(new String[]{"n", "N", "Nord", "NORD"}); //aggiunge degli alias al comando nord , lo stesso per gli altri comandi
        getCommands().add(nord); //aggiunge il comando nord alla lista dei comandi disponibili, lo stesso per gli altri comandi
        Command iventory = new Command(CommandType.INVENTORY, "inventario");
        iventory.setAlias(new String[]{"inv"});
        getCommands().add(iventory);
        Command sud = new Command(CommandType.SOUTH, "sud");
        sud.setAlias(new String[]{"s", "S", "Sud", "SUD"});
        getCommands().add(sud);
        Command est = new Command(CommandType.EAST, "est");
        est.setAlias(new String[]{"e", "E", "Est", "EST"});
        getCommands().add(est);
        Command ovest = new Command(CommandType.WEST, "ovest");
        ovest.setAlias(new String[]{"o", "O", "Ovest", "OVEST"});
        getCommands().add(ovest);
        Command end = new Command(CommandType.END, "end");
        end.setAlias(new String[]{"end", "fine", "esci", "muori", "ammazzati", "ucciditi", "suicidati", "exit", "basta"});
        getCommands().add(end);
        Command look = new Command(CommandType.LOOK_AT, "osserva");
        look.setAlias(new String[]{"guarda", "vedi", "trova", "cerca", "descrivi"});
        getCommands().add(look);
        Command pickup = new Command(CommandType.PICK_UP, "raccogli");
        pickup.setAlias(new String[]{"prendi"});
        getCommands().add(pickup);
        Command open = new Command(CommandType.OPEN, "apri");
        open.setAlias(new String[]{});
        getCommands().add(open);
        Command push = new Command(CommandType.PUSH, "premi");
        push.setAlias(new String[]{"spingi", "attiva"});
        getCommands().add(push);
        Command use = new Command(CommandType.USE, "usa");
        use.setAlias(new String[]{"utilizza", "combina"});
        getCommands().add(use);
        Command save = new Command(CommandType.SAVE, "salva");
        save.setAlias(new String[]{"salvataggio"});
        getCommands().add(save);
        //Rooms
        /*
         * Room è un attributo della classe GameDescription. Ogni stanza ha un id, un nome e una descrizione.
         * Ogni stanza ha un metodo setLook che permette di visualizzare la stanza e i collegamenti con le altre stanze.
         * L'inizializzazione delle stanze avviene tramite il costruttore della classe Room.
         */
        Room hall = new Room(0, "Corridoio", "Sei nel corridoio della vecchia casa.\nOrmai non abiti più qui da anni!\nTi ricorderai come raggiungere le innumerevoli stanze?");
        hall.setLook("Sei nel corridoio, a nord vedi il bagno, a sud il soggiorno e ad ovest la tua cameretta.\nForse il gioco sarà lì?");
        Room livingRoom = new Room(1, "Soggiorno", "Ti trovi nel soggiorno.\nCi sono quei mobili marrone scuro che hai sempre odiato e delle orribili sedie.");
        livingRoom.setLook("Non c'è nulla di interessante qui.");
        Room kitchen = new Room(2, "Cucina", "Ti trovi nella solita cucina.\nMobili bianchi, maniglie azzurre, quello strano lampadario che adoravi tanto quando eri piccolo.\n"
                + "C'è un tavolo con un bel portafrutta e una finestra.");
        kitchen.setLook("La solita cucina, ma noti una chiave vicino al portafrutta.");
        Room bathroom = new Room(3, "Bagno", "Sei nel bagno.\nQuanto tempo passato qui dentro...meglio non pensarci...");
        bathroom.setLook("Vedo delle batterie sul mobile alla destra del lavandino.");
        Room yourRoom = new Room(4, "La tua cameratta", "Finalmente la tua cameretta!\nQuesto luogo ti è così famigliare...ma non ricordi dove hai messo il nuovo regalo di zia Lina.");
        yourRoom.setLook("C'è un armadio bianco, di solito ci conservi i tuoi giochi.");
        //map
        /**
         * I metodi sottostanti definiscono la mappa del gioco. Ogni stanza ha un nome e una descrizione.
         */
        kitchen.setEast(livingRoom); //collega la cucina al soggiorno
        livingRoom.setNorth(hall); //collega il soggiorno al corridoio 
        livingRoom.setWest(kitchen); // collega il soggiorno alla cucina
        hall.setSouth(livingRoom); // collega il corridoio al soggiorno
        hall.setWest(yourRoom); // collega il corridoio alla tua cameretta
        hall.setNorth(bathroom); // collega il corridoio al bagno
        bathroom.setSouth(hall); // collega il bagno al corridoio
        yourRoom.setEast(hall);  // collega la tua cameretta al corridoio
        /*
         * Questi comandi aggiungono le stanze (kitchen, livingRoom, hall, bathroom, yourRoom) alla lista delle stanze del gioco
         * che si trovano nella classe GameDescription
         */
        getRooms().add(kitchen);  
        getRooms().add(livingRoom);
        getRooms().add(hall);
        getRooms().add(bathroom);
        getRooms().add(yourRoom);
        //obejcts
        /*
         * Permette di definire gli oggetti presenti nelle stanze.
         * Ogni AdvObject crea un oggetto con un id, un nome e una descrizione.
         */
        AdvObject battery = new AdvObject(1, "batteria", "Un pacco di batterie, chissà se sono cariche.");
        battery.setAlias(new String[]{"batterie", "pile", "pila"});
        bathroom.getObjects().add(battery);
        AdvObjectContainer wardrobe = new AdvObjectContainer(2, "armadio", "Un semplice armadio.");
        wardrobe.setAlias(new String[]{"guardaroba", "vestiario"});
        wardrobe.setOpenable(false); //metodo che setta l'armadio come non apribile
        wardrobe.setPickupable(false); //metodo che setta l'armadio come non raccoglibile
        wardrobe.setOpen(false); //metodo che setta l'armadio come chiuso
        yourRoom.getObjects().add(wardrobe); 
        AdvObject toy = new AdvObject(3, "giocattolo", "Il gioco che ti ha regalato zia Lina.");
        toy.setAlias(new String[]{"gioco", "robot"});
        toy.setPushable(false);
        toy.setPush(false); 
        wardrobe.add(toy);
        AdvObject kkey = new AdvObject(4, "chiave", "Usa semplice chiave come tante altre.");
        kkey.setAlias(new String[]{"key"});
        kkey.setPushable(false);
        kkey.setPush(false);
        kitchen.getObjects().add(kkey);
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
        GameObserver pickupObserver = new PickUpObserver();  // si occuperà di gestire l'azione di raccogliere
        this.attach(pickupObserver);
        GameObserver openObserver = new OpenObserver(); // si occuperà di gestire l'azione di aprire
        this.attach(openObserver);
        GameObserver useObserver = new UseObserver(); // si occuperà di gestire l'azione di usare
        this.attach(useObserver);
        //set starting room
        setCurrentRoom(hall); //setta la stanza iniziale del giocatore (nel  nostro caso sarà ingresso)
    }

    /**
     * Metodo che gestisce il comando successivo
     * @param p
     * @param out
     */
    @Override
    public void nextMove(ParserOutput p, PrintStream out) {
        parserOutput = p;
        messages.clear();
        if (p.getCommand() == null) {
            out.println("Non ho capito cosa devo fare! Prova con un altro comando.");
        } else {
            Room cr = getCurrentRoom();
            notifyObservers();
            boolean move = !cr.equals(getCurrentRoom()) && getCurrentRoom() != null;
            if (!messages.isEmpty()) {
                for (String m : messages) {
                    if (m.length() > 0) {
                        out.println(m);
                    }
                }
            }
            if (move) {
                out.println(getCurrentRoom().getName());
                out.println("================================================");
                out.println(getCurrentRoom().getDescription());
            }
        }
    }


    /** 
     *Metodo che permette di aggiungere un observer
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
     * @param o
     */
    @Override
    public void detach(GameObserver o) {
        observer.remove(o);
    }

    /**
     *Metodo che permette di notificare gli observer
     */
    @Override
    public void notifyObservers() {
        for (GameObserver o : observer) {
            messages.add(o.update(this, parserOutput));
        }
    }

    /**
     *Metodo che restituisce il messaggio di benvenuto
     * @return
     */
    @Override
    public String getWelcomeMsg() {
        return "Sei appena tornato a casa e non sai cosa fare.\nTi ricordi che non hai ancora utilizzato quel fantastico regalo di tua zia Lina.\n"
                + "Sarà il caso di cercarlo e di giocarci!\n";
    }
}
