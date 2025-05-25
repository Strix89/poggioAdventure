package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.GameContext;
import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.parser.ParserOutput;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.parser.CommandType;
import com.mycompany.poggioadventure.core.utils.Utils;
import java.io.Serializable;
import java.util.List;

/**
 * Observer che gestisce il comando PUSH per premere oggetti nell'inventario del giocatore.
 * 
 * Questa classe implementa il pattern Observer per rispondere ai comandi di tipo PUSH,
 * permettendo ai giocatori di premere oggetti raccolti nell'inventario. Include gestione
 * di combinazioni speciali tra oggetti, comportamenti divertenti e situazioni di GAME OVER.
 * 
 * <h3>Combinazioni Speciali Gestite:</h3>
 * <ul>
 *   <li><b>Pulsante Rack (ID: 24)</b> - GAME OVER: Causa chaos robotico nel collegio</li>
 *   <li><b>Martello + CPU (ID: 16+18)</b> - GAME OVER: Distrugge componenti delicati</li>
 *   <li><b>Foto San Nicola + Bobina PLA (ID: 4+27)</b> - Comportamento divertente: Crea reliquia tecnologica</li>
 *   <li><b>Bastone + Tastiera (ID: 8+21)</b> - Comportamento divertente: Gaming per anziani</li>
 * </ul>
 * 
 * <h3>Logica di Funzionamento:</h3>
 * 1. Controlla solo oggetti nell'inventario del giocatore
 * 2. Rileva presenza di oggetti per combinazioni speciali
 * 3. Esegue combinazioni speciali (se presenti) PRIMA dei comportamenti individuali
 * 4. Gestisce comportamenti di default per oggetti pushable singoli
 * 
 * @author pierpaolo & MikeRvsso
 * @version 1.0
 * @since 1.0
 * @see GameObserver
 * @see CommandType#PUSH
 */
public class PushObserver implements GameObserver, Serializable {

