package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.GameUtils;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.CommandType;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author pierpaolo
 */
public class UseObserver implements GameObserver, Serializable {

    @Override
    public String update(GameDescription description, ParserOutput parserOutput) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.USE) {
            boolean interact = false;

            List<AdvObject> allObjects = parserOutput.getObjects();
            allObjects.addAll(parserOutput.getInvObjects());

            for (AdvObject obj : allObjects) {
                if (obj == null) continue;

                int id = obj.getId();

                switch (id) {
                    case 1: // Batterie
                        AdvObject giocattolo = GameUtils.getObjectFromInventory(description.getInventory(), 3);
                        if (giocattolo != null) {
                            giocattolo.setPushable(true);
                            msg.append("Hai inserito le batterie nel giocattolo. Sei ritornato un bambino felice!\n");
                        } else {
                            msg.append("Non c'è nessun oggetto nell'inventario che funziona con questo tipo di batterie.\n");
                        }
                        interact = true;
                        break;

                    case 2: // Armadio
                        if (obj.isOpen()) {
                            msg.append("L'armadio è troppo pieno, non puoi inserirci più nulla!\n");
                        } else {
                            msg.append("L'armadio è chiuso e di certo non puoi sollevarlo o spostarlo, è troppo pesante e tu non hai abbastanza muscoli!\n");
                        }
                        interact = true;
                        break;

                    case 3: // Giocattolo
                        if (parserOutput.getInvObjects().contains(obj)) {
                            if (obj.isPushable()) {
                                msg.append("Premi il pulsante del giocattolo e in seguito ad una forte esplosione la tua casa prende fuoco...\n"
                                        + "tu e tuoi famigliari cercate invano di salvarvi e venite avvolti dalle fiamme...\n"
                                        + "è stata una morte CALOROSA...addio!\n");
                                description.setCurrentRoom(null);
                            } else {
                                msg.append("Mancano le batterie, non posso utilizzarlo così.\n");
                            }
                        } else {
                            msg.append("Devi prima raccoglierlo per poterlo utilizzare.\n");
                        }
                        interact = true;
                        break;

                    case 4: // Chiave
                        AdvObject armadio = description.getCurrentRoom().getObject(2);
                        if (armadio != null) {
                            if (armadio.isOpen()) {
                                msg.append("L'armadio è già aperto...hai la memoria di un criceto!\n");
                            } else if (armadio.isOpenable()) {
                                msg.append("L'armadio è già aperto! Perché mi chiedi di fare cose così stupide...\n");
                            } else {
                                msg.append("Sei fortunato! La chiave ha sbloccato la serratura dell'armadio. Adesso puoi aprirlo.\n");
                                armadio.setOpenable(true);
                            }
                            interact = true;
                        }
                        break;

                    case 5: // Scopettino
                        boolean hasBattery = GameUtils.getObjectFromInventory(description.getInventory(), 1) != null;
                        if (hasBattery) {
                            msg.append("Adesso hai uno scopettino elettrico potenziato!\n");
                        } else {
                            msg.append("Lo scopettino è solo uno scopettino. Magari con delle batterie potresti farlo brillare.\n");
                        }
                        interact = true;
                        break;

                    default:
                        break;
                }
            }
            if (!interact) {
                msg.append("Non ci sono oggetti utilizzabili qui.");
            }
        }
        return msg.toString();
    }
}
