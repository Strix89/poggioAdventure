package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.levels.FlipperLogic;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.ui.ColorText;
import com.mycompany.poggioadventure.ui.cli.CLIInputHandler;
import com.mycompany.poggioadventure.ui.gui.views.UI_Flipper;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.CommandType;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serializable;
import java.util.List;

/**
 * Observer per gestione comando USE con comportamenti specializzati per oggetti di gioco.
 * 
 * <p>Implementa logica uso oggetti context-aware basata su ID specifici correlati
 * alle tre prove del collegio. Fornisce feedback dettagliato e gestisce interazioni
 * speciali come apertura interfacce dedicate (Flipper Zero).
 * 
 * <p><b>Categorie oggetti gestite:</b>
 * <ul>
 *   <li>Prova 1: Test Logica (penna, foto San Nicola)</li>
 *   <li>Prova 2: Assemblaggio PC (componenti hardware, strumenti)</li>
 *   <li>Prova 3: Rivoluzione Robot (chiavi rack, pulsanti controllo)</li>
 *   <li>Speciali: Flipper Zero con interfaccia dedicata CLI/GUI</li>
 *   <li>Utility: Oggetti generici con comportamenti standard</li>
 * </ul>
 * 
 * <p><b>Funzionalità avanzate:</b>
 * <ul>
 *   <li>Validazione prerequisiti (oggetto in inventario)</li>
 *   <li>Interfaccia Flipper adaptive CLI/GUI</li>
 *   <li>Gestione finestre GUI con cleanup automatico</li>
 *   <li>Feedback contestualizzato per progressione prove</li>
 * </ul>
 * 
 * <p><b>Pattern:</b> Observer per comando USE, Strategy per comportamenti
 * oggetto-specifici, Factory per interfacce Flipper.
 */
public class UseObserver implements GameObserver, Serializable {

    /** Riferimento finestra Flipper GUI (transient per serializzazione) */
    private transient UI_Flipper flipperWindow;

