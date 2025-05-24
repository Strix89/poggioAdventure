package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.CommandType;
import com.mycompany.poggioadventure.ui.OutputHandler;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author MikeRvsso
 */
public class UseObserver implements GameObserver, Serializable {

   @Override
public String update(GameDescription description, ParserOutput parserOutput, OutputHandler output) {
    StringBuilder msg = new StringBuilder();

    if (parserOutput.getCommand().getType() == CommandType.USE) {
        boolean interact = false;

        List<AdvObject> allObjects = parserOutput.getObjects();
        allObjects.addAll(parserOutput.getInvObjects());

        for (AdvObject obj : allObjects) {
            if (obj == null) continue;

            int id = obj.getId();

            switch (id) {
                case 17: // Sega circolare - UNICO OGGETTO che può essere usato senza raccoglierlo
                    msg.append("Una sega circolare affilata per assemblare un pc? Mhh vedo che qualcuno qui non ti è molto simpatico... ");
                    msg.append("meglio lasciarla dove sta!\n");
                    interact = true;
                    break;

                // TUTTI GLI ALTRI OGGETTI richiedono di essere nell'inventario
                case 3: // Penna di Lorenzo Burdo
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Usi la penna di Lorenzo Burdo per scrivere il test di logica. ");
                        msg.append("La penna scorre fluida sulla carta, quasi magicamente...\n");
                    } else {
                        msg.append("Devi prima raccogliere la penna per poterla utilizzare.\n");
                    }
                    interact = true;
                    break;

                case 4: // Foto di San Nicola astronauta
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Osservi attentamente la foto di San Nicola vestito da astronauta. ");
                        msg.append("Forse nasconde un indizio per le prove future?\n");
                    } else {
                        msg.append("Devi prima raccogliere la foto per poterla esaminare da vicino.\n");
                    }
                    interact = true;
                    break;

                case 16: // Martello
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Un martello da laboratorio. Potrebbe essere utile per assemblare ");
                        msg.append("o sistemare componenti hardware, ma usalo con cautela!\n");
                    } else {
                        msg.append("Devi prima raccogliere il martello per poterlo utilizzare.\n");
                    }
                    interact = true;
                    break;

                case 25: // Set cacciaviti
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Un set di cacciaviti di precisione. ");
                        msg.append("Perfetti per assemblare componenti delicati del computer!\n");
                    } else {
                        msg.append("Devi prima raccogliere il set di cacciaviti per poterlo utilizzare.\n");
                    }
                    interact = true;
                    break;

                case 26: // Saldatore
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Un saldatore professionale. ");
                        msg.append("Utile per riparazioni elettroniche avanzate, ma richiede esperienza!\n");
                    } else {
                        msg.append("Devi prima raccogliere il saldatore per poterlo utilizzare.\n");
                    }
                    interact = true;
                    break;

                case 18: // CPU
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Il processore principale del computer. Questo è il cervello ");
                        msg.append("che farà funzionare tutto il sistema!\n");
                    } else {
                        msg.append("Devi prima raccogliere la CPU per poterla utilizzare.\n");
                    }
                    interact = true;
                    break;

                case 19: // Cavo HDMI
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Un cavo per collegare monitor e dispositivi. ");
                        msg.append("Fondamentale per vedere se il PC funziona correttamente!\n");
                    } else {
                        msg.append("Devi prima raccogliere il cavo HDMI per poterlo utilizzare.\n");
                    }
                    interact = true;
                    break;

                case 20: // Mouse
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Un mouse per controllare il computer. ");
                        msg.append("Sembra un po' usurato ma dovrebbe ancora funzionare.\n");
                    } else {
                        msg.append("Devi prima raccogliere il mouse per poterlo utilizzare.\n");
                    }
                    interact = true;
                    break;

                case 21: // Tastiera
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Una tastiera vintage. ");
                        msg.append("I tasti sembrano ancora responsivi nonostante l'età.\n");
                    } else {
                        msg.append("Devi prima raccogliere la tastiera per poterla utilizzare.\n");
                    }
                    interact = true;
                    break;

                case 27: // Bobina PLA
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Materiale per stampante 3D. ");
                        msg.append("Potrebbe servire per creare supporti o parti personalizzate!\n");
                    } else {
                        msg.append("Devi prima raccogliere la bobina PLA per poterla utilizzare.\n");
                    }
                    interact = true;
                    break;

                default:
                    // Per tutti gli altri oggetti, controlla sempre l'inventario
                    if (parserOutput.getInvObjects().contains(obj)) {
                        msg.append("Esamini ").append(obj.getName()).append(". ");
                        msg.append("Potrebbe essere utile per una delle prove del collegio.\n");
                    } else {
                        msg.append("Devi prima raccogliere ").append(obj.getName()).append(" per poterlo utilizzare.\n");
                    }
                    interact = true;
                    break;
            }
        }

        if (!interact) {
            msg.append("Non ci sono oggetti utilizzabili qui o non hai gli oggetti necessari nell'inventario.");
        }
    }

    return msg.toString();
}
}
