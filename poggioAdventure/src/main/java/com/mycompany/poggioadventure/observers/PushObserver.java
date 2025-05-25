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
 * @author pierpaolo & MikeRvsso
 */
public class PushObserver implements GameObserver, Serializable {

    @Override
    public String update(GameDescription description, ParserOutput parserOutput, OutputHandler output) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.PUSH) {
            boolean interacted = false;

            
            List<AdvObject> inventoryObjects = parserOutput.getInvObjects();

            // Se non ci sono oggetti nell'inventario, esci subito
            if (inventoryObjects.isEmpty()) {
                msg.append("Non hai oggetti nell'inventario che puoi premere.");
                return msg.toString();
            }

            // Rileva oggetti presenti NELL'INVENTARIO per le combinazioni
            boolean hasPulsanteRack = false;
            boolean hasMartello = false;
            boolean hasCPU = false;
            boolean hasBastonAnziani = false;
            boolean hasTastiera = false;
            boolean hasFotoSanNicola = false;
            boolean hasBobbinaPLA = false;

            // PRIMO LOOP: Rileva tutti gli oggetti nell'inventario
            for (AdvObject obj : inventoryObjects) {
                if (obj == null) continue;

                switch (obj.getId()) {
                    case 24 -> hasPulsanteRack = true;
                    case 16 -> hasMartello = true;
                    case 18 -> hasCPU = true;
                    case 8 -> hasBastonAnziani = true;
                    case 21 -> hasTastiera = true;
                    case 4 -> hasFotoSanNicola = true;
                    case 27 -> hasBobbinaPLA = true;
                }
            }

            //CONTROLLO COMBINAZIONI SPECIALI
            boolean specialCombinationHandled = false;

            // Game Over: Pulsante Rack
            if (hasPulsanteRack) {
                msg.append("ATTENZIONE! Hai premuto il pulsante di emergenza del rack!\n");
                msg.append("Tutti i robot del collegio iniziano a comportarsi in modo anomalo!\n");
                msg.append("Il sistema di sicurezza si attiva e chiude tutte le porte!\n");
                msg.append("Il Direttore Michele ti caccia immediatamente dal collegio!\n");
                msg.append("GAME OVER - La tua ammissione è stata revocata!\n");
                specialCombinationHandled = true;
                //implementare logica per Game Over
            }
            // Game Over: Martello + CPU
            else if (hasMartello && hasCPU) {
                msg.append("DISASTRO! Hai usato il martello sulla CPU delicata!\n");
                msg.append("*CRACK* Il processore si è frantumato in mille pezzi!\n");
                msg.append("Le scintille volano ovunque danneggiando altri componenti!\n");
                msg.append("Il direttore ti guarda inorridito... E successivamente ti comunica..!\n");
                msg.append("GAME OVER - Sei stato espulso per incompetenza tecnica!\n");
                specialCombinationHandled = true;
                // implementare logica per Game Over
            }
            // Comportamento divertenti: San Nicola + Bobina PLA
            else if (hasFotoSanNicola && hasBobbinaPLA) {
                msg.append("Premi delicatamente la foto contro la bobina PLA...\n");
                msg.append("'San Nicola Astronauta, protettore dei maker e delle stampanti 3D!'\n");
                msg.append("Hai appena creato la prima reliquia tecnologica del collegio!\n");
                msg.append("Gli studenti di ingegneria si raccolgono in preghiera davanti alla tua creazione.\n");
                msg.append("Forse hai inventato una nuova religione: il 'Tecno-Cristianesimo'!\n");
                specialCombinationHandled = true;
                
            }
            // Comportamento divertente: Bastone + Tastiera
            else if (hasBastonAnziani && hasTastiera) {
                msg.append("Usi il bastone per premere i tasti della tastiera...\n");
                msg.append("*Click* *Clack* *Tonk*\n");
                msg.append("Incredibile! Hai appena inventato il 'Gaming per Anziani'!\n");
                msg.append("La tastiera suona come un pianoforte scordato!\n");
                specialCombinationHandled = true;
            }

            //SE NON CI SONO COMBINAZIONI SPECIALI, ELABORA SINGOLI OGGETTI
            if (!specialCombinationHandled) {
                for (AdvObject obj : inventoryObjects) {
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

            if (!interacted) {
                msg.append("Non hai oggetti nell'inventario che puoi premere.");
            }
        }

        return msg.toString();
    }
}