    /**
     * Aggiorna lo stato del gioco in risposta a un comando PUSH.
     * 
     * Questo metodo viene chiamato quando il parser riconosce un comando PUSH.
     * Controlla esclusivamente gli oggetti nell'inventario del giocatore e gestisce:
     * - Combinazioni speciali di game over
     * - Comportamenti divertenti tra oggetti
     * - Interazioni singole con oggetti pushable
     * 
     * <h4>Priorità di Esecuzione:</h4>
     * <ol>
     *   <li>Verifica presenza oggetti nell'inventario</li>
     *   <li>Rilevamento oggetti per combinazioni speciali</li>
     *   <li>Esecuzione combinazioni speciali (mutualmente esclusive)</li>
     *   <li>Gestione comportamenti individuali (solo se nessuna combinazione attiva)</li>
     * </ol>
     * 
     * <h4>Combinazioni di Game Over:</h4>
     * <ul>
     *   <li><b>Pulsante Rack</b>: Attiva sistema di emergenza → espulsione immediata</li>
     *   <li><b>Martello + CPU</b>: Distrugge processore → fallimento prova assemblaggio</li>
     * </ul>
     * 
     * <h4>Comportamenti Divertenti:</h4>
     * <ul>
     *   <li><b>Foto + Bobina PLA</b>: Crea "reliquia tecnologica" umoristica</li>
     *   <li><b>Bastone + Tastiera</b>: Inventa sistema di "Gaming per Anziani"</li>
     * </ul>
     * 
     * @param description La descrizione corrente del gioco contenente stanze, oggetti e stato
     * @param parserOutput L'output del parser contenente il comando PUSH e gli oggetti identificati nell'inventario
     * @param output L'handler per gestire l'output verso l'interfaccia utente
     * @return Una stringa contenente la risposta dell'azione di premere, inclusi messaggi di game over o comportamenti speciali
     * 
     * @throws NullPointerException se description, parserOutput o output sono null
     * @throws IllegalStateException se l'inventario contiene oggetti con ID non gestiti
     * 
     * @see #detectSpecialCombinations(List)
     * @see #handleGameOverScenarios(boolean, boolean, boolean)
     * @see #handleFunBehaviors(boolean, boolean, boolean, boolean)
     */
    @Override
    public String update(GameDescription description, ParserOutput parserOutput, GameContext gameContext) {
        StringBuilder msg = new StringBuilder();

        if (parserOutput.getCommand().getType() == CommandType.PUSH) {
            boolean interacted = false;

            // Controllo preliminare: descrizione e parserOutput non null
            List<AdvObject> allObjects = parserOutput.getInvObjects();

            allObjects.addAll(parserOutput.getRoomObjects());

            // Controllo preliminare: nessun oggetto nell'inventario
            if (allObjects.isEmpty()) {
                msg.append("Non hai oggetti nell'inventario che puoi premere.");
                return msg.toString();
            }

            // ========== RILEVAMENTO OGGETTI PER COMBINAZIONI SPECIALI ==========
            
            /**
             * Flag per identificare la presenza di oggetti specifici nell'inventario.
             * Utilizzati per determinare quali combinazioni speciali sono possibili.
             */
            boolean hasPulsanteRack = false;      // ID: 24 - Pulsante di emergenza del rack
            boolean hasMartello = false;          // ID: 16 - Martello da laboratorio
            boolean hasCPU = false;               // ID: 18 - Processore del computer
            boolean hasBastonAnziani = false;     // ID: 8  - Bastone per anziani
            boolean hasTastiera = false;          // ID: 21 - Tastiera vintage
            boolean hasFotoSanNicola = false;     // ID: 4  - Foto di San Nicola astronauta
            boolean hasBobbinaPLA = false;        // ID: 27 - Bobina per stampante 3D

            /**
             * PRIMO LOOP: Scansione inventario per rilevamento oggetti.
             * Identifica tutti gli oggetti presenti che partecipano a combinazioni speciali.
             */
            for (AdvObject obj : allObjects) {
                if (obj == null) continue;

                switch (obj.getId()) {
                    case 24 -> hasPulsanteRack = true;    // Pulsante rack (PERICOLO!)
                    case 16 -> hasMartello = true;        // Martello (può causare danni)
                    case 18 -> hasCPU = true;             // CPU (componente delicato)
                    case 8  -> hasBastonAnziani = true;   // Bastone (accessorio divertente)
                    case 21 -> hasTastiera = true;        // Tastiera (strumento musicale?)
                    case 4  -> hasFotoSanNicola = true;   // Foto (oggetto sacro)
                    case 27 -> hasBobbinaPLA = true;      // Bobina (materiale artistico)
                }
            }

            // ========== GESTIONE COMBINAZIONI SPECIALI ==========
            
            /**
             * Flag per impedire l'esecuzione di comportamenti multipli.
             * Una volta attivata una combinazione speciale, previene l'elaborazione
             * di comportamenti individuali per evitare output confusi.
             */
            boolean specialCombinationHandled = false;

            // ===== SCENARIOS DI GAME OVER =====

            /**
             * GAME OVER SCENARIO 1: Pulsante di Emergenza del Rack
             * 
             * PERICOLO ESTREMO: Attivazione del sistema di emergenza del collegio.
             * Causa: Pressione del pulsante rosso del rack dei server. 
             * Questo scenario rappresenta il fallimento della Prova 3 (Rivoluzione Robot)
             * e porta alla revoca immediata dell'ammissione al collegio.
             */
            if (hasPulsanteRack) {
                msg.append("ATTENZIONE! Hai premuto il pulsante di emergenza del rack!\n");
                msg.append("Tutti i robot del collegio iniziano a comportarsi in modo anomalo!\n");
                msg.append("Il sistema di sicurezza si attiva e chiude tutte le porte!\n");
                msg.append("Il Direttore Michele ti caccia immediatamente dal collegio!\n");
                msg.append("GAME OVER - La tua ammissione è stata revocata!\n");
                specialCombinationHandled = true;
                // TODO: Implementare logica per Game Over nel sistema principale
            }
            
            /**
             * GAME OVER SCENARIO 2: Distruzione della CPU con Martello
             * 
             * DISASTRO TECNICO: Uso improprio di strumenti nella Prova 2.
             * Causa: Tentativo di "assemblare" un computer usando un martello sulla CPU.
             * Questo scenario rappresenta il fallimento della Prova 2 (Assemblaggio PC)
             * e porta all'espulsione per inadeguatezza tecnica.
             */
            else if (hasMartello && hasCPU) {
                msg.append("DISASTRO! Hai usato il martello sulla CPU delicata!\n");
                msg.append("*CRACK* Il processore si è frantumato in mille pezzi!\n");
                msg.append("Le scintille volano ovunque danneggiando altri componenti!\n");
                msg.append("Il direttore ti guarda inorridito... E successivamente ti comunica..!\n");
                msg.append("GAME OVER - Sei stato espulso per incompetenza tecnica!\n");
                specialCombinationHandled = true;
                // TODO: Implementare logica per Game Over nel sistema principale
            }

            // ===== COMPORTAMENTI DIVERTENTI (NON DANNOSI) =====

            /**
             * COMPORTAMENTO DIVERTENTE 1: Creazione di Reliquia Tecnologica
             * Questo comportamento non ha effetti negativi sul gioco ma aggiunge
             * un elemento umoristico e di caratterizzazione del mondo di gioco.
             */
            else if (hasFotoSanNicola && hasBobbinaPLA) {
                msg.append("Premi delicatamente la foto contro la bobina PLA...\n");
                msg.append("'San Nicola Astronauta, protettore dei maker e delle stampanti 3D!'\n");
                msg.append("Hai appena creato la prima reliquia tecnologica del collegio!\n");
                msg.append("Gli studenti di ingegneria si raccolgono in preghiera davanti alla tua creazione.\n");
                msg.append("Forse hai inventato una nuova religione: il 'Tecno-Cristianesimo'!\n");
                specialCombinationHandled = true;
            }
            
            /**
             * COMPORTAMENTO DIVERTENTE 2: Gaming per Anziani
             * Questo comportamento non ha effetti negativi sul gioco ma aggiunge
             * un elemento umoristico e di caratterizzazione del mondo di gioco.
             */
            else if (hasBastonAnziani && hasTastiera) {
                msg.append("Usi il bastone per premere i tasti della tastiera...\n");
                msg.append("*Click* *Clack* *Tonk*\n");
                msg.append("Incredibile! Hai appena inventato il 'Gaming per Anziani'!\n");
                msg.append("La tastiera suona come un pianoforte scordato!\n");
                specialCombinationHandled = true;
            }

            // ========== GESTIONE COMPORTAMENTI INDIVIDUALI ==========
            
            /**
             * Elaborazione di oggetti individuali pushable.
             * 
             * Questo blocco viene eseguito SOLO se nessuna combinazione speciale
             * è stata attivata, per evitare output confusi o duplicati.
             * 
             * Per ogni oggetto nell'inventario:
             * - Se è pushable: mostra messaggio generico di pressione
             * - Se non è pushable: informa che non può essere premuto
             */
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
                // Una combinazione speciale è stata gestita
                interacted = true;
            }

            // Messaggio di fallback se nessuna interazione è avvenuta
            if (!interacted) {
                msg.append("Non puoi premere nulla qui.\n");
            }
        }

        return msg.toString();
    }
}
