/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.GameDescription;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.AdvObjectContainer;
import di.uniba.map.b.adventure.type.CommandType;
import java.util.Iterator;
import di.uniba.map.b.adventure.GameObserver;

/**
 *
 * @author pierpaolo
 */
public class OpenObserver implements GameObserver {

    /** 
     * La classe OpenObserver gestisce l'azione di apertura di oggetti nel gioco. 
     * Gestisce sia oggetti nella stanza corrente che nell'inventario del giocatore. 
     * Se un oggetto è un contenitore, gli oggetti al suo interno vengono trasferiti alla stanza o all'inventario a seconda della loro posizione. 
     * Fornisce feedback al giocatore attraverso messaggi dettagliati su cosa è stato aperto e su eventuali errori o restrizioni
     *
     * @param description
     * @param parserOutput
     * @return
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput) {
        StringBuilder msg = new StringBuilder();
        if (parserOutput.getCommand().getType() == CommandType.OPEN) {
            /*ATTENZIONE: quando un oggetto contenitore viene aperto, tutti gli oggetti contenuti
                * vengongo inseriti nella stanza o nell'inventario a seconda di dove si trova l'oggetto contenitore.
                * Potrebbe non esssere la soluzione ottimale.( Commento di PippoKill)
             */
            if (parserOutput.getObject() == null && parserOutput.getInvObject() == null) { //controlla se l'oggetto è presente nella stanza o nell'inventario
                msg.append("Non c'è niente da aprire qui."); 
            } else {
                if (parserOutput.getObject() != null) { 
                    if (parserOutput.getObject().isOpenable() && parserOutput.getObject().isOpen() == false) { //controlla se l'oggetto è apribile
                        if (parserOutput.getObject() instanceof AdvObjectContainer) {  //controlla se l'oggetto è un contenitore, ovvero un oggetto che può contenere altri oggetti
                            msg.append("Hai aperto: ").append(parserOutput.getObject().getName()); //messaggio di conferma
                            AdvObjectContainer c = (AdvObjectContainer) parserOutput.getObject();  //crea un nuovo oggetto AdvObjectContainer
                            if (!c.getList().isEmpty()) { //controlla se la lista degli oggetti contenuti nell'oggetto è vuota
                                msg.append(c.getName()).append(" contiene:"); //
                                Iterator<AdvObject> it = c.getList().iterator();//crea un nuovo iteratore per scorrere la lista degli oggetti contenuti nell'oggetto
                                while (it.hasNext()) {
                                    AdvObject next = it.next(); //restituisce l'oggetto successivo nella lista
                                    description.getCurrentRoom().getObjects().add(next); //aggiunge l'oggetto alla stanza
                                    msg.append(" ").append(next.getName()); //aggiunge il nome dell'oggetto alla stringa
                                    it.remove(); //rimuove l'oggetto dalla lista
                                }
                                msg.append("\n"); 
                            }
                            parserOutput.getObject().setOpen(true); //aggiorna la variabile booleana per aprire l'oggetto
                        } else {
                            msg.append("Hai aperto: ").append(parserOutput.getObject().getName()); 
                            parserOutput.getObject().setOpen(true); //aggiorna la variabile booleana per aprire l'oggetto
                        }
                    } else {
                        msg.append("Non puoi aprire questo oggetto.");
                    }
                }
                if (parserOutput.getInvObject() != null) {
                    if (parserOutput.getInvObject().isOpenable() && parserOutput.getInvObject().isOpen() == false) { 
                        if (parserOutput.getInvObject() instanceof AdvObjectContainer) {
                            AdvObjectContainer c = (AdvObjectContainer) parserOutput.getInvObject();
                            if (!c.getList().isEmpty()) {
                                msg.append(c.getName()).append(" contiene:");
                                Iterator<AdvObject> it = c.getList().iterator();
                                while (it.hasNext()) {
                                    AdvObject next = it.next();
                                    description.getInventory().add(next);
                                    msg.append(" ").append(next.getName());
                                    it.remove();
                                }
                                msg.append("\n");
                            }
                            parserOutput.getInvObject().setOpen(true);
                        } else {
                            parserOutput.getInvObject().setOpen(true);
                        }
                        msg.append("Hai aperto nel tuo inventario: ").append(parserOutput.getInvObject().getName());
                    } else {
                        msg.append("Non puoi aprire questo oggetto.");
                    }
                }
            }
        }
        return msg.toString();
    }

}
