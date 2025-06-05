package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.abstracts.GameState;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.AdvNPC;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.AdvObjectContainer;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.parser.CommandType;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementazione del primo livello del gioco.
 */
public class Level1State extends GameState {

    public Level1State(long timeLimit, List<Integer> requiredObjects, List<Integer> forbiddenObjects) {
        super(timeLimit, requiredObjects, forbiddenObjects);
    }

    public Level1State(long timeLimit, Room startingRoom, List<Integer> requiredObjects, List<Integer> forbiddenObjects) {
        super(timeLimit, startingRoom,requiredObjects, forbiddenObjects);
    }
    
    @Override
    public void enter(GameDescription gameDescription, OutputHandler output, String playerName) {
        // Primo piano (indice 0)
        // Aggiungi un NPC alla stanza di ingresso
        AdvNPC guido = new AdvNPC(Utils.NPC_GUIDO_ID, "Guido", "Un nano sembra essere il portinaio e sembra essere anche \n\t il tipico interista rompiscatole");
        guido.setAlias(new String[] { "guido", "nano", "segretario", "portinaio"});
        guido.setImagePath(ResourceLoader.IMG_PATH.resolve("nano.png").toString());

        // === CONFIGURAZIONE DIALOGHI ===
        // Dialogo iniziale
        guido.addFirstDialogueLine("Ciao " + playerName +  "! Benvenuto a Poggiolevante!");
        guido.addFirstDialogueLine("Sono Guido, tifi inter per caso? Se si allora sei nel posto giusto!");
        guido.addFirstDialogueLine("Sei qui per l'ammissione? \n\tPer il test ti conviene andare in Galileo, si trova dritto e a destra. Grazieeee!");
        
        // Dialogo dopo la prima interazione (prima del test)
        guido.addSubsequentDialogueLine("Ancor do' ste'? Muoviti il direttore ti sta aspettando!");

        AdvNPC donMatteo = new AdvNPC(Utils.NPC_DONMATTEO_ID, "Don. Matteo", "Sembra il sommo Kayoshin di Dragon Ball,\n\t indossa una tunica nera e sta leggendo un libro camminando in maniera strana");
        donMatteo.setAlias(new String[] { "don", "matteo", "prete", "sacerdote" });

        donMatteo.setImagePath(ResourceLoader.IMG_PATH.resolve("DonMatteo.png").toString());
        donMatteo.addFirstDialogueLine("Ciao " + playerName + "! Sono Don Matteo, il sacrestano di Poggiolevante.");
        donMatteo.addFirstDialogueLine("Se hai bisogno di consigli spirituali o vuoi confessarti, sono qui per aiutarti.");

        donMatteo.addSubsequentDialogueLine("Ricorda, la confessione è un momento importante per riflettere e rinnovare il tuo spirito.");
        donMatteo.addSubsequentDialogueLine("Buona fortuna con il tuo test di logica, " + playerName + "!");
        donMatteo.addSubsequentDialogueLine("Pregherò per te affinché tu possa superarlo con successo.");

        AdvNPC direttoreGalileo = new AdvNPC(Utils.NPC_DIRETTOREGALILEO_ID, "Direttore", "C'è il direttore, ti sta guardando e senti strani: [SALMON]\"Mh Mh MH[/]!!!\"\n\tmentre ti fissa con gli occhi sbarrati");
        direttoreGalileo.setImagePath(ResourceLoader.IMG_PATH.resolve("DirettoreGalileo.png").toString());
        direttoreGalileo.addFirstDialogueLine("Allora " + playerName + ", ci siamo? Sei pronto per il test di logica?");
        direttoreGalileo.addSubsequentDialogueLine(playerName + " dai dobbiamo sbrigarci, devo andare in bagno, rischio di [BROWN]cagarmi[/] addosso!");
        direttoreGalileo.setAlias(new String[] { "caccone", "cupulino", "cupula" });

        // === CREAZIONE DOMANDE DEL TEST ===
        
        // Domanda 1: Logica sequenziale
        List<String> options1 = Arrays.asList(
                "32",
                "28", 
                "30",
                "34"
        );
        Question question1 = new Question(
                "Trova il numero mancante nella sequenza: 2, 6, 12, 20, ?",
                options1,
                0
        );

        // Domanda 2: Logica spaziale
        List<String> options2 = Arrays.asList(
                "Sud-Est",
                "Nord-Ovest",
                "Sud-Ovest", 
                "Nord-Est"
        );
        Question question2 = new Question(
                "Se cammini verso Nord per 3km, poi verso Est per 4km, in che direzione devi andare per tornare al punto di partenza nel modo più diretto?",
                options2,
                2
        );

        // Domanda 3: Logica deduttiva
        List<String> options3 = Arrays.asList(
                "Marco ha i capelli neri",
                "Luca ha i capelli biondi",
                "Paolo ha i capelli castani",
                "Non si può determinare"
        );
        Question question3 = new Question(
                "Marco, Luca e Paolo hanno capelli di colori diversi: nero, biondo e castano. Marco non ha capelli biondi né neri. Luca non ha capelli castani. Quale affermazione è vera?",
                options3,
                2
        );

        // Domanda 4: Problema del runner e smartwatch
        List<String> options4 = Arrays.asList(
                "Lo smartwatch si è rotto",
                "Ha corso più velocemente del solito",
                "Ha cambiato la posizione dello smartwatch da un braccio all'altro",
                "Il percorso era in salita"
        );
        Question question4 = new Question(
                "Un runner usa sempre lo stesso smartwatch per monitorare i suoi allenamenti. Un giorno, percorrendo esattamente lo stesso percorso di sempre, lo smartwatch registra più chilometri del solito, nonostante abbia corso alla stessa velocità. Qual è la spiegazione più logica?",
                options4,
                2
        );

        // Lista delle domande
        List<Question> questions = Arrays.asList(question1, question2, question3, question4);

        AdvObject pen = new AdvObject(Utils.OBJ_PENNA_ID, "Penna", 
                "Una penna molto particolare, sopra c'è inciso un nome: Lorenzo Burdo\n\t",
                ResourceLoader.IMG_PATH.resolve("penna.png").toString());
        pen.setAlias(new String[] { "penna", "pen"});

        // === CREAZIONE DEL TEST ===
        List<AdvObject> requiredObjectsForTestLogic = new ArrayList<>();
        requiredObjectsForTestLogic.add(pen); // Aggiungiamo l'oggetto penna direttamente
        
        Test logicTest = new Test(
                "Test di Logica e Ragionamento", // Nome del test
                questions,                        // Lista domande
                2,                               
                "[SALMON]Mh Mh MH!![/] Hai dimostrato ottime capacità logiche e di ragionamento! Però potevi fare meglio secondo me",
                "[RED]😞[/]Sei un idiota!!",
                requiredObjectsForTestLogic
        );

        direttoreGalileo.setTest(logicTest);
        
        // === CREAZIONE OGGETTO RICOMPENSA ===
        AdvObject level1Complete = new AdvObject(Utils.OBJ_LEVEL1_COMPLETE_ID, "level1Complete");
        level1Complete.setVisible(false);
        direttoreGalileo.setRewardObject(level1Complete);

        // Aggiungi un oggetto che l'NPC può dare al giocatore
        AdvObject post_it = new AdvObject(Utils.OBJ_POST_IT_ID, "post-it",
        "\nUn [ITEM]post-it[/] attaccato alla porta con le istruzioni per l'avventura.\n\n" +
        "[EMERALD]=== ISTRUZIONI PER L'AVVENTURA ===[/]\n\n" +
        "[NEON_ORANGE]COMANDI MOVIMENTO[/]:\n" +
        "[NEON_ORANGE]•[/] nord/n, sud/s, est/e, ovest/o - per spostarti\n" +
        "[NEON_ORANGE]•[/] inventario/inv - controlla i tuoi oggetti\n\n" +
        "[NEON_ORANGE]COMANDI OGGETTI[/]:\n" +
        "[NEON_ORANGE]•[/] raccogli [oggetto] - prendi un oggetto\n" +
        "[NEON_ORANGE]•[/] usa [oggetto] - utilizza un oggetto\n" +
        "[NEON_ORANGE]•[/] apri [contenitore] - apri armadi/vetrine\n" +
        "[NEON_ORANGE]•[/] osserva [oggetto] - esamina attentamente\n" +
        "[NEON_ORANGE]•[/] premi [oggetto] - attiva/spingi qualcosa\n\n" +
        "[NEON_ORANGE]COMANDI SOCIALI[/]:\n" +
        "[NEON_ORANGE]•[/] parla [npc] - conversa con i personaggi\n\n" +
        "[NEON_ORANGE]COMANDI SISTEMA[/]:\n" +
        "[NEON_ORANGE]•[/] salva - salva la partita\n" +
        "[NEON_ORANGE]•[/] end/esci - termina il gioco\n",
        ResourceLoader.IMG_PATH.resolve("post_it.png").toString());
        post_it.setAlias(new String[] {"note", "appunto" });
        post_it.setPickupable(true); // Imposta l'oggetto come raccoglibile

        AdvNPC tutor = new AdvNPC(Utils.NPC_TUTOR_ID, "Tutor", "C'è un ragazzo rosso in questa stanza, sembra un po' un pingone");
        tutor.setAlias(new String[] { "tutor", "pingone" , "giuseppe"});
        
        // === CONFIGURAZIONE DIALOGHI TUTOR ===
        // Dialoghi iniziali
        tutor.addFirstDialogueLine("Uauà, signurì! Ie so u Tutòr d stu collègge, m chiem Giusepp, tu c si? Si vinùt p le seleziòn?");
        tutor.addFirstDialogueLine("Ah, piacèr " + playerName + ", l'hê mmê pr'vèt u pèn d Matèra?");
        tutor.addFirstDialogueLine("T sèrv na pènn, n'è vèrr? Sènd, pigghj chèst, ié d nu vècchj d stu collègge.");
        tutor.addFirstDialogueLine("Tand n la us mèj.");
        
        // Dialoghi successivi
        tutor.addSubsequentDialogueLine("Madònn, c fam ca tègn! T truàss p caso n'anticchj d sòlz agr'dòlc?");
        tutor.setImagePath(ResourceLoader.IMG_PATH.resolve("Tutor.png").toString());

        tutor.addItemToGive(pen);

        AdvObject foto = new AdvObject(Utils.OBJ_FOTO_ID, "Foto",
                "Una foto di San Nicola vestito da Astronauta.\nAnche un Santo può essere un astronauta !",
                ResourceLoader.IMG_PATH.resolve("SanColino.png").toString());
        foto.setAlias(new String[] { "Foto", "immagine", "picture"});
        foto.setPickupable(true);
        foto.setPushable(true); // Imposta l'oggetto come spingibile

        //Definizione oggetto contenitore armadio
        AdvObjectContainer armadioHall = new AdvObjectContainer(Utils.OBJ_ARMADIO_HALL_ID,"armadio",
                "Un armadio di legno chiuso. Potrebbe contenere qualcosa di interessante.");
        armadioHall.setAlias(new String[] { "Armadio", "armadietto", "mobile" });
        armadioHall.setPickupable(false); // Non raccoglibile
        armadioHall.setOpenable(true); // Imposta l'oggetto come apribile

        //Oggetti nell'armadio
        AdvObject cappotto = new AdvObject(Utils.OBJ_CAPPOTTO_ID, "Cappotto", 
                "Un cappotto di pelle, sembra molto costoso.",
                ResourceLoader.IMG_PATH.resolve("Cappotto.png").toString());
        cappotto.setAlias(new String[] { "CSappotto", "giacca", "giaccone"});
        cappotto.setPickupable(true);
        armadioHall.add(cappotto);

        AdvObject pantaloni = new AdvObject(Utils.OBJ_PANTALONI_ID, "Pantaloni", 
                "Un paio di pantaloni sporchi, emanano un bel profumino.",
                ResourceLoader.IMG_PATH.resolve("Pantaloni.png").toString());
        pantaloni.setAlias(new String[] { "Pantaloni", "jeans", "trousers"});
        pantaloni.setPickupable(true);
        armadioHall.add(pantaloni);

        AdvObject bastone = new AdvObject(Utils.OBJ_BASTONE_ID, "Bastone", 
                "Un bastone di legno, sembra robusto.",
                ResourceLoader.IMG_PATH.resolve("Bastone.png").toString());
        bastone.setAlias(new String[] { "Bastone", "deambulatore", "stick"});
        bastone.setPickupable(true);
        bastone.setPushable(true); // Imposta l'oggetto come spingibile
        armadioHall.add(bastone);

        //Definizione oggetto contenitore vetrina
        AdvObjectContainer vetrina = new AdvObjectContainer(Utils.OBJ_VETRINA_ID,"Vetrina",
                "Una vetrina di legno chiusa. Potrebbe contenere qualcosa di interessante.");
        vetrina.setAlias(new String[] { "Vetrina", "mobile", "vetrinetta" });
        vetrina.setPickupable(false);
        vetrina.setOpenable(true);

        //Oggetti nella vetrina
        AdvObject statuetta = new AdvObject(Utils.OBJ_STATUETTA_ID, "Statuetta",
                "Una statuetta di San Josemaria",
                ResourceLoader.IMG_PATH.resolve("Statuetta.png").toString());
        statuetta.setAlias(new String[] { "Statuetta", "statua", "figurina", "San Josemaria"});
        statuetta.setPickupable(true);
        vetrina.add(statuetta);

        AdvObject bibbia = new AdvObject(Utils.OBJ_BIBBIA_ID, "Bibbia", 
                "Un libro sacro, sembra molto antico.",
                ResourceLoader.IMG_PATH.resolve("Bibbia.png").toString());
        bibbia.setAlias(new String[] { "Bibbia", "libro", "sacra scrittura"});
        bibbia.setPickupable(true);
        vetrina.add(bibbia);

        AdvObject foglio = new AdvObject(Utils.OBJ_FOGLIO_GUIDA_ID, "Foglio guida",
                "Una guida utile all'esame di coscenza per la confessione.");
        foglio.setAlias(new String[] { "Foglio", "guida", "appunto"});
        foglio.setPickupable(false);
        vetrina.add(foglio);

        AdvObject microSD = new AdvObject(Utils.OBJ_MICROSD_ID, "MicroSD",
                "Una MicroSD, potrebbe essere utile successivamente.",
                ResourceLoader.IMG_PATH.resolve("MicroSD.png").toString());
        microSD.setAlias(new String[] { "MicroSD", "scheda", "memoria"});
        microSD.setPickupable(true);

        AdvObject chiaviAuto = new AdvObject(Utils.OBJ_CHIAVI_AUTO_ID, "Chiavi",
                "Le chiavi della macchina del Direttore, se non vieni ammesso potresti rubargliela.",
                ResourceLoader.IMG_PATH.resolve("ChiaviAuto.png").toString());
        chiaviAuto.setAlias(new String[] { "Chiave", "mazzo"});
        chiaviAuto.setPickupable(true);

        AdvObject forbici = new AdvObject(Utils.OBJ_FORBICI_ID, "Forbici",
                "Un paio di forbici, potrebbero tornarti utili.",
                ResourceLoader.IMG_PATH.resolve("Forbici.png").toString());
        forbici.setAlias(new String[] { "Forbici", "attrezzo", "strumento"});
        forbici.setPickupable(true);

        AdvObject martello = new AdvObject(Utils.OBJ_MARTELLO_ID, "Martello",
                "Un martello, ha un manico molto interessante",
                ResourceLoader.IMG_PATH.resolve("Martello.png").toString());
        martello.setAlias(new String[] { "Martello", "attrezzo", "strumento"});
        martello.setPickupable(true);
        martello.setPushable(true); // Imposta l'oggetto come spingibile

        AdvObject segaCircolare = new AdvObject(Utils.OBJ_SEGA_CIRCOLARE_ID, "Sega circolare",
                "Una sega circolare, sembra molto affilata.");
        segaCircolare.setAlias(new String[] { "Sega", "circolare"});
        segaCircolare.setPickupable(false);
        segaCircolare.setPushable(true);

        AdvObjectContainer contenitore = new AdvObjectContainer(Utils.OBJ_CONTENITORE_ID,"Contenitore",
                "Un contenitore di plastica, sembraa contenere dell'elettronica");
        contenitore.setAlias(new String[] { "Contenitore", "cassetto"});
        contenitore.setPickupable(false);
        contenitore.setOpenable(true);

        //Oggetti nel contenitore
        AdvObject cavoHDMI = new AdvObject(Utils.OBJ_CAVO_HDMI_ID, "CavoHDMI",
                "Un cavo HDMI",
                ResourceLoader.IMG_PATH.resolve("CavoHDMI.png").toString());
        cavoHDMI.setAlias(new String[] { "Cavi", "cavo", "filo"});
        cavoHDMI.setPickupable(true);
        contenitore.add(cavoHDMI);

        AdvObject mouse = new AdvObject(Utils.OBJ_MOUSE_ID, "Mouse",
                "Un mouse, sembra usurato",
                ResourceLoader.IMG_PATH.resolve("Mouse.png").toString());
        mouse.setAlias(new String[] { "Mouse", "puntatore"});
        mouse.setPickupable(true);
        contenitore.add(mouse);

        AdvObject tastiera = new AdvObject(Utils.OBJ_TASTIERA_ID, "Tastiera",
                "Una tastiera molto vecchia",
                ResourceLoader.IMG_PATH.resolve("Tastiera.png").toString());
        tastiera.setAlias(new String[] { "Tastiera", "periferica"});
        tastiera.setPickupable(true);
        tastiera.setPushable(true); // Imposta l'oggetto come spingibile
        contenitore.add(tastiera);

        AdvObject cpu = new AdvObject(Utils.OBJ_CPU_ID, "CPU", 
                "Il processore principale del computer. Questo è il cervello che farà funzionare tutto il sistema!",
                ResourceLoader.IMG_PATH.resolve("CPU.png").toString());
        cpu.setAlias(new String[] { "CPU", "processore", "chip", "centrale"});
        cpu.setPickupable(true);
        cpu.setPushable(true);
        contenitore.add(cpu);

        AdvObjectContainer rack = new AdvObjectContainer(Utils.OBJ_RACK_ID,"Rack",
                "Un armadio rack, da qui puoi accedere a tutti i server");
        rack.setAlias(new String[] { "Rack", "armadio", "mobile" });
        rack.setPickupable(false);
        rack.setOpenable(false);
        
        AdvObject chiaveRack = new AdvObject(Utils.OBJ_CHIAVE_RACK_ID, "Chiave",
                "Una piccola chiave, sembra quella di un armadio",
                ResourceLoader.IMG_PATH.resolve("ChiaveRack.png").toString());
        chiaveRack.setAlias(new String[] { "Chiave", "chiavi", "mazzo"});
        chiaveRack.setPickupable(true);

        //Oggetti nel rack
        AdvObject pulsante = new AdvObject(Utils.OBJ_PULSANTE_ID, "Pulsante",
                "Un pulsante rosso, chissà cosa fa");
        pulsante.setAlias(new String[] { "Pulsante", "bottone", "switch"});
        pulsante.setPickupable(false);
        pulsante.setPushable(true);
        rack.add(pulsante);

        AdvObject setCacciaviti = new AdvObject(Utils.OBJ_SET_CACCIAVITI_ID, "Set cacciaviti",
                "Un set di cacciaviti di precisione, potrebbe esserti utili.",
                ResourceLoader.IMG_PATH.resolve("Cacciaviti.png").toString());
        setCacciaviti.setAlias(new String[] { "Cacciavite", "cacciaviti", "set"});
        setCacciaviti.setPickupable(true);

        AdvObject saldatore = new AdvObject(Utils.OBJ_SALDATORE_ID, "Saldatore",
                "Un saldatore a mano con punta fine",
                ResourceLoader.IMG_PATH.resolve("Saldatore.png").toString());
        saldatore.setAlias(new String[] { "attrezzo", "saldatrice"});
        saldatore.setPickupable(true);
        saldatore.setPushable(true); // Imposta l'oggetto come spingibile

        AdvObject bobbinaPLA = new AdvObject(Utils.OBJ_BOBINA_PLA_ID, "Bobina",
                "Una bobina di filamento PLA per stampante 3D",
                ResourceLoader.IMG_PATH.resolve("BobinaPLA.png").toString());
        bobbinaPLA.setAlias(new String[] { "filamento", "PLA"});
        bobbinaPLA.setPickupable(true);
        bobbinaPLA.setPushable(true);

        AdvObject flipper = new AdvObject(Utils.OBJ_FLIPPER_ZERO_ID, "FlipperZero",
                "Un oggetto tecnomagico",
                ResourceLoader.IMG_PATH.resolve("Flipper.png").toString());
        flipper.setAlias(new String[] {"flipper", "tecnomagia"});
        flipper.setPickupable(true);

        Room entry = new Room(0, "Ingresso", "Ti trovi nell'ingresso di Poggiolevante");
        entry.addObject(guido, null);
        entry.setImagePath(ResourceLoader.IMG_PATH.resolve("Ingresso.png").toString());
        entry.addObject(post_it, "C'è un post-it attaccato alla porta con delle istruzioni.");

        Room hall = new Room(1, "Hall", "Ti trovi nella Hall di PoggioLevante.");
        hall.setImagePath(ResourceLoader.IMG_PATH.resolve("Hall.png").toString());
        hall.addObject(armadioHall, "C'è un armadio di legno chiuso.");

        Room reception = new Room(2, "Portineria", "Ti trovi nella portineria.");
        reception.setImagePath(ResourceLoader.IMG_PATH.resolve("Portineria.png").toString());
        reception.addObject(forbici, null);
        reception.addObject(chiaveRack, null);

        Room corridor = new Room(3, "Corridoio", "Ti trovi nel corridoio del primo piano.");
        corridor.setImagePath(ResourceLoader.IMG_PATH.resolve("Corridoio.png").toString());
        corridor.addObject(vetrina, "C'è una vetrina di legno chiusa.");

        Room galileo = new Room(4, "Galileo", "Sei nella stanza Galileo");
        galileo.setImagePath(ResourceLoader.IMG_PATH.resolve("Galileo.png").toString());        
        galileo.addObject(foto, "C'è una foto con una figura religiosa strana");

        Room office = new Room(5, "Direzione", "Sei in direzione");
        office.setImagePath(ResourceLoader.IMG_PATH.resolve("Direzione.png").toString());
        office.addObject(microSD, null);
        office.addObject(chiaviAuto, null);

        // Secondo piano (indice 1)
        Room hallway = new Room(6, "Disimpegno", "Ti trovi al 2° piano in un disimpegno.");
        hallway.setImagePath(ResourceLoader.IMG_PATH.resolve("Disimpegno.png").toString());
        
        Room craftRoom = new Room(7, "Stanza di del Manutentore", "Sei nel laboratorio di Pino.");
        craftRoom.setImagePath(ResourceLoader.IMG_PATH.resolve("LabPino.png").toString());
        craftRoom.addObject(segaCircolare, null);
        craftRoom.addObject(martello, null);
        craftRoom.addObject(contenitore, null);
        
        Room entryLab = new Room(8, "Ingresso Laboratorio", "Ti trovi nell'ingresso del laboratorio.");
        entryLab.setImagePath(ResourceLoader.IMG_PATH.resolve("Lab.png").toString());
        entryLab.addObject(rack, "C'è un armadio rack chiuso.");

        Room lab5 = new Room(9, "Laboratorio 5", "Sei nel laboratorio 5.");
        lab5.setImagePath(ResourceLoader.IMG_PATH.resolve("Lab5.png").toString());
        
        Room corridorLab = new Room(10, "Corridoio Laboratorio", "Sei nel corridoio del laboratorio.");
        corridorLab.setImagePath(ResourceLoader.IMG_PATH.resolve("Lab2.png").toString());
        
        Room lab3D = new Room(11, "Laboratorio 3D", "Sei nel laboratorio per stampe 3D.");
        lab3D.setImagePath(ResourceLoader.IMG_PATH.resolve("Lab3D.png").toString());
        lab3D.addObject(bobbinaPLA, null);
        
        Room electronicsLab = new Room(12, "Laboratorio Elettronica", "Sei nel laboratorio di elettronica.");
        electronicsLab.setImagePath(ResourceLoader.IMG_PATH.resolve("LabE.png").toString());
        electronicsLab.addObject(setCacciaviti, null);
        electronicsLab.addObject(saldatore, null);
        
        // Collegamenti primo piano
        entry.setWest(reception);
        reception.setEast(entry);
        entry.setNorth(hall);
        hall.setSouth(entry);
        hall.setWest(corridor);
        hall.setEast(galileo);
        hall.addObject(donMatteo, null);
        corridor.setEast(hall);
        corridor.setWest(office);
        office.setEast(corridor);
        office.addObject(tutor, null);
        // Collegamenti secondo piano
        galileo.setWest(hall);
        galileo.addObject(direttoreGalileo, null);
        hallway.setEast(craftRoom);
        craftRoom.setWest(hallway);
        hallway.setNorth(entryLab);
        //hallway.setForbidden(true); // Decommentare quando in produzione
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
        gameDescription.getGameMap().addRoom(entry, 0);
        gameDescription.getGameMap().addRoom(hall, 0);
        gameDescription.getGameMap().addRoom(reception, 0);
        gameDescription.getGameMap().addRoom(corridor, 0);
        gameDescription.getGameMap().addRoom(galileo, 0);
        gameDescription.getGameMap().addRoom(office, 0);

        gameDescription.getGameMap().addRoom(hallway, 1);
        gameDescription.getGameMap().addRoom(craftRoom, 1);
        gameDescription.getGameMap().addRoom(entryLab, 1);
        gameDescription.getGameMap().addRoom(lab5, 1);
        gameDescription.getGameMap().addRoom(corridorLab, 1);
        gameDescription.getGameMap().addRoom(lab3D, 1);
        gameDescription.getGameMap().addRoom(electronicsLab, 1);

        // Collegamento tra i piani
        gameDescription.getGameMap().linkFloors(corridor, hallway, CommandType.NORD);
        gameDescription.setCurrentRoom(entry);
        this.setStartingRoom(entry);
    }
    
