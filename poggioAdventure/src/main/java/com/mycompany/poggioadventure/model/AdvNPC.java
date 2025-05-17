package com.mycompany.poggioadventure.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un NPC (Personaggio Non Giocante) nell'avventura. Un NPC può avere dialoghi e oggetti da dare al giocatore.
 * La classe estende {@link AdvObject} per ereditare le proprietà comuni di un oggetto.
 */
public class AdvNPC extends AdvObject {
    
    /** Elenco delle righe di dialogo che l'NPC dice la prima volta che viene interagito. */
    private final List<String> firstDialogue = new ArrayList<>();
    
    /** Elenco delle righe di dialogo che l'NPC dice dopo essere stato interagito. */
    private final List<String> subsequentDialogue = new ArrayList<>();
    
    /** Indica se l'NPC è stato interagito dal giocatore. */
    private boolean hasInteracted = false;
    
    /** Elenco degli oggetti che l'NPC può dare al giocatore. */
    private final List<AdvObject> itemsToGive = new ArrayList<>();

    /**
     * Costruisce un nuovo NPC con un ID, un nome e una descrizione.Inizializza la proprietà pickupable a false, in quanto l'NPC non è prelevabile.
     * 
     * @param id L'ID dell'NPC.
     * @param name Il nome dell'NPC.
     * @param description La descrizione dell'NPC.
     * @param imagePathForNPC
     */
    public AdvNPC(int id, String name, String description, String imagePathForNPC) {
        super(id, name, imagePathForNPC, description);
        setPickupable(false); // L'NPC non può essere prelevato
    }
    
    public AdvNPC(int id, String name, String description) {
        super(id, name, description);
        setPickupable(false); // L'NPC non può essere prelevato
    }


    /**
     * Aggiunge una riga di dialogo da parte dell'NPC che viene visualizzata la prima volta che il giocatore interagisce con lui.
     * 
     * @param line La riga di dialogo da aggiungere.
     */
    public void addFirstDialogueLine(String line) {
        firstDialogue.add(line);
    }

    /**
     * Aggiunge una riga di dialogo da parte dell'NPC che viene visualizzata dopo la prima interazione.
     * 
     * @param line La riga di dialogo da aggiungere.
     */
    public void addSubsequentDialogueLine(String line) {
        subsequentDialogue.add(line);
    }

    /**
     * Restituisce le righe di dialogo da visualizzare, a seconda che l'NPC sia stato interagito o meno.
     * Se l'NPC è stato interagito, vengono restituiti i dialoghi successivi; altrimenti, quelli iniziali.
     * 
     * @return La lista di righe di dialogo appropriate.
     */
    public List<String> getDialogue() {
        return hasInteracted ? subsequentDialogue : firstDialogue;
    }

    /**
     * Aggiunge un oggetto che l'NPC può dare al giocatore.
     * 
     * @param item L'oggetto da aggiungere alla lista.
     */
    public void addItemToGive(AdvObject item) {
        itemsToGive.add(item);
    }

    /**
     * Restituisce l'elenco degli oggetti che l'NPC può dare al giocatore.
     * 
     * @return La lista degli oggetti che l'NPC può dare.
     */
    public List<AdvObject> getItemsToGive() {
        return itemsToGive;
    }

    /**
     * Verifica se l'NPC è stato interagito dal giocatore.
     * 
     * @return {@code true} se l'NPC è stato interagito, altrimenti {@code false}.
     */
    public boolean hasInteracted() {
        return hasInteracted;
    }

    /**
     * Imposta lo stato dell'interazione dell'NPC con il giocatore.
     * 
     * @param hasInteracted {@code true} se l'NPC è stato interagito, {@code false} altrimenti.
     */
    public void setHasInteracted(boolean hasInteracted) {
        this.hasInteracted = hasInteracted;
    }
}
