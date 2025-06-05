package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.CommandType;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.gui.GUIOutputHandler;
import com.mycompany.poggioadventure.ui.gui.views.UI_Inventory;
import java.io.Serializable;
import javax.swing.Timer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Metodo che permette di visualizzare l'inventario del giocatore
 * Observer verifica che il comando sia di tipo INVENTORY e restituisce la lista degli oggetti presenti nell'inventario
 * Implementa GameObserver e aggiorna la descrizione del gioco
 * 
 * Supporta aggiornamento automatico ogni 5 secondi per le interfacce GUI.
 */
public class InventoryObserver implements GameObserver, Serializable {

    private transient UI_Inventory inventoryWindow; // Riferimento alla finestra dell'inventario
    private transient Timer updateTimer; // Timer per l'aggiornamento automatico
    private static final int UPDATE_INTERVAL = 5000; // 5 secondi in millisecondi
    private transient GameDescription gameDescription; // Riferimento alla descrizione del gioco

    /**
     * Metodo che permette di visualizzare l'inventario del giocatore
     * Observer verifica che il comando sia di tipo INVENTORY e restituisce la lista degli oggetti presenti nell'inventario
     * Per interfacce GUI, viene mostrata una finestra grafica che si aggiorna automaticamente
     * Per interfacce CLI, viene restituita una descrizione testuale
     * 
     * @param description La descrizione del gioco
     * @param parserOutput L'output del parser
     * @param gameContext Il contesto del gioco
     * @return Stringa vuota per GUI, descrizione testuale per CLI
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();
        OutputHandler output = gameContext.getOutputHandler();
        
        if (parserOutput.getCommand().getType() == CommandType.INVENTORY) {
            if (output instanceof GUIOutputHandler) {
                // Versione GUI: Mostra/Aggiorna la finestra dell'inventario
                handleGUIInventory(description);
                return "";
            } else {
                // Versione CLI: Restituisce la descrizione testuale dell'inventario
                List<AdvObject> visibleInventory = getVisibleInventory(description.getInventory());
                return buildCLIInventoryMessage(visibleInventory);
            }
        }
        return msg.toString();
    }
    
    /**
     * Costruisce il messaggio dell'inventario per CLI usando lambda
     * @param visibleInventory Lista degli oggetti visibili
     * @return Stringa formattata per l'inventario
     */
    private String buildCLIInventoryMessage(List<AdvObject> visibleInventory) {
        if (visibleInventory.isEmpty()) {
            return "Il tuo inventario è vuoto!";
        }
        
        String inventoryContent = visibleInventory.stream()
            .map(obj -> "-[ITEM]" + obj.getName() + "[/]: " + obj.getDescription())
            .collect(java.util.stream.Collectors.joining("\n"));
        
        return "Nel tuo inventario ci sono:\n" + inventoryContent;
    }
    
    /**
     * Gestisce la visualizzazione dell'inventario per interfacce GUI
     * Crea una finestra se non esiste, altrimenti la porta in primo piano
     * Configura un timer per l'aggiornamento automatico
     * 
     * @param description La descrizione del gioco
     */
    private void handleGUIInventory(GameDescription description) {
        this.gameDescription = description;
        
        if (isInventoryWindowClosed()) {
            createAndShowInventoryWindow(description);
        } else {
            bringInventoryToFront();
        }
    }
    
    /**
     * Verifica se la finestra dell'inventario è chiusa o non visibile
     * @return true se la finestra è chiusa
     */
    private boolean isInventoryWindowClosed() {
        return inventoryWindow == null || !inventoryWindow.isVisible();
    }
    
    /**
     * Crea e mostra una nuova finestra dell'inventario
     * @param description La descrizione del gioco
     */
    private void createAndShowInventoryWindow(GameDescription description) {
        List<AdvObject> visibleInventory = getVisibleInventory(description.getInventory());
        
        inventoryWindow = new UI_Inventory();
        inventoryWindow.addObjectsToScroller(visibleInventory);
        
        // Aggiunge un listener lambda per la chiusura della finestra
        inventoryWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                cleanupInventoryWindow();
            }
        });
        
        startUpdateTimer();
        inventoryWindow.setVisible(true);
    }
    
    /**
     * Porta la finestra dell'inventario in primo piano
     */
    private void bringInventoryToFront() {
        inventoryWindow.toFront();
        ensureTimerIsRunning();
    }
    
    /**
     * Pulisce le risorse quando la finestra viene chiusa
     */
    private void cleanupInventoryWindow() {
        stopUpdateTimer();
        inventoryWindow = null;
    }
    
    /**
     * Assicura che il timer sia in esecuzione
     */
    private void ensureTimerIsRunning() {
        if (updateTimer == null || !updateTimer.isRunning()) {
            startUpdateTimer();
        }
    }
    
    /**
     * Avvia il timer per l'aggiornamento automatico dell'inventario
     */
    private void startUpdateTimer() {
        if (updateTimer == null || !updateTimer.isRunning()) {
            updateTimer = new Timer(UPDATE_INTERVAL, e -> updateInventory());
            updateTimer.start();
        }
    }
    
    /**
     * Ferma il timer di aggiornamento
     */
    private void stopUpdateTimer() {
        if (updateTimer != null && updateTimer.isRunning()) {
            updateTimer.stop();
            updateTimer = null;
        }
    }
    
    /**
     * Aggiorna la lista degli oggetti nell'inventario
     * Chiamato periodicamente dal timer
     */
    private void updateInventory() {
        if (inventoryWindow != null && inventoryWindow.isVisible() && gameDescription != null) {
            List<AdvObject> visibleInventory = getVisibleInventory(gameDescription.getInventory());
            inventoryWindow.addObjectsToScroller(new ArrayList<>(visibleInventory));
        } else {
            stopUpdateTimer();
        }
    }

    /**
     * Filtra l'inventario per ottenere solo gli oggetti visibili usando Stream API
     * @param inventory Lista completa dell'inventario
     * @return Lista degli oggetti visibili
     */
    private List<AdvObject> getVisibleInventory(List<AdvObject> inventory) {
        return inventory.stream()
                .filter(AdvObject::isVisible)
                .collect(java.util.stream.Collectors.toList());
    }
}