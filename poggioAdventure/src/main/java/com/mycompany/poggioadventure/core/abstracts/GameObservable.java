package com.mycompany.poggioadventure.core.abstracts;

import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.observers.GameObserver;
import com.mycompany.poggioadventure.parser.ParserOutput;

/**
 * Interfaccia per implementare il pattern Observer nel sistema di gioco.
 * 
 * <p>Permette la registrazione di observer specializzati che vengono notificati
 * quando il giocatore esegue azioni specifiche. Ogni observer può gestire
 * un tipo particolare di comando (movimento, inventario, interazioni, etc.).
 * 
 * <p>Fornisce disaccoppiamento tra il motore di gioco e la logica specifica
 * delle azioni, permettendo estensibilità e manutenibilità del codice.
 */
public interface GameObservable {
    
    /**
     * Registra un observer per ricevere notifiche delle azioni di gioco.
     * Previene duplicati nella lista degli observer.
     * 
     * @param o Observer da registrare per le notifiche
     */
    public void attach(GameObserver o);
    
    /**
     * Rimuove un observer dalla lista delle notifiche.
     * 
     * @param o Observer da rimuovere dal sistema di notifiche
     */
    public void detach(GameObserver o);
    
    /**
     * Notifica tutti gli observer registrati dell'azione corrente del giocatore.
     * Ogni observer può elaborare l'azione e restituire messaggi di feedback.
     * 
     * @param parserOutput Output del parser contenente comando e parametri
     * @param gameContext Contesto di gioco con handler I/O e stato corrente
     */
    public void notifyObservers(ParserOutput parserOutput, GameContext gameContext);
    
}
