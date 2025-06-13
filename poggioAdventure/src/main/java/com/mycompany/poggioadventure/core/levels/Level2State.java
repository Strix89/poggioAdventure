package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.abstracts.GameState;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.AdvNPC;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.AdvObjectContainer;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;

import java.util.List;

/**
 * Secondo livello del gioco: prova tecnica nel laboratorio elettronico.
 * 
 * <p>Il giocatore deve dimostrare competenze tecniche per completare l'ammissione
 * al collegio. La sfida richiede l'assemblaggio di un dispositivo elettronico
 * utilizzando componenti trovati nei laboratori precedentemente inaccessibili.
 * 
 * <p><b>Obiettivi del livello:</b>
 * <ul>
 *   <li>Accesso ai laboratori del primo piano (precedentemente vietati)</li>
 *   <li>Raccolta componenti elettronici necessari</li>
 *   <li>Assemblaggio dispositivo IoT completo</li>
 *   <li>Superamento test tecnico finale</li>
 * </ul>
 * 
 * <p><b>Meccaniche:</b>
 * <ul>
 *   <li>Sblocco automatico area laboratori</li>
 *   <li>Continuazione dalla posizione corrente del giocatore</li>
 *   <li>Raccolta componenti specifici per assemblaggio</li>
 *   <li>Comando speciale "assembla dispositivo"</li>
 * </ul>
 * 
 * <p>Completamento: assemblaggio corretto del dispositivo con tutti i componenti richiesti.
 */
public class Level2State extends GameState {

    private GameDescription gameDescription;

    /** Costruttore base per configurazione livello */
    public Level2State(long timeLimit, List<Integer> requiredObjects, List<Integer> forbiddenObjects) {
        super(timeLimit, requiredObjects, forbiddenObjects);
    }

    /** Costruttore con stanza di partenza personalizzata */
    public Level2State(long timeLimit, Room startingRoom, List<Integer> requiredObjects, List<Integer> forbiddenObjects) {
        super(timeLimit, startingRoom, requiredObjects, forbiddenObjects);
    }
    
