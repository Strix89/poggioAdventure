/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.GameDescription;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.GameObserver;
import java.io.Serializable;

/**
 *
 * @author pierpaolo
 */
public class PickUpObserver implements GameObserver, Serializable {

    /**
     * Observer che verifica il comando di raccoglimento di un oggetto
     * verifica del Comando: Controlla se il comando ricevuto è di tipo PICK_UP (raccogli).
     * Controllo Oggetto: Se l'oggetto è presente nella stanza e può essere raccolto, viene aggiunto all'inventario del giocatore e rimosso dalla stanza.
     * Aggiornamento della Descrizione della Stanza: Se l'oggetto raccolto era nella cucina o nella camera da letto, aggiorna la descrizione della stanza corrente.
     * Messaggi di Feedback: Restituisce messaggi informativi per indicare se l'oggetto è stato raccolto con successo o se non era possibile raccoglierlo.
     * 
     * @param description
     * @param parserOutput
     * @return
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput) {
        StringBuilder msg = new StringBuilder(); // crea un nuovo oggetto StringBuilder
        if (parserOutput.getCommand().getType() == CommandType.PICK_UP) { // controlla il comando inserito 
            if (parserOutput.getObject() != null) { // controlla se l'oggetto è presente nella stanza
                if (parserOutput.getObject().isPickupable()) { // controlla se l'oggetto è raccoglibile
                    description.getInventory().add(parserOutput.getObject()); // aggiunge l'oggetto all'inventario
                    description.getCurrentRoom().getObjects().remove(parserOutput.getObject()); // rimuove l'oggetto dalla stanza
                    msg.append("Hai raccolto: ").append(parserOutput.getObject().getDescription()); // messaggio di conferma
                    if (description.getCurrentRoom().getId() == 2) { // controlla se la stanza corrente è la cucina
                        description.getCurrentRoom().setLook("La solita cucina..."); // aggiorna la descrizione della stanza
                    } else if (description.getCurrentRoom().getId() == 3) { // controlla se la stanza corrente è la camera da letto
                        description.getCurrentRoom().setLook("Non c'è nulla di interessante qui.");
                    }
                } else {
                    msg.append("Non puoi raccogliere questo oggetto.");
                }
            } else {
                msg.append("Non c'è niente da raccogliere qui.");
            }
        }
        return msg.toString(); // restituisce il messaggio
    }

}
