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
 * Observer specializzato per gestione comando inventario con supporto multi-interfaccia.
 * 
 * <p>Implementa visualizzazione adaptive dell'inventario giocatore basata sul tipo
 * di interfaccia utente. Per GUI fornisce finestra dedicata con aggiornamento
 * automatico, per CLI restituisce output testuale formattato.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Rilevamento automatico tipo interfaccia (GUI vs CLI)</li>
 *   <li>Finestra GUI con aggiornamento periodico (5s)</li>
 *   <li>Filtering oggetti visibili con Stream API</li>
 *   <li>Gestione lifecycle finestra con cleanup automatico</li>
 *   <li>Serializzazione con campi transient per componenti UI</li>
 * </ul>
 * 
 * <p><b>Pattern implementati:</b>
 * <ul>
 *   <li>Observer: reazione a comando INVENTORY</li>
 *   <li>Strategy: rendering differenziato per tipo interfaccia</li>
 *   <li>Singleton Window: gestione istanza unica finestra</li>
 * </ul>
 */
public class InventoryObserver implements GameObserver, Serializable {

    /** Riferimento finestra inventario GUI (transient per serializzazione) */
    private transient UI_Inventory inventoryWindow;
    
    /** Timer per aggiornamento automatico finestra GUI */
    private transient Timer updateTimer;
    
    /** Intervallo aggiornamento automatico in millisecondi */
    private static final int UPDATE_INTERVAL = 5000;
    
    /** Riferimento stato gioco per aggiornamenti periodici */
    private transient GameDescription gameDescription;

    /**
     * Gestisce comando INVENTORY con rendering adaptive per tipo interfaccia.
     * GUI: apre/aggiorna finestra dedicata con timer automatico.
     * CLI: restituisce output testuale formattato con markup.
     * 
     * @param description Stato corrente mondo di gioco
     * @param parserOutput Comando parsato con tipo e parametri
     * @param gameContext Contesto esecuzione con handler I/O
     * @return Stringa vuota per GUI, descrizione formattata per CLI
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
     * Genera output CLI formattato per inventario utilizzando Stream API.
     * Applica markup colori per migliorare leggibilità.
     * 
     * @param visibleInventory Lista oggetti visibili al giocatore
     * @return Stringa formattata con elenco oggetti e descrizioni
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
     * Orchestrazione gestione finestra GUI con pattern Singleton.
     * Crea nuova finestra se necessario o porta esistente in primo piano.
     * 
     * @param description Stato gioco per popolazione iniziale
     */
    private void handleGUIInventory(GameDescription description) {
        this.gameDescription = description;
        
        if (isInventoryWindowClosed()) {
            createAndShowInventoryWindow(description);
        } else {
            bringInventoryToFront();
        }
    }
    
    /** Verifica stato finestra per decisioni di creazione/riutilizzo */
    private boolean isInventoryWindowClosed() {
        return inventoryWindow == null || !inventoryWindow.isVisible();
    }
    
    /**
     * Factory method per creazione finestra con configurazione completa.
     * Inizializza contenuto, listener cleanup e timer aggiornamento.
     * 
     * @param description Stato gioco per popolazione contenuti
     */
    private void createAndShowInventoryWindow(GameDescription description) {
        List<AdvObject> visibleInventory = getVisibleInventory(description.getInventory());
        
        inventoryWindow = new UI_Inventory();
        inventoryWindow.addObjectsToScroller(visibleInventory);
        
        // Configurazione cleanup automatico alla chiusura
        inventoryWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                cleanupInventoryWindow();
            }
        });
        
        startUpdateTimer();
        inventoryWindow.setVisible(true);
    }
    
    /** Porta finestra esistente in primo piano e assicura timer attivo */
    private void bringInventoryToFront() {
        inventoryWindow.toFront();
        ensureTimerIsRunning();
    }
    
    /** Cleanup risorse alla chiusura finestra per prevenire memory leak */
    private void cleanupInventoryWindow() {
        stopUpdateTimer();
        inventoryWindow = null;
    }
    
    /** Verifica e avvio timer se non già in esecuzione */
    private void ensureTimerIsRunning() {
        if (updateTimer == null || !updateTimer.isRunning()) {
            startUpdateTimer();
        }
    }
    
    /**
     * Inizializzazione timer per aggiornamento periodico contenuti.
     * Previene creazione timer multipli con controllo stato.
     */
    private void startUpdateTimer() {
        if (updateTimer == null || !updateTimer.isRunning()) {
            updateTimer = new Timer(UPDATE_INTERVAL, e -> updateInventory());
            updateTimer.start();
        }
    }
    
    /** Terminazione timer con cleanup riferimenti */
    private void stopUpdateTimer() {
        if (updateTimer != null && updateTimer.isRunning()) {
            updateTimer.stop();
            updateTimer = null;
        }
    }
    
    /**
     * Callback timer per refresh contenuti finestra.
     * Auto-terminazione se finestra chiusa per efficienza risorse.
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
     * Filtering oggetti inventario con Stream API per visibilità.
     * Ottimizzazione per non mostrare oggetti sistema o nascosti.
     * 
     * @param inventory Lista completa inventario giocatore
     * @return Lista filtrata oggetti visibili
     */
    private List<AdvObject> getVisibleInventory(List<AdvObject> inventory) {
        return inventory.stream()
                .filter(AdvObject::isVisible)
                .collect(java.util.stream.Collectors.toList());
    }
}