    /**
     * Gestisce comando USE con dispatcher basato su ID oggetto.
     * Implementa validazione prerequisiti e comportamenti specializzati
     * per ogni categoria di oggetti delle prove collegio.
     * 
     * @param description Stato mondo per accesso inventario e stanza
     * @param parserOutput Comando parsato con oggetti target
     * @param gameContext Contesto per handler I/O e interfacce speciali
     * @return Messaggio feedback uso oggetto o errore validazione
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.USE) {
            boolean interact = false;

            // Unificazione scope ricerca: stanza + inventario
            List<AdvObject> allObjects = parserOutput.getRoomObjects();
            allObjects.addAll(parserOutput.getInvObjects());

            // Processing con dispatcher ID-based
            for (AdvObject obj : allObjects) {
                if (obj == null) continue;

                int id = obj.getId();

                switch (id) {
                    // === PROVA 1: TEST LOGICA ===
                    case Utils.OBJ_PENNA_ID:
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_PENNA_ID) != null) {
                            msg.append("Usi la penna di Lorenzo Burdo per scrivere il test di logica. ");
                            msg.append("La penna scorre fluida sulla carta, quasi magicamente...\n");
                        } else {
                            msg.append("Devi prima raccogliere la penna per poterla utilizzare.\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_FOTO_ID:
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_FOTO_ID) != null) {
                            msg.append("Osservi attentamente la foto di San Nicola vestito da astronauta. ");
                            msg.append("Forse nasconde un indizio per le prove future?\n");
                        } else {
                            msg.append("Devi prima raccogliere la foto per poterla esaminare da vicino.\n");
                        }
                        interact = true;
                        break;

                    // === PROVA 2: ASSEMBLAGGIO PC ===
                    case Utils.OBJ_MICROSD_ID:
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_MICROSD_ID) != null) {
                            msg.append("Una scheda di memoria che potrebbe contenere dati importanti ");
                            msg.append(
                                    "per l'assemblaggio del computer. Sembra essere la scheda madre di cui hai bisogno!\n");
                        } else {
                            msg.append("Devi prima raccogliere la MicroSD per poterla utilizzare.\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_MARTELLO_ID:
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_MARTELLO_ID) != null) {
                            msg.append("Un martello da laboratorio. Potrebbe essere utile per assemblare ");
                            msg.append("o sistemare componenti hardware, ma usalo con cautela!\n");
                        } else {
                            msg.append("Devi prima raccogliere il martello per poterlo utilizzare.\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_SEGA_CIRCOLARE_ID:
                        msg.append("Una sega circolare affilata per assemblare un pc ? Mhh vedo che qualcuno qui non ti è molto simpatico.., ");
                        msg.append("meglio lasciarla dove sta!\n");
                        interact = true;
                        break;

                    case Utils.OBJ_CPU_ID: // CPU
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_CPU_ID) != null) {
                            msg.append("Il processore principale del computer. Questo è il cervello ");
                            msg.append("che farà funzionare tutto il sistema!\n");
                        } else {
                            msg.append("Devi prima raccogliere la CPU per poterla utilizzare.\n");
                            msg.append(
                                    "Sembra che qualcuno l'abbia lasciata qui dopo aver assemblato un computer...\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_CAVO_HDMI_ID: // Cavo HDMI
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_CAVO_HDMI_ID) != null) {
                            msg.append("Un cavo per collegare monitor e dispositivi. ");
                            msg.append("Fondamentale per vedere se il PC funziona correttamente!\n");
                        } else {
                            msg.append("Devi prima raccogliere il cavo HDMI per poterlo utilizzare.\n");
                            msg.append(
                                    "Sembra che qualcuno l'abbia lasciato qui dopo aver assemblato un computer...\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_MOUSE_ID: // Mouse
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_MOUSE_ID) != null) {
                            msg.append("Un mouse per controllare il computer. ");
                            msg.append("Sembra un po' usurato ma dovrebbe ancora funzionare.\n");
                        } else {
                            msg.append("Devi prima raccogliere il mouse per poterlo utilizzare.\n");
                            msg.append(
                                    "Sembra che qualcuno l'abbia lasciato qui dopo aver assemblato un computer...\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_TASTIERA_ID: // Tastiera
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_TASTIERA_ID) != null) {
                            msg.append("Una tastiera vintage. ");
                            msg.append("I tasti sembrano ancora responsivi nonostante l'età.\n");
                        } else {
                            msg.append("Devi prima raccogliere la tastiera per poterla utilizzare.\n");
                            msg.append(
                                    "Sembra che qualcuno l'abbia lasciata qui dopo aver assemblato un computer...\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_SET_CACCIAVITI_ID: // Set cacciaviti
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_SET_CACCIAVITI_ID) != null) {
                            msg.append("Un set di cacciaviti di precisione. ");
                            msg.append("Perfetti per assemblare componenti delicati del computer!\n");
                        } else {
                            msg.append("Devi prima raccogliere il set di cacciaviti per poterlo utilizzare.\n");
                            msg.append(
                                    "Sembra che qualcuno l'abbia lasciato qui dopo aver assemblato un computer...\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_SALDATORE_ID: // Saldatore
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_SALDATORE_ID) != null) {
                            msg.append("Un saldatore professionale. ");
                            msg.append("Utile per riparazioni elettroniche avanzate, ma richiede esperienza!\n");
                        } else {
                            msg.append("Devi prima raccogliere il saldatore per poterlo utilizzare.\n");
                            msg.append("Sembra che qualcuno l'abbia lasciato qui dopo una riparazione...\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_BOBINA_PLA_ID: // Bobina PLA
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_BOBINA_PLA_ID) != null) {
                            msg.append("Materiale per stampante 3D. ");
                            msg.append("Potrebbe servire per creare supporti o parti personalizzate!\n");
                        } else {
                            msg.append("Devi prima raccogliere la bobina PLA per poterla utilizzare.\n");
                            msg.append("Sembra che qualcuno l'abbia dimenticata qui...\n");
                        }
                        interact = true;
                        break;

                    // === PROVA 3: RIVOLUZIONE ROBOT ===
                    case Utils.OBJ_CHIAVE_RACK_ID:
                        AdvObject rack = description.getCurrentRoom().getObject(Utils.OBJ_RACK_ID);
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

                    case Utils.OBJ_PULSANTE_ID:
                        msg.append("ATTENZIONE! Questo pulsante sembra controllare il sistema principale. ");
                        msg.append("Premerlo potrebbe causare il caos totale nel collegio!\n");
                        interact = true;
                        break;

                    // === OGGETTO SPECIALE: FLIPPER ZERO ===
                    case Utils.OBJ_FLIPPER_ZERO_ID:
                        boolean hasFlipper = Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_FLIPPER_ZERO_ID) != null;
                        if (hasFlipper) {    
                            // Prevenzione istanze multiple GUI
                            if (flipperWindow != null && flipperWindow.isVisible()) {
                                msg.append("[RED]Il Flipper Zero è già attivo! Completa prima l'operazione in corso.[/]\n");
                                break;
                            }                        
                            msg.append("[CRIMSON]Attivazione Flipper Zero...[/]");
                            
                            // Gestione adaptive interfaccia CLI vs GUI
                            if (gameContext.getInputHandler() instanceof CLIInputHandler) {
                                gameContext.getOutputHandler().writeln(msg.toString(), ColorText.WHITE);
                                msg.setLength(0);
                                FlipperLogic flipperLogic = new FlipperLogic(gameContext);
                                flipperLogic.startInteractiveCLISession();
                            } else {
                                handleFlipperGUI(gameContext);
                            }
                        } else {
                            msg.append("Hai avuto per caso un'illuminazione da [HOT_PINK]San Josè Maria[/]?\nNO NO Flipper? Hai rotto il ca***!\n");
                        }
                        interact = true;
                        break;

                    // === OGGETTI UTILITY ===
                    case Utils.OBJ_CHIAVI_AUTO_ID: // Chiavi auto del Direttore
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_CHIAVI_AUTO_ID) != null) {
                            msg.append("Le chiavi della macchina del Direttore. ");
                            msg.append("Se non vieni ammesso, potresti sempre rubar... no, meglio di no!\n");
                        } else {
                            msg.append("Devi prima raccogliere le chiavi per poterle utilizzare.\n");
                            msg.append("Sembra che il Direttore abbia dimenticato di chiuderle a chiave...\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_FORBICI_ID: // Forbici
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_FORBICI_ID) != null) {
                            msg.append("Un paio di forbici affilate. ");
                            msg.append("Potrebbero tornare utili per tagliare cavi o imballaggi.\n");
                        } else {
                            msg.append("Devi prima raccogliere le forbici per poterle utilizzare.\n");
                            msg.append("Sembra che siano state usate per tagliare i capelli di qualche studente...\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_PANTALONI_ID: // Pantaloni Sporchi
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_PANTALONI_ID) != null) {
                            msg.append("Pantaloni sporchi di una sostanza strana...");
                            msg.append("Sembra che qualcuno abbia fatto un pasticcio qui!\n");
                            msg.append("Meglio non toccarli, potrebbero essere infetti!\n");
                        } else {
                            msg.append("Devi prima raccogliere i pantaloni per poterli utilizzare.\n");
                            msg.append("Ma... sei sicuro di volerli toccare?\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_CAPPOTTO_ID: // Cappotto vecchio
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_CAPPOTTO_ID) != null) {
                            msg.append("Indossi il cappotto. Sembra che ti stia a pennello !");
                            msg.append("Potrebbe aiutarti nelle prove successive!\n");
                        } else {
                            msg.append("Devi prima raccogliere il cappotto per poterlo utilizzare.\n");
                            msg.append("Sembra abbastanza costoso...\n");
                        }
                        interact = true;
                        break;

                    case Utils.OBJ_BASTONE_ID: // Bastone per anziani
                        if (Utils.getObjectFromInventory(description.getInventory(), Utils.OBJ_BASTONE_ID) != null) {
                            msg.append("Usi il bastone per anziani come supporto. ");
                            msg.append("Ti fa sentire più saggio, ma forse anche più vecchio!\n");
                        } else {
                            msg.append("Devi prima raccogliere il bastone per poterlo utilizzare.\n");
                            msg.append("Sembra essere stato di qualche anziano ospite del collegio...\n");
                        }
                        interact = true;
                        break;
                    default:
                        // Fallback generico per oggetti non specializzati
                        msg.append("Esamini ").append(obj.getName()).append(". ");
                        msg.append("Potrebbe essere utile per una delle prove del collegio.\n");
                        interact = true;
                        break;
                }
            }

            // Messaggio fallback per nessuna interazione
            if (!interact) {
                msg.append("Non ci sono oggetti utilizzabili qui o non hai gli oggetti necessari nell'inventario.");
            }
        }
        return msg.toString();
    }

    /**
     * Factory method per interfaccia GUI Flipper con gestione lifecycle.
     * Crea finestra dedicata e configura cleanup automatico alla chiusura.
     * 
     * @param gameContext Contesto per inizializzazione FlipperLogic
     */
    private void handleFlipperGUI(GameContext gameContext) {
        FlipperLogic flipperLogic = new FlipperLogic(gameContext);
        flipperWindow = flipperLogic.openGUIInterface();
        
        // Configurazione cleanup automatico per prevenire memory leak
        flipperWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                flipperWindow = null;
            }
        });
    }  
}
