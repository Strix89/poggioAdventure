package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.CommandType;
import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.core.utils.Utils;

import java.io.Serializable;
import java.util.List;

/**
 * Observer per gestione comando PUSH con combinazioni speciali e scenari game over.
 * 
 * <p>Implementa logica complessa di interazioni tra oggetti nell'inventario con
 * priority system: combinazioni speciali > comportamenti individuali.
 * Gestisce scenari critici che possono terminare il gioco e interazioni umoristiche.
 * 
 * <p><b>Combinazioni critiche:</b>
 * <ul>
 *   <li>Pulsante Rack (ID: 24) → GAME OVER immediato</li>
 *   <li>Martello + CPU (ID: 16+18) → GAME OVER per distruzione</li>
 *   <li>Foto + Bobina PLA (ID: 4+27) → Easter egg umoristico</li>
 *   <li>Bastone + Tastiera (ID: 8+21) → Interazione divertente</li>
 * </ul>
 * 
 * <p><b>Algoritmo:</b>
 * <ol>
 *   <li>Scansione inventario per rilevamento oggetti chiave</li>
 *   <li>Valutazione combinazioni speciali (mutualmente esclusive)</li>
 *   <li>Esecuzione comportamenti individuali se nessuna combinazione attiva</li>
 * </ol>
 * 
 */
public class PushObserver implements GameObserver, Serializable {

    /**
     * Gestisce comando PUSH con priority system per combinazioni speciali.
     * Analizza inventario per identificare pattern di oggetti e attiva
     * comportamenti appropriati con precedenza per scenari critici.
     * 
     * @param description Stato mondo per accesso inventario e aggiornamenti
     * @param parserOutput Comando parsato con oggetti target
     * @param gameContext Contesto esecuzione (non utilizzato)
     * @return Messaggio risultato interazione o scenario game over
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.PUSH) {
            boolean interacted = false;

            List<AdvObject> allObjects = parserOutput.getInvObjects();
            allObjects.addAll(parserOutput.getRoomObjects());

            if (allObjects.isEmpty()) {
                msg.append("Non hai oggetti nell'inventario che puoi premere.");
                return msg.toString();
            }

            // Rilevamento oggetti per combinazioni speciali
            boolean hasPulsanteRack = false;
            boolean hasMartello = false;
            boolean hasCPU = false;
            boolean hasBastonAnziani = false;
            boolean hasTastiera = false;
            boolean hasFotoSanNicola = false;
            boolean hasBobbinaPLA = false;

            // Scansione inventario per pattern matching
            for (AdvObject obj : allObjects) {
                if (obj == null) continue;

                switch (obj.getId()) {
                    case 24 -> hasPulsanteRack = true;
                    case 16 -> hasMartello = true;
                    case 18 -> hasCPU = true;
                    case 8  -> hasBastonAnziani = true;
                    case 21 -> hasTastiera = true;
                    case 4  -> hasFotoSanNicola = true;
                    case 27 -> hasBobbinaPLA = true;
                }
            }

            boolean specialCombinationHandled = false;

            // SCENARIO CRITICO 1: Pulsante emergenza rack
            if (hasPulsanteRack) {
                msg.append("[RED]ATTENZIONE![/] Hai premuto il pulsante di emergenza del rack!\n");
                msg.append("Tutti i [NPC]robot[/] del collegio iniziano a comportarsi in modo anomalo!\n");
                msg.append("Il sistema di sicurezza si attiva e spegne tutte le macchine!\n");
                msg.append("Il Direttore Michele ti caccia immediatamente dal collegio!\n");
                msg.append("[NPC]Direttore[/]: [RED]GAME OVER[/] - Non puoi essere ammesso sei un [RED]idiota[/]!\n");
                specialCombinationHandled = true;
                description.getInventory().add(Utils.buildLoseGameObject());
            }
            
            // SCENARIO CRITICO 2: Distruzione CPU con martello
            else if (hasMartello && hasCPU) {
                msg.append("[RED]DISASTRO[/]! Hai usato il martello sulla CPU delicata!\n");
                msg.append("[RED]*CRACK*[/] Il processore si è frantumato in mille pezzi!\n");
                msg.append("Non puoi più montarlo nel computer\n");
                msg.append("[NPC]Direttore[/]: [RED]GAME OVER[/] - Non puoi essere ammesso sei un [RED]idiota[/]!\n");
                specialCombinationHandled = true;
                description.getInventory().add(Utils.buildLoseGameObject());
            }

            // EASTER EGG 1: Reliquia tecnologica
            else if (hasFotoSanNicola && hasBobbinaPLA) {
                msg.append("Premi delicatamente la foto contro la bobina PLA...\n");
                msg.append("'San Nicola Astronauta, protettore dei maker e delle stampanti 3D!'\n");
                msg.append("Hai appena creato la prima reliquia tecnologica del collegio!\n");
                msg.append("Gli studenti di ingegneria si raccolgono in preghiera davanti alla tua creazione.\n");
                msg.append("Forse hai inventato una nuova religione: il 'Tecno-Cristianesimo'!\n");
                specialCombinationHandled = true;
            }
            
            // EASTER EGG 2: Gaming per anziani
            else if (hasBastonAnziani && hasTastiera) {
                msg.append("Usi il bastone per premere i tasti della tastiera...\n");
                msg.append("*Click* *Clack* *Tonk*\n");
                msg.append("Incredibile! Hai appena inventato il 'Gaming per Anziani'!\n");
                msg.append("La tastiera suona come un pianoforte scordato!\n");
                specialCombinationHandled = true;
            }

            // Comportamenti individuali se nessuna combinazione speciale attiva
            if (!specialCombinationHandled) {
                for (AdvObject obj : allObjects) {
                    if (obj == null) continue;

                    if (obj.isPushable()) {
                        msg.append("Hai premuto ").append(obj.getName()).append(" ma non succede nulla di particolare.\n");
                        interacted = true;
                    } else {
                        msg.append("Non puoi premere ").append(obj.getName()).append(".\n");
                        interacted = true;
                    }
                }
            } else {
                interacted = true;
            }

            // Fallback per nessuna interazione
            if (!interacted) {
                msg.append("Non puoi premere nulla qui.\n");
            }
        }
        return msg.toString();
    }
}
