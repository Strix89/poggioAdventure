package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.abstracts.GameState;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.Room;
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
        Room hallway = gameDescription.getGameMap().findRoomById(Utils.ROOM_HALLWAY_ID);
        hallway.setForbidden(false);
        
        // Continua dalla posizione corrente invece di forzare spostamento
        this.setStartingRoom(gameDescription.getCurrentRoom());
        
        output.writeln("Sembra che il disimpegno si sia aperto", ColorText.PLUM);
    }

    /**
     * Verifica completamento: possesso di tutti i componenti richiesti per l'assemblaggio.
     */
    @Override
    public boolean isCompleted(GameDescription game) {
        return game.getInventory().stream()
            .mapToInt(obj -> obj.getId())
            .boxed()
            .collect(java.util.stream.Collectors.toSet())
            .containsAll(this.getRequiredIDObjects());
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
    }

    /**
     * Mostra briefing della prova tecnica con istruzioni del Professor Rossi.
     * Include lista componenti richiesti e comando speciale di assemblaggio.
     */
    @Override
    public void getLevelDescription(OutputHandler output, String playerName, String remainingTime) {
        output.writeln("\n" + "=".repeat(60), ColorText.LIGHT_ORANGE);
        output.writeln("LIVELLO 2 - PROVA TECNICA", ColorText.LIGHT_ORANGE);
        output.writeln("=".repeat(60), ColorText.LIGHT_ORANGE);
        output.writeln("[NPC]Professor Rossi[/]: \"Benvenuto nel laboratorio elettronico, " + playerName + "!\"");
        output.writeln("[NPC]Professor Rossi[/]: \"Devi assemblare un dispositivo IoT utilizzando i componenti del laboratorio.\"");
        output.writeln("[NPC]Professor Rossi[/]: \"Ti servono: microcontrollore, sensori, resistenze, breadboard e cavi.\"");
        output.writeln("[NPC]Professor Rossi[/]: \"Usa il comando 'assembla dispositivo' quando hai tutti i componenti.\"");
        output.writeln("[NPC]Professor Rossi[/]: \"Hai [RED]" + remainingTime + "[/] minuti per completare la prova.\"");
        output.writeln("[NPC]Professor Rossi[/]: \"Successivamente dovrai sostenere un breve test tecnico.\"");
        output.writeln("=".repeat(60) + "\n", ColorText.LIGHT_ORANGE);
    }
}