    /**
     * Inizializza il secondo livello sbloccando l'accesso ai laboratori.
     * Mantiene la posizione corrente del giocatore come punto di partenza.
     */
    @Override
    public void enter(GameDescription gameDescription, OutputHandler output, String playerName) {
        // Sblocca accesso ai laboratori del primo piano

        this.gameDescription = gameDescription;

        Room hallway = gameDescription.getGameMap().findRoomById(Utils.ROOM_HALLWAY_ID);
        hallway.setForbidden(false);
        
        // Continua dalla posizione corrente invece di forzare spostamento
        this.setStartingRoom(gameDescription.getCurrentRoom());
        
        output.writeln("Sembra che il disimpegno si sia aperto..", ColorText.RED);


        // -- OGGETTI PER COSTRUIRE IL PC DESKTOP --

        // Contenitore principale per assemblaggio PC
        AdvObjectContainer casePc = new AdvObjectContainer(Utils.OBJ_CASE_PC_ID, "Case PC",
                "Un case per computer desktop vuoto. Qui dovrai assemblare tutti i componenti per creare un sistema funzionante.");
        casePc.setAlias(new String[]{"case", "chassis", "tower", "cabinet", "pc"});
        casePc.setPickupable(false);
        casePc.setOpenable(true);
        
        // CPU - Processore principale
        AdvObject cpu = new AdvObject(Utils.OBJ_CPU_ID, "CPU", 
                "Un processore Intel di ultima generazione. Il cervello del computer che eseguirà tutti i calcoli.",
                ResourceLoader.IMG_PATH.resolve("CPU.png").toString());
        cpu.setAlias(new String[]{"processore", "chip", "intel", "centrale"});
        cpu.setPickupable(true);
        
        // RAM - Memoria volatile
        AdvObject ram = new AdvObject(Utils.OBJ_RAM_ID, "RAM", 
                "Moduli di memoria DDR4 da 16GB. Forniscono la memoria temporanea necessaria per l'esecuzione dei programmi.",
                ResourceLoader.IMG_PATH.resolve("RAM.png").toString());
        ram.setAlias(new String[]{"memoria", "ddr4", "dimm", "stick"});
        ram.setPickupable(true);
        
        // Scheda madre - Collegamento di tutti i componenti
        AdvObject schedaMadre = new AdvObject(Utils.OBJ_SCHEDA_MADRE_ID, "Scheda madre", 
                "La scheda madre principale che collegherà tutti i componenti del sistema. Ha socket per CPU, slot RAM e connettori vari.",
                ResourceLoader.IMG_PATH.resolve("SchedaMadre.png").toString());
        schedaMadre.setAlias(new String[]{"motherboard", "mainboard", "scheda", "piastra"});
        schedaMadre.setPickupable(true);
        
        // Alimentatore - Fornisce energia elettrica
        AdvObject alimentatore = new AdvObject(Utils.OBJ_ALIMENTATORE_ID, "Alimentatore", 
                "Un alimentatore da 650W modulare. Convertirà la corrente alternata in continua per alimentare tutti i componenti.",
                ResourceLoader.IMG_PATH.resolve("Alimentatore.png").toString());
        alimentatore.setAlias(new String[]{"psu", "power", "supply", "trasformatore"});
        alimentatore.setPickupable(true);
        
        // Pasta termica - Per il raffreddamento CPU
        AdvObject pastaTermica = new AdvObject(Utils.OBJ_PASTA_TERMICA_ID, "Pasta termica", 
                "Pasta termoconduttiva di alta qualità. Essenziale per trasferire il calore dalla CPU al dissipatore.",
                ResourceLoader.IMG_PATH.resolve("PastaTermica.png").toString());
        pastaTermica.setAlias(new String[]{"pasta", "termica", "compound", "grasso"});
        pastaTermica.setPickupable(true);
        
        // Dissipatore - Sistema di raffreddamento
        AdvObject dissipatore = new AdvObject(Utils.OBJ_DISSIPATORE_ID, "Dissipatore", 
                "Un dissipatore a torre con ventola da 120mm. Manterrà la CPU a temperature operative sicure.",
                ResourceLoader.IMG_PATH.resolve("Dissipatore.png").toString());
        dissipatore.setAlias(new String[]{"cooler", "ventola", "radiatore", "heatsink"});
        dissipatore.setPickupable(true);
        
        // GPU - Scheda grafica
        AdvObject gpu = new AdvObject(Utils.OBJ_GPU_ID, "GPU", 
                "Una scheda grafica NVIDIA RTX di fascia alta. Gestirà rendering grafico e calcoli paralleli avanzati.",
                ResourceLoader.IMG_PATH.resolve("GPU.png").toString());
        gpu.setAlias(new String[]{"scheda grafica", "video card", "nvidia", "rtx"});
        gpu.setPickupable(true);

        // Poster informativo sulle frequenze operative dei robot
        AdvObject poster = new AdvObject(Utils.OBJ_POSTER_ID, "Poster",
                "\n[NEON_ORANGE]=== FREQUENZE ===[/]\n\n" +
                "[YELLOW]FREQUENZE OPERATIVE ROBOT:[/]\n" +
                "• [GREEN]433.92 MHz[/] - Canale comando ricarica\n" +
                "• [ORANGE]868.0 MHz[/] - Canale override sistema\n" +
                "• [BLUE]915.0 MHz[/] - Canale arresto temporaneo\n\n" +
                "[RED]ATTENZIONE:[/] Utilizzare solo in caso di emergenza!\n" +
                "Formato comando: [frequenza] [comando]");
        poster.setAlias(new String[]{"poster","foglio"});
        poster.setPickupable(false);

        // -- NPC LIvello 2 --

        // DIRETTORE NEL LABORATORIO
        AdvNPC direttore = new AdvNPC(Utils.NPC_DIRETTORE_LAB_ID, "Direttore", "Il direttore del collegio, sembra stia attendendo il tuo arrivo");
        direttore.setImagePath(ResourceLoader.IMG_PATH.resolve("DirettoreLab.png").toString());
        direttore.setAlias(new String[]{"direttore", "michele", "dottore"});
        direttore.addFirstDialogueLine(playerName + ", finalmente sei arrivato!");
        direttore.addFirstDialogueLine("La seconda prova consiste nell'assemblaggio di un pc desktop");
        direttore.addFirstDialogueLine("Per fare ciò, dovrai recuperare tutti i componenti necessari\n e assemblarli nell'ordine corretto");
        direttore.addFirstDialogueLine("Nello specifico, i componenti che ti serviranno sono:");
        direttore.addFirstDialogueLine("- [RED]Case pc, scheda madre, RAM, SSD, CPU, pasta termica, dissipatore, GPU e alimentatore[/]");
        direttore.addFirstDialogueLine("Dopo aver assemblato il pc, dovrai accenderlo per completare la prova");
        direttore.addFirstDialogueLine("In fondo al corridoio del laboratorio trovarai già il case del PC vuoto,\n nel quale dovrai inserire tutti i componenti");
        direttore.addFirstDialogueLine("Il primo componente te lo fornisco io, gli altri dovrai recuperarli in giro per il collegio");
        direttore.addSubsequentDialogueLine(playerName + ", hai già recuperato tutte le compenenti?");
        direttore.addItemToGive(schedaMadre);

        //PINO, il manutentore del collegio
        AdvNPC pino = new AdvNPC(Utils.NPC_PINO_ID, "Pino", "Il manutentore del collegio, sembra avere il cancro ai polmoni per quanto tossisce");
        pino.setImagePath(ResourceLoader.IMG_PATH.resolve("Manutentore.png").toString());
        pino.setAlias(new String[]{"pino", "manutentore"});
        pino.addFirstDialogueLine("Buongiorno, tu dovresti essere il nuovo canditato, vero?");
        pino.addFirstDialogueLine("Io sono Pino, il manutentore del collegio.");
        pino.addFirstDialogueLine("In giro per questo piano trovarai tutti gli strumenti che ti servono per la prova tecnica.");
        pino.addFirstDialogueLine("Nel mentre, io vado a fumare una sigaretta, il direttore oggi mi ha rotto i coglioni più del solito.");
        pino.addSubsequentDialogueLine("Cof Cof, che schifo il fumo, ma che ci vuoi fare, la vita è questa..");
        
        //LUIGI D'ORONZO
        AdvNPC luigi = new AdvNPC(Utils.NPC_LUIGI_ID, "Luigi", "Un collegiale molto particolare, sembra essere un esperto di elettronica");
        luigi.setImagePath(ResourceLoader.IMG_PATH.resolve("Scimmia.png").toString());
        luigi.setAlias(new String[]{"luigi", "scimmia", "bom"});
        luigi.addFirstDialogueLine("Ciao, sono Luigi, ma i miei amici mi chiamano Bom.");
        luigi.addFirstDialogueLine("Non ti preoccupare, non sono un terrorista, mi piace solo far esplodere i condensatori!");
        luigi.addFirstDialogueLine("Per la tua prova, ti consiglio di dare un occhiata in questo laboratorio,\npotresti trovare oggetti utili");
        luigi.addSubsequentDialogueLine("Vuoi sapere un segreto? Ho un dispositivo che può far esplodere i condensatori a distanza!");
        luigi.addSubsequentDialogueLine("Ma non ti preoccupare, non succede niente...");

        // DOTTOR BUURDOH
        AdvNPC lorenzo = new AdvNPC(Utils.NPC_LORENZO_ID, "Lorenzo Burdo", "Il vicedirettore del collegio, sembra essere un tipo molto strano");
        lorenzo.setImagePath(ResourceLoader.IMG_PATH.resolve("Burdo.png").toString());
        lorenzo.setAlias(new String[]{"burdo", "lorenzo", "vicedirettore"});
        lorenzo.addFirstDialogueLine("We we che piacere rivederti !");
        lorenzo.addFirstDialogueLine("Sto cercando il mio giaccone, forse è nell'armadio");
        lorenzo.addSubsequentDialogueLine("Lo sai che Napoli è la città più bella del mondo? Io ci ho vissuto 40 anni!");
        lorenzo.addSubsequentDialogueLine("Ma ora sono qui, al collegio, a fare il vicedirettore. Che vita triste...");
        lorenzo.addSubsequentDialogueLine("Comunque, viva San Gennero e Forza Napoli!"); 

        // RECUPERO STANZE per posizionamento oggetti
        Room craftRoom = gameDescription.getGameMap().findRoomById(Utils.ROOM_CRAFT_ROOM_ID);
        Room electronicsLab = gameDescription.getGameMap().findRoomById(Utils.ROOM_ELECTRONICS_LAB_ID);
        Room lab3D = gameDescription.getGameMap().findRoomById(Utils.ROOM_LAB3D_ID);
        Room lab5 = gameDescription.getGameMap().findRoomById(Utils.ROOM_LAB5_ID);
        Room entryLab = gameDescription.getGameMap().findRoomById(Utils.ROOM_ENTRY_LAB_ID);
        Room corridorLab = gameDescription.getGameMap().findRoomById(Utils.ROOM_CORRIDOR_LAB_ID);
        Room hall = gameDescription.getGameMap().findRoomById(Utils.ROOM_HALL_ID);

        // -- POSIZIONAMENTO NPC --
        craftRoom.addObject(pino, null);
        electronicsLab.addObject(luigi, null);
        entryLab.addObject(direttore, null);
        hall.addObject(lorenzo, null);

        // -- POSIZIONAMENTO OGGETTI --
        corridorLab.addObject(casePc, null);
        lab5.addObject(cpu, "Un processore, sembra essere in buone condizioni");
        craftRoom.addObject(ram, "Un modulo di RAM DDR4, sembra essere in buone condizioni");
        craftRoom.addObject(alimentatore, "Un alimentatore da 650W, sembra essere funzionante");
        electronicsLab.addObject(pastaTermica, "Una siringa di pasta termica, sembra essere nuovo");
        lab3D.addObject(dissipatore, "Un dissipatore a torre con ventola");
        lab5.addObject(gpu, "Una scheda grafica NVIDIA, sembra adatta per il tuo PC");
        lab5.addObject(poster, null);
        
    }

