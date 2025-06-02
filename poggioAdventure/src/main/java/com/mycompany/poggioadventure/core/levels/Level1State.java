package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.abstracts.GameState;
import com.mycompany.poggioadventure.model.Room;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.OutputHandler;

import java.util.Arrays;
import java.util.List;

/**
 * Implementazione del primo livello del gioco.
 */
public class Level1State implements GameState {
    
    private static final long TIME_LIMIT = 5 * 60 * 1000; // 5 minuti in millisecondi
    private static final List<Integer> REQUIRED_OBJECTS = Arrays.asList(1, 2, 3); // IDs oggetti richiesti
    
    @Override
    public void enter(GameDescription gameDescription, OutputHandler output) {
        output.writeln("📚 Benvenuto nel Livello 1: Tutorial di Java!", ColorText.CYAN);
        output.writeln("🎯 Obiettivo: Raccogli tutti gli oggetti necessari per il test!", ColorText.YELLOW);
        output.writeln("⚠️ Attenzione: Hai solo 5 minuti per completare questo livello!", ColorText.WARNING);
        
        // Inizializza elementi specifici del livello se necessario
        // Ad esempio: spawna oggetti, configura NPC, ecc.
    }
    
    @Override
    public Room getStartingRoom() {
        // Restituisce la stanza iniziale del primo livello
        // Dovrai implementare questo metodo basandoti sulla tua GameMap
        return null; // TODO: implementare recupero stanza dal GameMap
    }
    
    @Override
    public List<Integer> getRequiredObjects() {
        return REQUIRED_OBJECTS;
    }
    
    @Override
    public boolean isCompleted(GameDescription game) {
        // Verifica se tutti gli oggetti richiesti sono nell'inventario
        return game.getInventory().stream()
            .mapToInt(obj -> obj.getId())
            .boxed()
            .collect(java.util.stream.Collectors.toSet())
            .containsAll(REQUIRED_OBJECTS);
    }
    
    @Override
    public boolean isFailureConditionMet(GameDescription game, long elapsedTime) {
        // Fallimento se il tempo è scaduto o altre condizioni specifiche del livello
        return elapsedTime >= TIME_LIMIT;
    }
    
    @Override
    public void handleSuccess(Runnable onSuccess) {
        // Esegue il callback per la transizione al livello successivo
        onSuccess.run();
    }
    
    @Override
    public void handleFailure(FailureType failureType, Runnable onFailure) {
        // Esegue il callback appropriato per il tipo di fallimento
        onFailure.run();
    }
    
    @Override
    public String getLevelName() {
        return "Tutorial Java - Livello 1";
    }
    
    @Override
    public long getTimeLimit() {
        return TIME_LIMIT;
    }
}