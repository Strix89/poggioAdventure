package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.abstracts.GameState;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;

import java.util.List;

/**
 * Implementazione del secondo livello del gioco - Prova Tecnica.
 * 
 * <p>Il giocatore deve dimostrare le sue competenze tecniche nel laboratorio
 * per essere ammesso al collegio. Deve assemblare un dispositivo elettronico
 * utilizzando i componenti trovati nei laboratori.
 * 
 * @author Strix89
 */
public class Level2State extends GameState {

    public Level2State(long timeLimit, List<Integer> requiredObjects, List<Integer> forbiddenObjects) {
        super(timeLimit, requiredObjects, forbiddenObjects);
    }

    public Level2State(long timeLimit, Room startingRoom, List<Integer> requiredObjects, List<Integer> forbiddenObjects) {
        super(timeLimit, startingRoom, requiredObjects, forbiddenObjects);
    }
    
    @Override
    public void enter(GameDescription gameDescription, OutputHandler output, String playerName) {
        Room hallway = gameDescription.getGameMap().findRoomById(Utils.ROOM_HALLWAY_ID);
        hallway.setForbidden(false);
        this.setStartingRoom(gameDescription.getCurrentRoom()); // Riga importante per settare come stanza iniziale del livello quella corrente in cui si trova il giocatore, volendo si può decidere arbitrariamente la stanza iniziale.
        output.writeln("Sembra che il disimpegno si sia aperto", ColorText.PLUM);
    }

    @Override
    public boolean isCompleted(GameDescription game) {
        // Verifica se l'oggetto di completamento è nell'inventario
        return game.getInventory().stream()
            .mapToInt(obj -> obj.getId())
            .boxed()
            .collect(java.util.stream.Collectors.toSet())
            .containsAll(this.getRequiredIDObjects());
    }
    
    @Override
    public boolean isFailureConditionMet(GameDescription game) {
        // Verifica se ci sono oggetti proibiti nell'inventario
        return game.getInventory().stream()
                   .mapToInt(obj -> obj.getId())
                   .boxed()
                   .collect(java.util.stream.Collectors.toSet())
                   .stream()
                   .anyMatch(id -> this.getForbidenIDObjects().contains(id));
    }
    
    @Override
    public void handleSuccess(Runnable onSuccess) {
        onSuccess.run();
    }
    
    @Override
    public void handleFailure(Runnable onFailure) {
        onFailure.run();
    }
    
    @Override
    public String getLevelName() {
        return "Prova Tecnica - Livello 2";
    }

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