    /**
     * Verifica completamento livello: controllo possesso oggetto per lo switch di stato.
     */
    @Override
    public boolean isCompleted(GameDescription game) {
        // Trova il case PC nella mappa
        AdvObjectContainer casePc = findCasePcInMap(game);
        if (casePc == null || !casePc.isOpen()) {
            return false;
        }
        
        // Verifica assemblaggio completo e corretto utilizzando l'helper
        if (PcAssemblyHelper.isPcCorrectlyAssembled(casePc)) {
            // Controlla se il PC è stato usato (oggetto completamento presente)
            boolean hasUsedPc = game.getInventory().stream()
                .anyMatch(obj -> obj.getId() == Utils.OBJ_LEVEL2_COMPLETE_ID);
                
            return hasUsedPc; // Completato solo se ha usato il PC
        }
        
        return false;
    }

    private AdvObjectContainer findCasePcInMap(GameDescription game) {
        // Cerca il case PC in tutte le stanze di tutti i piani
        return game.getGameMap().getAllFloors().stream()
            .flatMap(List::stream) // Flattena tutti i piani in un unico stream di stanze
            .flatMap(room -> room.getObjects().stream()) // Flattena tutti gli oggetti delle stanze
            .filter(obj -> obj.getId() == Utils.OBJ_CASE_PC_ID && obj instanceof AdvObjectContainer)
            .map(obj -> (AdvObjectContainer) obj)
            .findFirst()
            .orElse(null);
    }

    
    /**
     * Verifica fallimento: possesso di componenti vietati o incompatibili.
     */
    @Override
    public boolean isFailureConditionMet(GameDescription game) {
        return game.getInventory().stream()
                   .mapToInt(obj -> obj.getId())
                   .boxed()
                   .collect(java.util.stream.Collectors.toSet())
                   .stream()
                   .anyMatch(id -> this.getForbidenIDObjects().contains(id));
    }
    
