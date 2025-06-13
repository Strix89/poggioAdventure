package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.abstracts.GameState;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.AdvNPC;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;

import java.util.List;

/**
 * Terzo livello del gioco: prova finale con controllo robot aspirapolvere.
 * 
 * <p>Il livello finale del processo di ammissione in cui il candidato deve
 * dimostrare le proprie competenze informatiche controllando robot aspirapolvere
 * impazziti utilizzando un dispositivo Flipper Zero.
 * 
 * <p><b>Obiettivi del livello:</b>
 * <ul>
 *   <li>Ottenere il manuale dei robot dal portinaio Guido</li>
 *   <li>Trovare il dispositivo Flipper Zero nascosto nei laboratori</li>
 *   <li>Utilizzare le frequenze corrette per controllare i robot</li>
 *   <li>Risolvere la crisi dei robot aspirapolvere</li>
 * </ul>
 * 
 * <p><b>Meccaniche:</b>
 * <ul>
 *   <li>Interazione con Flipper Zero tramite frequenze specifiche</li>
 *   <li>Comando speciale "usa flipper zero"</li>
 *   <li>Sistema di penalità/bonus temporali</li>
 *   <li>Completamento automatico con comando GoToRecharge</li>
 * </ul>
 * 
 * <p>Completamento: esecuzione del comando GoToRecharge con frequenza corretta.
 */
public class Level3State extends GameState {

    /** Costruttore base per configurazione livello */
    public Level3State(long timeLimit, List<Integer> requiredObjects, List<Integer> forbiddenObjects) {
        super(timeLimit, requiredObjects, forbiddenObjects);
    }

    /** Costruttore con stanza di partenza personalizzata */
    public Level3State(long timeLimit, Room startingRoom, List<Integer> requiredObjects, List<Integer> forbiddenObjects) {
        super(timeLimit, startingRoom, requiredObjects, forbiddenObjects);
    }
    
    /**
     * Inizializza il livello con gli oggetti e NPC necessari.
     * Mantiene la posizione corrente del giocatore e prepara gli oggetti
     */
    @Override
    public void enter(GameDescription gameDescription, OutputHandler output, String playerName) {
        
        // Giocatore inizia nel laboratorio di ingresso
        Room entryLab = gameDescription.getGameMap().findRoomById(Utils.ROOM_ENTRY_LAB_ID);
        this.setStartingRoom(entryLab);

        // -- OGGETTI LIVELLO 3 --

        // Dispositivo Flipper Zero - oggetto chiave per il controllo robot
        AdvObject flipperZero = new AdvObject(Utils.OBJ_FLIPPER_ZERO_ID, "FlipperZero",
                "Un dispositivo che consente di effettuare diversi attacchi informatici",
                ResourceLoader.IMG_PATH.resolve("Flipper.png").toString());
        flipperZero.setAlias(new String[]{"flipper", "flipperzero", "tecnomagia", "dispositivo"});
        flipperZero.setPickupable(true);
        
        // Manuale del Flipper Zero - fornisce istruzioni sui comandi disponibili
        AdvObject manualeFlipper = new AdvObject(Utils.OBJ_MANUALE_ID, "Manuale Flipper",
                "[NEON_ORANGE]=== MANUALE FLIPPER ZERO ===[/]\n\n" +
                "[YELLOW]COMANDI DISPONIBILI:[/]\n" +
                "• [GREEN]GoToRecharge[/] - Invia i robot alle stazioni di ricarica\n" +
                "• [ORANGE]Override[/] - Sovrascrive i controlli\n" +
                "• [BLUE]Stop[/] - Ferma temporaneamente i robot\n\n" +
                "[RED]IMPORTANTE:[/] Ogni comando deve essere inviato sulla frequenza corretta!\n",
                ResourceLoader.IMG_PATH.resolve("Manuale.png").toString());
        manualeFlipper.setAlias(new String[]{"manuale", "istruzioni", "libro"});
        manualeFlipper.setPickupable(true);

        
        // --- NPC Livello 3 ---

        // GUIDO - Portinaio del collegio
        AdvNPC guidoPanico = new AdvNPC(Utils.NPC_GUIDO_ID, "Guido", 
                    "Il portinaio sembra in preda al panico! Sta gesticolando verso i laboratori.");
        guidoPanico.setAlias(new String[]{"guido", "portinaio", "nano"});
        guidoPanico.setImagePath(ResourceLoader.IMG_PATH.resolve("NanoPanico.png").toString());
        guidoPanico.addFirstDialogueLine("AIUTO! " + playerName + "! I robot aspirapolvere sono impazziti!");
        guidoPanico.addFirstDialogueLine("Stanno aspirando tutto quello che trovano, compresi i miei pochi capelli!");
        guidoPanico.addFirstDialogueLine("Ho qui il manuale d'istruzioni dei robot, prendilo!");
        guidoPanico.addFirstDialogueLine("Devi trovare il modo di fermarli prima che distruggano tutto il collegio!");
        guidoPanico.addSubsequentDialogueLine("Allora ?! Hai trovato il modo per fermarli?");
        guidoPanico.addItemToGive(manualeFlipper);

        // DIRETTORE 
        AdvNPC direttore = new AdvNPC(Utils.NPC_DIRETTORE_LAB_ID, "Direttore", "Il direttore del collegio, sembra stia attendendo il tuo arrivo");
        direttore.setImagePath(ResourceLoader.IMG_PATH.resolve("DirettoreLab.png").toString());
        direttore.setAlias(new String[]{"direttore", "michele", "dottore"});
        direttore.addFirstDialogueLine("Hai parlato con Guido? Sembra molto agitato...");
        direttore.addSubsequentDialogueLine("Devi risolvere questa situazione al più presto!");

        // LUIGI 
        AdvNPC luigi = new AdvNPC(Utils.NPC_LUIGI_ID, "Luigi", "Un collegiale molto particolare, sembra essere un esperto di elettronica");
        luigi.setImagePath(ResourceLoader.IMG_PATH.resolve("Scimmia.png").toString());
        luigi.setAlias(new String[]{"luigi", "scimmia", "bom"});
        luigi.addFirstDialogueLine("Ciao " + playerName + "! Hai bisogno di aiuto con quei robot?");
        luigi.addFirstDialogueLine("Non dirlo al direttore, ma ho un dispositivo che potrebbe aiutarti a controllarli!");
        luigi.addFirstDialogueLine("Si chiama Flipper Zero, è un dispositivo fantastico per fare hacking e controllare dispositivi elettronici!");
        luigi.addFirstDialogueLine("Posso dartelo, ma devi promettere di usarlo solo per il bene del collegio!");
        luigi.addItemToGive(flipperZero);
        luigi.addSubsequentDialogueLine(playerName + ", hai utilizzato il Flipper Zero? È davvero potente!");

        // -- RECUPERO STANZE per posizionamento oggetti --
        Room electronicsLab = gameDescription.getGameMap().findRoomById(Utils.ROOM_ELECTRONICS_LAB_ID);

        // -- POSIZIONAMENTO NPC --
        entryLab.addObject(guidoPanico, null);
        entryLab.addObject(direttore, null);
        electronicsLab.addObject(luigi, null);

    }

