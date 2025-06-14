package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.parser.ParserOutput;

/**
 * Interfaccia Observer per monitoraggio eventi di gioco e reazioni adaptive.
 * 
 * <p>Implementa il pattern Observer per consentire a sottosistemi di reagire
 * a cambiamenti di stato del gioco senza accoppiamento diretto. Gli observer
 * ricevono notifiche su comandi eseguiti e possono produrre output contextual.
 * 
 * <p><b>Utilizzo tipico:</b>
 * <ul>
 *   <li>Sistema achievements per tracking progressi</li>
 *   <li>Gestione eventi triggered da azioni specifiche</li>
 *   <li>Logging e analytics delle interazioni giocatore</li>
 *   <li>Sistema hint dinamici basati su stato</li>
 *   <li>Validazione constraints e regole di gioco</li>
 * </ul>
 * 
 * <p><b>Pattern:</b> Observer per notifiche decentralizzate, Strategy per 
 * comportamenti specifici per tipo di evento.
 */
public interface GameObserver { 

    /**
     * Riceve notifica di evento di gioco e produce output contextual.
     * 
     * <p>Metodo chiamato dal sistema di gioco dopo ogni comando processato
     * per permettere reazioni adaptive. L'observer analizza stato attuale
     * e comando eseguito per determinare se produrre output aggiuntivo.
     * 
     * @param description Stato corrente del mondo di gioco
     * @param parserOutput Comando parsato e risultato esecuzione
     * @param gameContext Contesto esecuzione con handler I/O e logging
     * @return Messaggio da mostrare al giocatore (null/vuoto = nessun output)
     */
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext);
}
