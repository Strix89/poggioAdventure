package com.mycompany.poggioadventure.model;

import com.mycompany.poggioadventure.core.levels.Test;
import com.mycompany.poggioadventure.core.levels.TestSession;
import java.util.ArrayList;
import java.util.List;

/**
 * Personaggio Non Giocante (NPC) con sistema di dialoghi, test e ricompense.
 * 
 * <p>Estende AdvObject per ereditare proprietà base, aggiungendo funzionalità
 * specifiche per interazioni sociali, gestione test interattivi e sistema
 * di ricompense. Supporta dialoghi contestuali e stato di interazione.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Sistema dialoghi differenziati (primo incontro vs successivi)</li>
 *   <li>Gestione oggetti da donare al giocatore</li>
 *   <li>Test interattivi con sessioni di stato</li>
 *   <li>Sistema ricompense per completamento test</li>
 *   <li>Tracking stato interazioni per personalizzazione</li>
 * </ul>
 * 
 * <p><b>Pattern implementati:</b>
 * <ul>
 *   <li>State Machine: gestione stati interazione e test</li>
 *   <li>Strategy: dialoghi contestuali basati su stato</li>
 *   <li>Observer: notifica completamento test per ricompense</li>
 * </ul>
 */
public class AdvNPC extends AdvObject {
    
    /** Dialoghi per primo incontro con giocatore */
    private final List<String> firstDialogue = new ArrayList<>();
    
    /** Dialoghi per incontri successivi */
    private final List<String> subsequentDialogue = new ArrayList<>();
    
    /** Flag stato interazione con giocatore */
    private boolean hasInteracted = false;
    
    /** Oggetti che NPC può donare al giocatore */
    private final List<AdvObject> itemsToGive = new ArrayList<>();
    
    /** Test configurato per questo NPC */
    private Test test = null;
    
    /** Sessione test attualmente attiva */
    private TestSession activeTestSession = null;

    /** Flag completamento test */
    private boolean testCompleted = false;
    
    /** Oggetto ricompensa per superamento test */
    private AdvObject rewardObject = null;

    /**
     * Costruttore completo con path immagine personalizzato.
     * Configura NPC come non raccoglibile per preservare presenza nel mondo.
     * 
     * @param id Identificatore univoco NPC
     * @param name Nome visualizzato
     * @param description Descrizione per esaminazione
     * @param imagePathForNPC Path immagine specifica NPC
     */
    public AdvNPC(int id, String name, String description, String imagePathForNPC) {
        super(id, name, description, imagePathForNPC);
        setPickupable(false);
    }
    
    /**
     * Costruttore base senza immagine personalizzata.
     * 
     * @param id Identificatore univoco NPC
     * @param name Nome visualizzato  
     * @param description Descrizione per esaminazione
     */
    public AdvNPC(int id, String name, String description) {
        super(id, name, description);
        setPickupable(false);
    }
    
    /**
     * Aggiunge riga dialogo per primo incontro.
     * Costruisce progressivamente conversazione iniziale.
     * 
     * @param line Riga dialogo da aggiungere
     */
    public void addFirstDialogueLine(String line) {
        firstDialogue.add(line);
    }

    /**
     * Aggiunge riga dialogo per incontri successivi.
     * Permette conversazioni diverse dopo prima interazione.
     * 
     * @param line Riga dialogo da aggiungere
     */
    public void addSubsequentDialogueLine(String line) {
        subsequentDialogue.add(line);
    }

    /**
     * Restituisce dialoghi contestuali basati su stato interazione.
     * Implementa strategia di dialogo adattivo per maggiore immersione.
     * 
     * @return Lista dialoghi appropriati per stato corrente
     */
    public List<String> getDialogue() {
        return hasInteracted ? subsequentDialogue : firstDialogue;
    }

    /**
     * Aggiunge oggetto alla lista doni disponibili.
     * 
     * @param item Oggetto che NPC può donare
     */
    public void addItemToGive(AdvObject item) {
        itemsToGive.add(item);
    }

    /**
     * Restituisce oggetti disponibili per donazione.
     * 
     * @return Lista oggetti donabili
     */
    public List<AdvObject> getItemsToGive() {
        return itemsToGive;
    }

    /** Verifica se NPC è già stato avvicinato dal giocatore */
    public boolean hasInteracted() {
        return hasInteracted;
    }

    /**
     * Aggiorna stato interazione NPC per personalizzazione dialoghi.
     * 
     * @param hasInteracted true se NPC è stato avvicinato
     */
    public void setHasInteracted(boolean hasInteracted) {
        this.hasInteracted = hasInteracted;
    }
    
    // ========== SISTEMA TEST INTERATTIVI ==========
    
    /**
     * Configura test che NPC può amministrare al giocatore.
     * 
     * @param test Test da assegnare per questo NPC
     */
    public void setTest(Test test) {
        this.test = test;
    }
    
    /** Restituisce test configurato per questo NPC */
    public Test getTest() {
        return test;
    }
    
    /**
     * Verifica disponibilità test per esecuzione.
     * Test disponibile solo se configurato e non ancora completato.
     * 
     * @return true se test può essere eseguito
     */
    public boolean hasTest() {
        return test != null && !testCompleted;
    }
    
    /**
     * Inizializza nuova sessione test se disponibile.
     * Crea stato di esecuzione per tracking progresso.
     * 
     * @return true se sessione avviata con successo
     */
    public boolean startTestSession() {
        if (!hasTest()) {
            return false;
        }
        
        this.activeTestSession = new TestSession(test);
        return true;
    }
    
    /** Verifica presenza sessione test in corso */
    public boolean hasActiveTestSession() {
        return activeTestSession != null && activeTestSession.isActive();
    }
    
    /** Restituisce sessione test corrente per gestione domande */
    public TestSession getActiveTestSession() {
        return activeTestSession;
    }
    
    /** Termina e pulisce sessione test attiva */
    public void clearTestSession() {
        this.activeTestSession = null;
    }
    
    /** Verifica se test è stato completato con successo */
    public boolean isTestCompleted() {
        return testCompleted;
    }
    
    /**
     * Aggiorna stato completamento test per controllo ricompense.
     * 
     * @param testCompleted true se test superato
     */
    public void setTestCompleted(boolean testCompleted) {
        this.testCompleted = testCompleted;
    }
    
    /**
     * Configura ricompensa per superamento test.
     * 
     * @param rewardObject Oggetto da assegnare come premio
     */
    public void setRewardObject(AdvObject rewardObject) {
        this.rewardObject = rewardObject;
    }
    
    /** Restituisce oggetto ricompensa configurato */
    public AdvObject getRewardObject() {
        return rewardObject;
    }
    
    /** Verifica se NPC ha ricompensa configurata per test */
    public boolean hasRewardObject() {
        return rewardObject != null;
    }
}