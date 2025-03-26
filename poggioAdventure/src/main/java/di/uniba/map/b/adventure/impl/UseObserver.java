package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.GameDescription;
import di.uniba.map.b.adventure.GameObserver;
import di.uniba.map.b.adventure.GameUtils;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.CommandType;
import java.io.Serializable;

/**
 *
 * @author pierpaolo
 */
public class UseObserver implements GameObserver, Serializable {

    /**
     * La classe UseObserver gestisce le azioni del giocatore relative all'uso di oggetti nel gioco. 
     * Implementando l'interfaccia GameObserver, la classe osserva e risponde ai comandi di utilizzo 
     * di oggetti specifici, come la chiave, le batterie, l'armadio e il giocattolo.
     * @param description
     * @param parserOutput
     * @return
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput) {
        StringBuilder msg = new StringBuilder();
        if (parserOutput.getCommand().getType() == CommandType.USE) { //verifica il comando inserito
            boolean interact = false; //variabile booleana per controllare se l'oggetto è stato utilizzato
            boolean key = parserOutput.getInvObject() != null && parserOutput.getInvObject().getId() == 4; //controlla se la chiave è presente nell'inventario
            key = key || parserOutput.getObject() != null && parserOutput.getObject().getId() == 4; //controlla se la chiave è presente nella stanza
            if (key && description.getCurrentRoom().getObject(2) != null) { //controlla se la chiave è presente nell'inventario e se l'armadio è presente nella stanza
                if (description.getCurrentRoom().getObject(2).isOpen()) { //controlla se l'armadio è già aperto
                    msg.append("L'armadio è già aperto...hai la memoria di un criceto!"); //messaggio di errore
                } else {
                    if (description.getCurrentRoom().getObject(2).isOpenable()) { //controlla se l'armadio è apribile
                        msg.append(("L'armadio è già aperto! Perché mi chiedi di fare cose così stupide...")); //messaggio di errore
                    } else {
                        msg.append("Sei fortunato! La chiave ha sbloccato la serratura dell'armadio. Adesso puoi aprirlo."); //messaggio di conferma se abbiamo la chiave e l'armadio è chiuso
                        description.getCurrentRoom().getObject(2).setOpenable(true); //aggiorna la variabile booleana per aprire l'armadio
                    }
                }
                interact = true; //aggiorna la variabile booleana
            }
            boolean battery = parserOutput.getInvObject() != null && parserOutput.getInvObject().getId() == 1; //controlla se le batterie sono presenti nell'inventario
            battery = battery || parserOutput.getObject() != null && parserOutput.getObject().getId() == 1; //controlla se le batterie sono presenti nella stanza
            if (battery && GameUtils.getObjectFromInventory(description.getInventory(), 3) != null) { //controlla se le batterie sono presenti nell'inventario e se il giocattolo è presente nella stanza
                GameUtils.getObjectFromInventory(description.getInventory(), 3).setPushable(true); //aggiorna la variabile booleana per spingere il giocattolo
                msg.append("Hai inserito le batterie nel giocattolo. Sei ritornato un bambino felice!"); //messaggio di conferma
                interact = true; //aggiorna la variabile booleana
            } else if (battery) { //controlla se le batterie sono presenti nell'inventario
                msg.append("Non c'è nessun oggetto nell'inventario che funziona con questo tipo di batterie."); //messaggio di errore
                interact = true; //aggiorna la variabile booleana
            }
            boolean wardrobe = parserOutput.getObject() != null && parserOutput.getObject().getId() == 2; //controlla se l'armadio è presente nella stanza
            if (wardrobe) { 
                if (parserOutput.getObject().isOpen()) { //controlla se l'armadio è aperto
                    msg.append("L'armadio è troppo pieno, non puoi inserirci più nulla!"); 
                } else {
                    msg.append("L'armadio è chiuso e di certo non puoi sollevarlo o spostarlo, è troppo pesante e tu non hai abbastanza muscoli!");
                }
                interact = true; //aggiorna la variabile booleana
            }
            boolean toy = parserOutput.getInvObject() != null && parserOutput.getInvObject().getId() == 3; //controlla se il giocattolo è presente nell'inventario
            toy = toy || parserOutput.getObject() != null && parserOutput.getObject().getId() == 3; //controlla se il giocattolo è presente nella stanza
            if (toy) {
                if (parserOutput.getObject() != null && parserOutput.getObject().getId() == 3) { //controlla se il giocattolo è presente nella stanza
                    msg.append("Devi prima raccoglierlo per poterlo utilizzare."); //messaggio di errore
                    interact = true;
                } else if (parserOutput.getInvObject() != null && parserOutput.getInvObject().getId() == 3) { //controlla se il giocattolo è presente nell'inventario
                    if (parserOutput.getInvObject().isPushable()) { //controlla se il giocattolo è spingibile
                        msg.append("Premi il pulsante del giocattolo e in seguito ad una forte esplosione la tua casa prende fuoco...\ntu e tuoi famigliari cercate invano di salvarvi e venite avvolti dalle fiamme...\nè stata una morte CALOROSA...addio!");
                        description.setCurrentRoom(null); //se le condizioni sono verificate, il gioco termina
                        interact = true; //aggiorna la variabile booleana
                    } else { 
                        msg.append("Mancano le batterie, non posso utilizzarlo così.");
                        interact = true; 
                    }
                }
            }
            if (!interact) { //controlla se l'oggetto è stato utilizzato
                msg.append("Non ci sono oggetti utilizzabili qui.");
            }
        }
        return msg.toString(); //restituisce il messaggio
    }

}