    /**
     * Verifica completamento livello: [LOGICA DI COMPLETAMENTO]
     */
    @Override
    public boolean isCompleted(GameDescription game) {
        return game.getInventory().stream()
        .anyMatch(obj -> obj.getId() == Utils.OBJ_LEVEL3_COMPLETE_ID);
    }

    /**
     * Verifica fallimento: [LOGICA DI FALLIMENTO]
     */
    @Override
    public boolean isFailureConditionMet(GameDescription game) {
        return game.getInventory().stream()
                   .mapToInt(obj -> obj.getId())
                   .boxed()
                   .collect(java.util.stream.Collectors.toSet())
                   .stream()
                   .anyMatch(id -> this.getForbiddenIDObjects().contains(id));
    }
    
    /** Esegue callback per completamento del livello */
    @Override
    public void handleSuccess(Runnable onSuccess) {
        onSuccess.run();
    }
    
    /** Esegue callback per gestione fallimento */
    @Override
    public void handleFailure(Runnable onFailure) {
        onFailure.run();
    }
    
    @Override
    public String getLevelName() {
        return "Livello 3 - Rivoluzione Robot";
    }

    /**
     * Descrizione del livello 3: Rivoluzione Robot
     * 
     * <p>Mostra la descrizione del livello, i dialoghi e le immagini
     * necessarie per il completamento.
     */
    @Override
    public void getLevelDescription(OutputHandler output, String playerName, String remainingTime) {
        output.writeln("\n" + "=".repeat(60), ColorText.CYAN);
        output.writeln("LIVELLO 3 - RIVOLUZIONE ROBOT", ColorText.CYAN);
        output.writeln("=".repeat(60), ColorText.CYAN);
        output.writeln();
        output.writeln("IMAGE:" + ResourceLoader.IMG_PATH.resolve("Dir_Nano_Lab.png").toString());
        
        output.writeln("\nIl Direttore ti si avvicina con un sorriso soddisfatto...\n");
        
        // Dialoghi del Direttore che si congratula
        output.writeln("[NPC]Direttore[/]: \"Complimenti " + playerName + "! Hai superato brillantemente anche la seconda prova!\"");
        output.writeln("[NPC]Direttore[/]: \"Le tue competenze tecniche sono davvero impressionanti...\"");
        output.writeln("[NPC]Direttore[/]: \"Devo ammettere che non mi aspettavo un assemblaggio così preciso e veloce!\"");
        
        output.writeln("\nMa improvvisamente, vedi una figura familiare correre verso di voi...\n", ColorText.YELLOW);
        
        // Descrizione di Guido che arriva in panico
        output.writeln("È Guido, il portinaio! Sta correndo come un pazzo, strillando e gesticolando disperatamente!", ColorText.RED);
        output.writeln("Il suo volto è contorto dalla paura e sembra completamente fuori di sé!", ColorText.RED);
        
        // Reazione del Direttore
        output.writeln("\n[NPC]Direttore[/]: \"Cosa diavolo...? Guido sembra terrorizzato!\"");
        output.writeln("[NPC]Direttore[/]: \"" + playerName + ", dobbiamo parlare con lui IMMEDIATAMENTE!\"");
        output.writeln("[NPC]Direttore[/]: \"Qualcosa di grave deve essere successo nel collegio...\"");
        output.writeln("[NPC]Direttore[/]: \"Vai da lui e scopri cosa è successo, potrebbero esserci delle emergenze!\"");
        
        output.writeln("\nSITUAZIONE DI EMERGENZA", ColorText.RED);
        output.writeln("Devi parlare con Guido per capire cosa sta succedendo!", ColorText.WHITE);
        
        output.writeln("[NPC]Direttore[/]: \"Hai [RED]" + remainingTime + "[/] minuti per risolvere questa situazione!\"");
        output.writeln("=".repeat(60) + "\n", ColorText.CYAN);
    }
}