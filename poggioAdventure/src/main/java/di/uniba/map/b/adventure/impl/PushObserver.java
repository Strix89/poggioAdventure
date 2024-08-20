/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.GameDescription;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.GameObserver;
import di.uniba.map.b.adventure.GameUtils;

/**
 *
 * @author pierpaolo
 */
public class PushObserver implements GameObserver {

    /**
     * PushObserver gestisce le azioni di spingere o premere oggetti nel gioco:
     * Verifica se il comando è un'azione di spinta.
     * Controlla se l'oggetto è pushabile e gestisce gli oggetti specifici come il giocattolo con l'ID 3, inclusa la logica 
     * per terminare il gioco in caso di esplosione se le condizioni sono soddisfatte 
     * Fornisce messaggi appropriati basati sulle azioni e sulla disponibilità degli oggetti.
     * Observer che verifica il comando per prendere un oggetto
     * @param description
     * @param parserOutput
     * @return
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput) {
        StringBuilder msg = new StringBuilder(); 
        if (parserOutput.getCommand().getType() == CommandType.PUSH) { //verifica il comando inserito
            //ricerca oggetti pushabili
            if (parserOutput.getObject() != null && parserOutput.getObject().isPushable()) { //controlla che l'oggetto sia presente nella stanza e sia prendibile
                msg.append("Hai premuto: ").append(parserOutput.getObject().getName()).append("\n"); //messaggio di conferma
                if (parserOutput.getObject().getId() == 3 && GameUtils.getObjectFromInventory(description.getInventory(), 1) != null) { //controlla se l'oggetto è il giocattolo e se le batterie sono presenti nell'inventario
                    msg.append("Premi il pulsante del giocattolo e in seguito ad una forte esplosione la tua casa prende fuoco...\ntu e tuoi famigliari cercate invano di salvarvi e venite avvolti dalle fiamme...\nè stata una morte CALOROSA...addio!");
                    description.setCurrentRoom(null); // se le condizioni sono verificate, il gioco termina 
                } else if (parserOutput.getObject().getId() == 3) { //se l'oggetto è il giocattolo e le batterie non sono presenti nell'inventario
                    msg.append("Non posso utilizzare il giocattolo senza delle batterie.");
                }
            } else if (parserOutput.getInvObject() != null && parserOutput.getInvObject().isPushable()) { //controlla se l'oggetto è presente nell'inventario e se è prendibile
                msg.append("Hai premuto: ").append(parserOutput.getInvObject().getName()).append("\n"); //messaggio di conferma
                if (parserOutput.getInvObject().getId() == 3 && GameUtils.getObjectFromInventory(description.getInventory(), 1) != null) { //controlla se l'oggetto è il giocattolo e se le batterie sono presenti nell'inventario
                    msg.append("Premi il pulsante del giocattolo e in seguito ad una forte esplosione la tua casa prende fuoco...\ntu e tuoi famigliari cercate invano di salvarvi e venite avvolti dalle fiamme...\nè stata una morte CALOROSA...addio!");
                    description.setCurrentRoom(null); // se le condizioni sono verificate, il gioco termina
                } else if (parserOutput.getInvObject().getId() == 3) { //se l'oggetto è il giocattolo e le batterie non sono presenti nell'inventario
                    msg.append("Non posso utilizzare il giocattolo senza delle batterie."); //messaggio di errore
                }
            } else {
                msg.append("Non ci sono oggetti che puoi premere qui.");
            }
        }
        return msg.toString();
    }

}
