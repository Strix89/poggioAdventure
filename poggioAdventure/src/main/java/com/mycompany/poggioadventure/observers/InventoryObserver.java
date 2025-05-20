package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
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
     * @param output L'handler per l'output
     * @return Stringa vuota per GUI, descrizione testuale per CLI
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, OutputHandler output) {
        StringBuilder msg = new StringBuilder();
        
        if (parserOutput.getCommand().getType() == CommandType.INVENTORY) {
            if (output instanceof GUIOutputHandler) {
                // Versione GUI: Mostra/Aggiorna la finestra dell'inventario
                handleGUIInventory(description);
                return ""; // Non è necessario restituire una stringa per la GUI
            } else {
                // Versione CLI: Restituisce la descrizione testuale dell'inventario
                if (description.getInventory().isEmpty()) {
                    msg.append("Il tuo inventario è vuoto!");
                } else {
                    msg.append("Nel tuo inventario ci sono:\n");
                    for (AdvObject o : description.getInventory()) {
                        msg.append("[yellow]").append(o.getName()).append("[/]").append(": ").append(o.getDescription()).append("\n");
                    }
                }
            }
        }
        return msg.toString();
    }
    
    /**
     * Gestisce la visualizzazione dell'inventario per interfacce GUI
     * Crea una finestra se non esiste, altrimenti la porta in primo piano
     * Configura un timer per l'aggiornamento automatico
     * 
     * @param description La descrizione del gioco
     */
    private void handleGUIInventory(GameDescription description) {
        this.gameDescription = description; // Salva il riferimento alla descrizione del gioco
        
        if (inventoryWindow == null || !inventoryWindow.isVisible()) {
            // Crea una nuova finestra dell'inventario
            inventoryWindow = new UI_Inventory();
            inventoryWindow.addObjectsToScroller(description.getInventory());
            
            // Aggiunge un listener per sapere quando la finestra viene chiusa
            inventoryWindow.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    stopUpdateTimer(); // Ferma il timer quando la finestra viene chiusa
                    inventoryWindow = null;
                }
            });
            
            // Avvia il timer per l'aggiornamento automatico
            startUpdateTimer();
            
            // Mostra la finestra
            inventoryWindow.setVisible(true);
        } else {
            // La finestra esiste già, la porta in primo piano
            inventoryWindow.toFront();
            
            // Assicura che il timer sia attivo
            if (updateTimer == null || !updateTimer.isRunning()) {
                startUpdateTimer();
            }
        }
    }
    
    /**
     * Avvia il timer per l'aggiornamento automatico dell'inventario
     */
    private void startUpdateTimer() {
        // Crea e avvia il timer solo se non esiste già
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
            // Crea una copia della lista per evitare problemi di concorrenza
            List<AdvObject> currentInventory = new ArrayList<>(gameDescription.getInventory());
            inventoryWindow.addObjectsToScroller(currentInventory);
        } else {
            // Se la finestra è stata chiusa o non è più visibile, ferma il timer
            stopUpdateTimer();
        }
    }
}