    @Override
    public boolean isCompleted(GameDescription game) {
        // Verifica se tutti gli oggetti richiesti sono nell'inventario
        return game.getInventory().stream()
            .mapToInt(obj -> obj.getId())
            .boxed()
            .collect(java.util.stream.Collectors.toSet())
            .containsAll(this.getRequiredIDObjects());
    }
    
    @Override
    public boolean isFailureConditionMet(GameDescription game) {
        return  game.getInventory().stream()
                   .mapToInt(obj -> obj.getId())
                   .boxed()
                   .collect(java.util.stream.Collectors.toSet())
                   .stream()
                   .anyMatch(id -> this.getForbidenIDObjects().contains(id));
    }
    
    @Override
    public void handleSuccess(Runnable onSuccess) {
        // Esegue il callback per la transizione al livello successivo
        onSuccess.run();
    }
    
    @Override
    public void handleFailure(Runnable onFailure) {
        onFailure.run();
    }
    
    @Override
    public String getLevelName() {
        return "Introduzione & Test di Logica - Livello 1";
    }

    @Override
    public void getLevelDescription(OutputHandler output, String playerName, String remainingTime) {
        output.writeln("IMAGE:" + ResourceLoader.IMG_PATH.resolve("DirettoreIngresso.png").toString());
        output.writeln(this.getLevelName(), ColorText.TURQUOISE);
        output.writeln("[NPC]Direttore[/]: \"Ciao " + playerName + " benvenuto a Poggiolevante!\"");
        output.writeln("[NPC]Direttore[/]: \"Sono il Direttore di questo collegio e saro io a supervisionare la tua ammissione\"");
        output.writeln("[NPC]Direttore[/]: \"Per iniziare, devi superare un test di logica. Devo capire quanto sei intelligente e se sei quindi adatto a questo posto\n\tSpero tu abbia una [ITEM] penna[/] con te, ti servirà per il test!\"");
        output.writeln("[NPC]Direttore[/]: \"Successivamente dovrai superare una prova tecnica nel nostro laboratoio, devo capire se hai conoscenze elettroniche/informatiche.\"");
        output.writeln("[NPC]Direttore[/]: \"Hai [RED]" + remainingTime + "[/] minuti per venire a fare il test,\n\t se non verrai saprò che non ti interessa questo posto.\"");
        output.writeln("[NPC]Direttore[/]: \"Ci vediamo nella stanza Galileo. [SALMON]Mh Mh MH!!![/]\"");
    }
}