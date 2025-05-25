package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.GameContext;
import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.cli.CLIInputHandler;
import com.mycompany.poggioadventure.ui.gui.views.UI_Flipper;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.FlipperLogic;
import com.mycompany.poggioadventure.parser.CommandType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.List;

/**
 * Observer che gestisce il comando USE per utilizzare oggetti nel gioco.
 * 
 * Controlla oggetti nell'inventario e nella stanza corrente, fornendo
 * risposte specifiche basate sull'ID dell'oggetto e le tre prove del collegio.
 * 
 * @author pierpaolo & MikeRvsso
 * @version 1.0
 * @see CommandType#USE
 */
public class UseObserver implements GameObserver, Serializable {


    private transient UI_Flipper flipperWindow;

    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.USE) {
            boolean interact = false;

            List<AdvObject> allObjects = parserOutput.getRoomObjects();
            allObjects.addAll(parserOutput.getInvObjects());

            for (AdvObject obj : allObjects) {
                if (obj == null)
                    continue;

                int id = obj.getId();

                switch (id) {
                    // OGGETTI PROVA 1 - Test Logica
                    case 3: // Penna di Lorenzo Burdo

                        if (Utils.getObjectFromInventory(description.getInventory(), 3) != null) {
                            msg.append("Usi la penna di Lorenzo Burdo per scrivere il test di logica. ");
                            msg.append("La penna scorre fluida sulla carta, quasi magicamente...\n");
                        } else {
                            msg.append("Devi prima raccogliere la penna per poterla utilizzare.\n");
                        }
                        interact = true;
                        break;

                    case 4: // Foto di San Nicola astronauta
                        if (Utils.getObjectFromInventory(description.getInventory(), 4) != null) {
                            msg.append("Osservi attentamente la foto di San Nicola vestito da astronauta. ");
                            msg.append("Forse nasconde un indizio per le prove future?\n");
                        } else {
                            msg.append("Devi prima raccogliere la foto per poterla esaminare da vicino.\n");
                        }
                        interact = true;
                        break;

                    // OGGETTI PROVA 2 - Assemblaggio PC
                    case 13: // MicroSD
                        if (Utils.getObjectFromInventory(description.getInventory(), 13) != null) {
                            msg.append("Una scheda di memoria che potrebbe contenere dati importanti ");
                            msg.append(
                                    "per l'assemblaggio del computer. Sembra essere la scheda madre di cui hai bisogno!\n");
                        } else {
                            msg.append("Devi prima raccogliere la MicroSD per poterla utilizzare.\n");
                        }
                        interact = true;
                        break;

                    case 16: // Martello
                        if (Utils.getObjectFromInventory(description.getInventory(), 16) != null) {
                            msg.append("Un martello da laboratorio. Potrebbe essere utile per assemblare ");
                            msg.append("o sistemare componenti hardware, ma usalo con cautela!\n");
                        } else {
                            msg.append("Devi prima raccogliere il martello per poterlo utilizzare.\n");
                        }
                        interact = true;
                        break;

                    case 17: // Sega circolare
                        msg.append(
                                "Una sega circolare affilata per assemblare un pc ? Mhh vedo che qualcuno qui non ti è molto simpatico.., ");
                        msg.append("meglio lasciarla dove sta!\n");
                        interact = true;
                        break;

                    case 18: // CPU
                        if (Utils.getObjectFromInventory(description.getInventory(), 18) != null) {
                            msg.append("Il processore principale del computer. Questo è il cervello ");
                            msg.append("che farà funzionare tutto il sistema!\n");
                        } else {
                            msg.append("Devi prima raccogliere la CPU per poterla utilizzare.\n");
                            msg.append(
                                    "Sembra che qualcuno l'abbia lasciata qui dopo aver assemblato un computer...\n");
                        }
                        interact = true;
                        break;

                    case 19: // Cavo HDMI
                        if (Utils.getObjectFromInventory(description.getInventory(), 19) != null) {
                            msg.append("Un cavo per collegare monitor e dispositivi. ");
                            msg.append("Fondamentale per vedere se il PC funziona correttamente!\n");
                        } else {
                            msg.append("Devi prima raccogliere il cavo HDMI per poterlo utilizzare.\n");
                            msg.append(
                                    "Sembra che qualcuno l'abbia lasciato qui dopo aver assemblato un computer...\n");
                        }
                        interact = true;
                        break;

                    case 20: // Mouse
                        if (Utils.getObjectFromInventory(description.getInventory(), 20) != null) {
                            msg.append("Un mouse per controllare il computer. ");
                            msg.append("Sembra un po' usurato ma dovrebbe ancora funzionare.\n");
                        } else {
                            msg.append("Devi prima raccogliere il mouse per poterlo utilizzare.\n");
                            msg.append(
                                    "Sembra che qualcuno l'abbia lasciato qui dopo aver assemblato un computer...\n");
                        }
                        interact = true;
                        break;

                    case 21: // Tastiera
                        if (Utils.getObjectFromInventory(description.getInventory(), 21) != null) {
                            msg.append("Una tastiera vintage. ");
                            msg.append("I tasti sembrano ancora responsivi nonostante l'età.\n");
                        } else {
                            msg.append("Devi prima raccogliere la tastiera per poterla utilizzare.\n");
                            msg.append(
                                    "Sembra che qualcuno l'abbia lasciata qui dopo aver assemblato un computer...\n");
                        }
                        interact = true;
                        break;

                    case 25: // Set cacciaviti
                        if (Utils.getObjectFromInventory(description.getInventory(), 25) != null) {
                            msg.append("Un set di cacciaviti di precisione. ");
                            msg.append("Perfetti per assemblare componenti delicati del computer!\n");
                        } else {
                            msg.append("Devi prima raccogliere il set di cacciaviti per poterlo utilizzare.\n");
                            msg.append(
                                    "Sembra che qualcuno l'abbia lasciato qui dopo aver assemblato un computer...\n");
                        }
                        interact = true;
                        break;

                    case 26: // Saldatore
                        if (Utils.getObjectFromInventory(description.getInventory(), 26) != null) {
                            msg.append("Un saldatore professionale. ");
                            msg.append("Utile per riparazioni elettroniche avanzate, ma richiede esperienza!\n");
                        } else {
                            msg.append("Devi prima raccogliere il saldatore per poterlo utilizzare.\n");
                            msg.append("Sembra che qualcuno l'abbia lasciato qui dopo una riparazione...\n");
                        }
                        interact = true;
                        break;

                    case 27: // Bobina PLA
                        if (Utils.getObjectFromInventory(description.getInventory(), 27) != null) {
                            msg.append("Materiale per stampante 3D. ");
                            msg.append("Potrebbe servire per creare supporti o parti personalizzate!\n");
                        } else {
                            msg.append("Devi prima raccogliere la bobina PLA per poterla utilizzare.\n");
                            msg.append("Sembra che qualcuno l'abbia dimenticata qui...\n");
                        }
                        interact = true;
                        break;

                    // OGGETTI PROVA 3 - Rivoluzione Robot
                    case 23: // Chiave Rack
                        AdvObject rack = description.getCurrentRoom().getObject(22);
                        if (rack != null) {
                            if (rack.isOpen()) {
                                msg.append("Il rack è già aperto. ");
                                msg.append("All'interno potresti trovare strumenti per controllare i robot!\n");
                            } else {
                                msg.append("Usi la chiave per aprire il rack del server. ");
                                msg.append("Ora puoi accedere ai controlli dei robot!\n");
                                rack.setOpenable(true);
                            }
                        }
                        interact = true;
                        break;

                    case 24: // Pulsante del rack
                        msg.append("ATTENZIONE! Questo pulsante sembra controllare il sistema principale. ");
                        msg.append("Premerlo potrebbe causare il caos totale nel collegio!\n");
                        interact = true;
                        break;

                    // OGGETTI BONUS/UTILITÀ
                    case 14: // Chiavi auto del Direttore
                        if (Utils.getObjectFromInventory(description.getInventory(), 14) != null) {
                            msg.append("Le chiavi della macchina del Direttore. ");
                            msg.append("Se non vieni ammesso, potresti sempre rubar... no, meglio di no!\n");
                        } else {
                            msg.append("Devi prima raccogliere le chiavi per poterle utilizzare.\n");
                            msg.append("Sembra che il Direttore abbia dimenticato di chiuderle a chiave...\n");
                        }
                        interact = true;
                        break;

                    case 15: // Forbici
                        if (Utils.getObjectFromInventory(description.getInventory(), 15) != null) {
                            msg.append("Un paio di forbici affilate. ");
                            msg.append("Potrebbero tornare utili per tagliare cavi o imballaggi.\n");
                        } else {
                            msg.append("Devi prima raccogliere le forbici per poterle utilizzare.\n");
                            msg.append("Sembra che siano state usate per tagliare i capelli di qualche studente...\n");
                        }
                        interact = true;
                        break;

                    case 7: // Pantaloni Sporchi
                        if (Utils.getObjectFromInventory(description.getInventory(), 7) != null) {
                            msg.append("Pantaloni sporchi di una sostanza strana...");
                            msg.append("Sembra che qualcuno abbia fatto un pasticcio qui!\n");
                            msg.append("Meglio non toccarli, potrebbero essere infetti!\n");
                        } else {
                            msg.append("Devi prima raccogliere i pantaloni per poterli utilizzare.\n");
                            msg.append("Ma... sei sicuro di volerli toccare?\n");
                        }
                        interact = true;
                        break;

                    case 6: // Cappotto vecchio
                        if (Utils.getObjectFromInventory(description.getInventory(), 6) != null) {
                            msg.append("Indossi il cappotto. Sembra che ti stia a pennello !");
                            msg.append("Potrebbe aiutarti nelle prove successive!\n");
                        } else {
                            msg.append("Devi prima raccogliere il cappotto per poterlo utilizzare.\n");
                            msg.append("Sembra abbastanza costoso...\n");
                        }
                        interact = true;
                        break;

                    case 8: // Bastone per anziani
                        if (Utils.getObjectFromInventory(description.getInventory(), 8) != null) {
                            msg.append("Usi il bastone per anziani come supporto. ");
                            msg.append("Ti fa sentire più saggio, ma forse anche più vecchio!\n");
                        } else {
                            msg.append("Devi prima raccogliere il bastone per poterlo utilizzare.\n");
                            msg.append("Sembra essere stato di qualche anziano ospite del collegio...\n");
                        }
                        interact = true;
                        break;
                    case 50:
                        boolean hasFlipper = Utils.getObjectFromInventory(description.getInventory(), 50) != null;
                        if (hasFlipper) {    
                            if (flipperWindow != null && flipperWindow.isVisible()) {
                                msg.append("[RED]Il Flipper Zero è già attivo! Completa prima l'operazione in corso.[/]\n");
                                break;
                            }                        
                            msg.append("[CRIMSON]Attivazione Flipper Zero...[/]");
                            
                            if (gameContext.getInputHandler() instanceof CLIInputHandler) {
                                // IMPORTANTE: Forza la visualizzazione immediata del messaggio
                                gameContext.getOutputHandler().writeln(msg.toString(), ColorText.WHITE);
                                msg.setLength(0); // Svuota il buffer perché è già stato stampato
                                FlipperLogic flipperLogic = new FlipperLogic(gameContext);
                                flipperLogic.startInteractiveCLISession();
                            } else {
                                handleFlipperGUI(gameContext);
                            }
                        } else {
                            msg.append("Hai avuto per caso un'illuminazione da [HOT_PINK]San Josè Maria[/]?\nNO NO Flipper? Hai rotto il ca***!\n");
                        }
                        interact = true;
                    default:
                        // Gestione generica per oggetti non specificamente programmati
                        msg.append("Esamini ").append(obj.getName()).append(". ");
                        msg.append("Potrebbe essere utile per una delle prove del collegio.\n");
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

    /**
     * Gestisce l'apertura della GUI del Flipper
     * Crea una finestra se non esiste, altrimenti mostra un messaggio di errore
     * 
     * @param gameContext Il contesto del gioco
     */
    private void handleFlipperGUI(GameContext gameContext) {
        // Crea e mostra la finestra del Flipper
        FlipperLogic flipperLogic = new FlipperLogic(gameContext);
        flipperWindow = flipperLogic.openGUIInterface(); // Ora restituisce l'istanza
        
        // Aggiunge un listener per sapere quando la finestra viene chiusa
        flipperWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                flipperWindow = null;
            }
        });
    }  
}
