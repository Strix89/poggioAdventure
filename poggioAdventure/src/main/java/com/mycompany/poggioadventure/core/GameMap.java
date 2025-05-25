package com.mycompany.poggioadventure.core;

import com.mycompany.poggioadventure.model.AdvNPC;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.AdvObjectContainer;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.parser.CommandType;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameMap implements Serializable{
    private final List<List<Room>> allFloors = new ArrayList<>(); // Lista di piani

    public GameMap() {
        allFloors.add(new ArrayList<>()); // Inizializza primo piano
        allFloors.add(new ArrayList<>()); // Inizializza secondo piano
    }

    /**
     * Aggiunge una stanza a un piano specifico.
     * @param room Stanza da aggiungere
     * @param floorNumber Numero del piano (0-based)
     */
    public void addRoom(Room room, int floorNumber) {
        if (floorNumber >= 0 && floorNumber < allFloors.size()) {
            allFloors.get(floorNumber).add(room);
        } else {
            throw new IllegalArgumentException("Piano non valido: " + floorNumber);
        }
    }

    /**
     * Collega due stanze su piani diversi.
     * @param sourceRoom Stanza di partenza
     * @param targetRoom Stanza di arrivo
     */
    public void linkFloors(Room sourceRoom, Room targetRoom, CommandType dir) {
        if (!dir.isDirection()) {
            throw new IllegalArgumentException("La direzione deve essere NORD, SUD, EST o OVEST");
        }
        sourceRoom.setLinkedRoom(targetRoom, dir);
        targetRoom.setLinkedRoom(sourceRoom, dir.getOpposite());
    }

    /**
     * Restituisce la stanza iniziale (es. ingresso primo piano).
     */
    public Room getStartingRoom() {
        return allFloors.get(0).get(0); // Prima stanza del primo piano
    }

    /**
     * Restituisce una stanza dato il suo nome.
     * @param name Nome della stanza da cercare
     * @return La stanza trovata o null se non esiste
     */
    public Room getRoomByName(String name) {
        for (List<Room> floor : allFloors) {
            for (Room room : floor) {
                if (room.getName().equalsIgnoreCase(name)) {
                    return room;
                }
            }
        }
        return null;
    }

    // Metodo per aggiungere tutte le stanze (esempio)
    public void addElementsToGameDescription() {

        // Primo piano (indice 0)
        // Aggiungi un NPC alla stanza di ingresso
        AdvNPC guido = new AdvNPC(1, "Guido", "Un simpatico nano segretario");
        guido.setAlias(new String[] { "guido", "nano", "segretario" });
        guido.setImagePath(ResourceLoader.IMG_PATH.resolve("nano.png").toString());

        // Dialogo iniziale
        guido.addFirstDialogueLine("Ciao! Benvenuto a Poggiolevante!");
        guido.addFirstDialogueLine("Ho qui un oggetto che potrebbe esserti utile...");
        // Dialogo successivo
        guido.addSubsequentDialogueLine("Ben tornato! Spero che il post-it ti sia stato utile.");
        guido.addSubsequentDialogueLine("Buona fortuna per la tua avventura!");

        // Aggiungi un oggetto che l'NPC può dare al giocatore
        AdvObject post_it = new AdvObject(2, "post-it", "Un post-it con delle istruzioni.");
        post_it.setAlias(new String[] { "post-it", "note", "appunto" });
        post_it.setPickupable(true); // Imposta l'oggetto come raccoglibile
        guido.addItemToGive(post_it);
        
        AdvObject pen = new AdvObject(3, "Penna", 
                ResourceLoader.IMG_PATH.resolve("penna.png").toString(),
                "Una penna molto particolare, apprtenuta a Lovrenzo Burdo.\nTrattala con passione e devozione.");
        pen.setAlias(new String[] { "penna", "pen"});
        pen.setPickupable(true);

        AdvObject foto = new AdvObject(4, "Foto",
                ResourceLoader.IMG_PATH.resolve("SanColino.png").toString(),
                "Una foto di San Nicola vestito da Astronauta.\nAnche un Santo può essere un astronauta !");
        foto.setAlias(new String[] { "Foto", "immagine", "picture"});
        foto.setPickupable(true);

        //Definizione oggetto contenitore armadio
        AdvObjectContainer armadioHall = new AdvObjectContainer(5,"armadio",
                "Un armadio di legno chiuso. Potrebbe contenere qualcosa di interessante.");
        armadioHall.setAlias(new String[] { "Armadio", "armadietto", "mobile" });
        armadioHall.setPickupable(false); // Non raccoglibile
        armadioHall.setOpenable(true); // Imposta l'oggetto come apribile

        //Oggetti nell'armadio
        AdvObject cappotto = new AdvObject(6, "Cappotto", 
                ResourceLoader.IMG_PATH.resolve("Cappotto.png").toString(),
                "Un cappotto di pelle, sembra molto costoso.");
        cappotto.setAlias(new String[] { "CSappotto", "giacca", "giaccone"});
        cappotto.setPickupable(true);
        armadioHall.add(cappotto);

        AdvObject pantaloni = new AdvObject(7, "Pantaloni", 
                ResourceLoader.IMG_PATH.resolve("Pantaloni.png").toString(),
                "Un paio di pantaloni sporchi, emanano un bel profumino.");
        pantaloni.setAlias(new String[] { "Pantaloni", "jeans", "trousers"});
        pantaloni.setPickupable(true);
        armadioHall.add(pantaloni);

        AdvObject bastone = new AdvObject(8, "Bastone", 
                ResourceLoader.IMG_PATH.resolve("Bastone.png").toString(),
                "Un bastone di legno, sembra robusto.");
        bastone.setAlias(new String[] { "Bastone", "deambulatore", "stick"});
        bastone.setPickupable(true);
        armadioHall.add(bastone);

        //Definizione oggetto contenitore vetrina
        AdvObjectContainer vetrina = new AdvObjectContainer(9,"Vetrina",
                "Una vetrina di legno chiusa. Potrebbe contenere qualcosa di interessante.");
        vetrina.setAlias(new String[] { "Vetrina", "mobile", "vetrinetta" });
        vetrina.setPickupable(false);
        vetrina.setOpenable(true);

        //Oggetti nella vetrina
        AdvObject statuetta = new AdvObject(10, "Statuetta", 
                ResourceLoader.IMG_PATH.resolve("Statuetta.png").toString(),
                "Una statuetta di San Josemaria");
        statuetta.setAlias(new String[] { "Statuetta", "statua", "figurina", "San Josemaria"});
        statuetta.setPickupable(true);
        vetrina.add(statuetta);

        AdvObject bibbia = new AdvObject(11, "Bibbia", 
                ResourceLoader.IMG_PATH.resolve("Bibbia.png").toString(),
                "Un libro sacro, sembra molto antico.");
        bibbia.setAlias(new String[] { "Bibbia", "libro", "sacra scrittura"});
        bibbia.setPickupable(true);
        vetrina.add(bibbia);

        AdvObject foglio = new AdvObject(12, "Foglio guida",
                "Una guida utile all'esame di coscenza per la confessione.");
        foglio.setAlias(new String[] { "Foglio", "guida", "appunto"});
        foglio.setPickupable(false);
        vetrina.add(foglio);

        AdvObject microSD = new AdvObject(13, "MicroSD",
                ResourceLoader.IMG_PATH.resolve("MicroSD.png").toString(),
                "Una MicroSD, potrebbe essere utile successivamente.");
        microSD.setAlias(new String[] { "MicroSD", "scheda", "memoria"});
        microSD.setPickupable(true);

        AdvObject chiaviAuto = new AdvObject(14, "Chiavi",
                ResourceLoader.IMG_PATH.resolve("ChiaviAuto.png").toString(),
                "Le chiavi della macchina del Direttore, se non vieni ammesso potresti rubargliela.");
        chiaviAuto.setAlias(new String[] { "Chiave", "mazzo"});
        chiaviAuto.setPickupable(true);

        AdvObject forbici = new AdvObject(15, "Forbici",
                ResourceLoader.IMG_PATH.resolve("Forbici.png").toString(),
                "Un paio di forbici, potrebbero tornarti utili.");
        forbici.setAlias(new String[] { "Forbici", "attrezzo", "strumento"});
        forbici.setPickupable(true);
        forbici.setPushable(true);

        AdvObject martello = new AdvObject(16, "Martello",
                ResourceLoader.IMG_PATH.resolve("Martello.png").toString(),
                "Un martello, ha un manico molto interessante");
        martello.setAlias(new String[] { "Martello", "attrezzo", "strumento"});
        martello.setPickupable(true);

        AdvObject segaCircolare = new AdvObject(17, "Sega circolare",
                "Una sega circolare, sembra molto affilata.");
        segaCircolare.setAlias(new String[] { "Sega", "circolare"});
        segaCircolare.setPickupable(false);
        segaCircolare.setPushable(true);

        AdvObjectContainer contenitore = new AdvObjectContainer(18,"Contenitore",
                "Un contenitore di plastica, semba contenere dell'elettronica");
        contenitore.setAlias(new String[] { "Contenitore", "cassetto"});
        contenitore.setPickupable(false);
        contenitore.setOpenable(true);

        //Oggetti nel contenitore
        AdvObject cavoHDMI = new AdvObject(19, "CavoHDMI",
                ResourceLoader.IMG_PATH.resolve("CavoHDMI.png").toString(),
                "Un cavo HDMI");
        cavoHDMI.setAlias(new String[] { "Cavi", "cavo", "filo"});
        cavoHDMI.setPickupable(true);
        contenitore.add(cavoHDMI);

        AdvObject mouse = new AdvObject(20, "Mouse",
                ResourceLoader.IMG_PATH.resolve("Mouse.png").toString(),
                "Un mouse, sembra usurato");
        mouse.setAlias(new String[] { "Mouse", "puntatore"});
        mouse.setPickupable(true);
        contenitore.add(mouse);

        AdvObject tastiera = new AdvObject(21, "Tastiera",
                ResourceLoader.IMG_PATH.resolve("Tastiera.png").toString(),
                "Una tastiera molto vecchia");
        tastiera.setAlias(new String[] { "Tastiera", "periferica"});
        tastiera.setPickupable(true);
        contenitore.add(tastiera);

        AdvObjectContainer rack = new AdvObjectContainer(22,"Rack",
                "Un armadio rack, da qui puoi accedere a tutti i server");
        rack.setAlias(new String[] { "Rack", "armadio", "mobile" });
        rack.setPickupable(false);
        rack.setOpenable(false);
        
        AdvObject chiaveRack = new AdvObject(23, "Chiave",
                ResourceLoader.IMG_PATH.resolve("ChiaveRack.png").toString(),
                "Una piccola chiave, sembra quella di un armadio");
        chiaveRack.setAlias(new String[] { "Chiave", "chiavi", "mazzo"});
        chiaveRack.setPickupable(true);

        //Oggetti nel rack
        AdvObject pulsante = new AdvObject(24, "Pulsante",
                "Un pulsante rosso, chissà cosa fa");
        pulsante.setAlias(new String[] { "Pulsante", "bottone", "switch"});
        pulsante.setPickupable(false);
        pulsante.setPushable(true);
        rack.add(pulsante);

        AdvObject setCacciaviti = new AdvObject(25, "Set cacciaviti",
                ResourceLoader.IMG_PATH.resolve("Cacciaviti.png").toString(),
                "Un set di cacciaviti di precisione, potrebbe esserti utili.");
        setCacciaviti.setAlias(new String[] { "Cacciavite", "cacciaviti", "set"});
        setCacciaviti.setPickupable(true);

        AdvObject saldatore = new AdvObject(26, "Saldatore",
                ResourceLoader.IMG_PATH.resolve("Saldatore.png").toString(),
                "Un saldatore a mano con punta fine");
        saldatore.setAlias(new String[] { "attrezzo", "saldatrice"});
        saldatore.setPickupable(true);

        AdvObject bobbinaPLA = new AdvObject(27, "Bobina",
                ResourceLoader.IMG_PATH.resolve("BobinaPLA.png").toString(),
                "Una bobina di filamento PLA per stampante 3D");
        bobbinaPLA.setAlias(new String[] { "filamento", "PLA"});
        bobbinaPLA.setPickupable(true);
        
        AdvObject flipper = new AdvObject(50, "FlipperZero",
                ResourceLoader.IMG_PATH.resolve("Flipper.png").toString(),
                "Un oggetto tecnomagico");
        flipper.setAlias(new String[] {"flipper", "tecnomagia"});
        flipper.setPickupable(true);

        Room entry = new Room(0, "Ingresso", "Ti trovi nell'ingresso di Poggiolevante");
        entry.addObject(guido, "C'è un nano vicino la porta, leggi il nome sulla targhetta si chiama GUIDO");
        entry.setImagePath(ResourceLoader.IMG_PATH.resolve("Ingresso.png").toString());

        Room hall = new Room(1, "Hall", "Ti trovi nella Hall di PoggioLevante.");
        hall.setImagePath(ResourceLoader.IMG_PATH.resolve("Hall.png").toString());
        hall.addObject(armadioHall, "C'è un armadio di legno chiuso.");

        Room reception = new Room(2, "Portineria", "Ti trovi nella portineria.");
        reception.setImagePath(ResourceLoader.IMG_PATH.resolve("Portineria.png").toString());
        reception.addObject(forbici);
        reception.addObject(chiaveRack);

        Room corridor = new Room(3, "Corridoio", "Ti trovi nel corridoio del primo piano.");
        corridor.setImagePath(ResourceLoader.IMG_PATH.resolve("Corridoio.png").toString());
        corridor.addObject(vetrina, "C'è una vetrina di legno chiusa.");

        Room galileo = new Room(4, "Galileo", "Sei nella stanza Galileo");
        galileo.setImagePath(ResourceLoader.IMG_PATH.resolve("Galileo.png").toString());        
        galileo.addObject(pen);
        galileo.addObject(foto);
        galileo.addObject(flipper);

        Room office = new Room(5, "Direzione", "Sei in direzione");
        office.setImagePath(ResourceLoader.IMG_PATH.resolve("Direzione.png").toString());
        office.addObject(microSD);
        office.addObject(chiaviAuto);

        // Secondo piano (indice 1)
        Room hallway = new Room(6, "Disimpegno", "Ti trovi al 2° piano in un disimpegno.");
        hallway.setImagePath(ResourceLoader.IMG_PATH.resolve("Disimpegno.png").toString());
        
        Room craftRoom = new Room(7, "Stanza di Pino", "Sei nel laboratorio di Pino.");
        craftRoom.setImagePath(ResourceLoader.IMG_PATH.resolve("LabPino.png").toString());
        craftRoom.addObject(segaCircolare);
        craftRoom.addObject(martello);
        craftRoom.addObject(contenitore);
        
        Room entryLab = new Room(8, "Ingresso Laboratorio", "Ti trovi nell'ingresso del laboratorio.");
        entryLab.setImagePath(ResourceLoader.IMG_PATH.resolve("Lab.png").toString());
        entryLab.addObject(rack, "C'è un armadio rack chiuso.");

        Room lab5 = new Room(9, "Laboratorio 5", "Sei nel laboratorio 5.");
        lab5.setImagePath(ResourceLoader.IMG_PATH.resolve("Lab5.png").toString());
        
        Room corridorLab = new Room(10, "Corridoio Laboratorio", "Sei nel corridoio del laboratorio.");
        corridorLab.setImagePath(ResourceLoader.IMG_PATH.resolve("Lab2.png").toString());
        
        Room lab3D = new Room(11, "Laboratorio 3D", "Sei nel laboratorio per stampe 3D.");
        lab3D.setImagePath(ResourceLoader.IMG_PATH.resolve("Lab3D.png").toString());
        lab3D.addObject(bobbinaPLA);
        
        Room electronicsLab = new Room(12, "Laboratorio Elettronica", "Sei nel laboratorio di elettronica.");
        electronicsLab.setImagePath(ResourceLoader.IMG_PATH.resolve("LabE.png").toString());
        electronicsLab.addObject(setCacciaviti);
        electronicsLab.addObject(saldatore);
        
        // Collegamenti primo piano
        entry.setWest(reception);
        reception.setEast(entry);
        entry.setNorth(hall);
        hall.setSouth(entry);
        hall.setWest(corridor);
        hall.setEast(galileo);
        corridor.setEast(hall);
        corridor.setWest(office);
        office.setEast(corridor);
        // Collegamenti secondo piano
        galileo.setWest(hall);
        hallway.setEast(craftRoom);
        craftRoom.setWest(hallway);
        hallway.setNorth(entryLab);
        entryLab.setSouth(hallway);
        entryLab.setEast(lab5);
        lab5.setWest(entryLab);
        entryLab.setNorth(corridorLab);
        corridorLab.setSouth(entryLab);
        corridorLab.setEast(lab3D);
        lab3D.setWest(corridorLab);
        corridorLab.setNorth(electronicsLab);
        electronicsLab.setSouth(corridorLab);
        // Aggiunta stanze ai piani
        addRoom(entry, 0);
        addRoom(hall, 0);
        addRoom(reception, 0);
        addRoom(corridor, 0);
        addRoom(galileo, 0);
        addRoom(office, 0);

        addRoom(hallway, 1);
        addRoom(craftRoom, 1);
        addRoom(entryLab, 1);
        addRoom(lab5, 1);
        addRoom(corridorLab, 1);
        addRoom(lab3D, 1);
        addRoom(electronicsLab, 1);

        // Collegamento tra i piani
        linkFloors(corridor, hallway, CommandType.NORD);
    }

    public List<List<Room>> getAllFloors() {
        return allFloors;
    }
    
    /**
    * Rimuove le immagini da tutti gli NPC presenti nelle stanze del gioco.Utilizza espressioni lambda e stream per l'elaborazione.
     * @param obscure
    */
    public void alterateNPCImages(boolean obscure) {
        allFloors.stream()
            .flatMap(List::stream) // Flattena tutte le stanze in un unico stream
            .forEach(room -> room.getObjects().stream()
                .filter(obj -> obj instanceof AdvNPC)
                .forEach(npc -> ((AdvNPC) npc).setObscureImage(obscure))
            );
    }
}