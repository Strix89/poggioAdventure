package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.GameDescription;
import di.uniba.map.b.adventure.parser.ParserOutput;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.CommandType;
import di.uniba.map.b.adventure.GameObserver;
import di.uniba.map.b.adventure.GameUtils;

import java.util.List;

public class PushObserver implements GameObserver {

    @Override
    public String update(GameDescription description, ParserOutput parserOutput) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.PUSH) {
            boolean interacted = false;

            List<AdvObject> allObjects = parserOutput.getObjects();
            allObjects.addAll(parserOutput.getInvObjects());

            boolean hasBattery = false;
            boolean hasScopettino = false;

            for (AdvObject obj : allObjects) {
                if (obj == null) continue;

                int id = obj.getId();

                // Rileva se sono presenti batteria e scopettino
                if (id == 1) hasBattery = true;
                if (id == 5) hasScopettino = true;

                // Gestione oggetti pushabili
                if (obj.isPushable()) {
                    msg.append("Hai premuto: ").append(obj.getName()).append("\n");

                    if (id == 3) { // Giocattolo
                        if (GameUtils.getObjectFromInventory(description.getInventory(), 1) != null) {
                            msg.append("Premi il pulsante del giocattolo e in seguito ad una forte esplosione la tua casa prende fuoco...\n")
                               .append("tu e tuoi famigliari cercate invano di salvarvi e venite avvolti dalle fiamme...\n")
                               .append("è stata una morte CALOROSA...addio!\n");
                            description.setCurrentRoom(null);
                        } else {
                            msg.append("Non posso utilizzare il giocattolo senza delle batterie.\n");
                        }
                    }

                    interacted = true;
                }
            }

            // Interazione speciale batteria + scopettino
            if (hasBattery && hasScopettino) {
                msg.append("Lo scopettino ti esplode in faccia! Sei un rimbambito.\n");
                interacted = true;
            }

            if (!interacted) {
                msg.append("Non ci sono oggetti che puoi premere qui.");
            }
        }

        return msg.toString();
    }
}