    /** Esegue callback per completamento del gioco o transizione livello */
    @Override
    public void handleSuccess(Runnable onSuccess) {

        Room entry = gameDescription.getGameMap().findRoomById(Utils.ROOM_ENTRY_ID);
        Room entryLab = gameDescription.getGameMap().findRoomById(Utils.ROOM_ENTRY_LAB_ID);
        Room electronicsLab = gameDescription.getGameMap().findRoomById(Utils.ROOM_ELECTRONICS_LAB_ID);

        entry.removeObject(Utils.NPC_GUIDO_ID);
        entryLab.removeObject(Utils.NPC_DIRETTORE_LAB_ID);
        electronicsLab.removeObject(Utils.NPC_LUIGI_ID);


        onSuccess.run();
    }
    
    /** Esegue callback per gestione fallimento */
    @Override
    public void handleFailure(Runnable onFailure) {
        onFailure.run();
    }
    
    @Override
    public String getLevelName() {
        return "Prova Tecnica - Livello 2";
    }    /**
     * Mostra briefing della prova tecnica con istruzioni di Lorenzo Burdo.
     * Include lista componenti richiesti e comando speciale di assemblaggio.
     */
    @Override
    public void getLevelDescription(OutputHandler output, String playerName, String remainingTime) {
        output.writeln("\n" + "=".repeat(60), ColorText.LIGHT_ORANGE);
        output.writeln("LIVELLO 2 - PROVA TECNICA", ColorText.LIGHT_ORANGE);
        output.writeln("=".repeat(60), ColorText.LIGHT_ORANGE);
        output.writeln("IMAGE:" + ResourceLoader.IMG_PATH.resolve("Burdo_direttore.png").toString());
        output.writeln("\nVedi il Direttore che inzia a correre verso la porta... sembra che non stia molto bene...\n");
        output.writeln("[NPC]Lorenzo Burdo[/]: \"Weee! Ma che piacere conoscere il candidato di oggi!!\"");
        output.writeln("[NPC]Lorenzo Burdo[/]: \"Io sono Lorenzo Burdo, il vicedirettore del collegio\"");
        output.writeln("[NPC]Lorenzo Burdo[/]: \"Ehiii, ma hai tu la mia penna ! Ridammela subito !\"");
        output.writeln("[NPC]Lorenzo Burdo[/]: \"Bene, ora che abbiamo risolto questo piccolo inconveniente, passiamo alla seconda prova !\"");
        output.writeln("[NPC]Lorenzo Burdo[/]: \"Dirigiti in laboratorio, li troverai il Direttore il quale ti darà ulteriori informazioni a riguardo\"");
        output.writeln("[NPC]Lorenzo Burdo[/]: \"Hai [RED]" + remainingTime + "[/] minuti per completare la prova, ma tu stai senza pensieri!\"");
        output.writeln("=".repeat(60) + "\n", ColorText.LIGHT_ORANGE);
    }
}