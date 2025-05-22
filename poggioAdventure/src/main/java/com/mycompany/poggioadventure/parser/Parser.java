package com.mycompany.poggioadventure.parser;

import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.model.AdvObject;
import com.mycompany.poggioadventure.model.AdvObjectContainer;

import java.util.*;

/**
 * La classe Parser è responsabile per l'analisi (parsing) dei comandi immessi dall'utente.
 * Essa si occupa di riconoscere i comandi, gli oggetti e le congiunzioni tra comandi, restituendo i relativi oggetti e comandi 
 * che il motore del gioco può eseguire.
 * 
 * I comandi possono essere:
 * - Un singolo comando con un oggetto
 * - Più comandi concatenati con congiunzioni come "e", "poi", "dopo", "quindi", etc.
 * - Oggetti multipli per ciascun comando
 */
public class Parser {

    private final Set<String> stopwords; // Insieme di parole da ignorare (es. articoli, preposizioni)
    private final Set<String> conjunctions = new HashSet<>(Arrays.asList("e", "poi", "dopo", "quindi", "inoltre", "successivamente"));

    /**
     * Costruttore della classe Parser. Inizializza il set di stopwords.
     *
     * @param stopwords Un insieme di parole da ignorare durante il parsing.
     */
    public Parser(Set<String> stopwords) {
        this.stopwords = stopwords;
    }

    /**
     * Controlla se un token corrisponde ad un comando valido.
     *
     * @param token La parola da verificare.
     * @param commands La lista di comandi disponibili nel gioco.
     * @return L'indice del comando se trovato, altrimenti -1.
     */
    private int checkForCommand(String token, List<Command> commands) {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).getName().equals(token) || commands.get(i).getAlias().contains(token)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Divide la lista di token in più comandi separati da congiunzioni.
     * La congiunzione deve essere seguita da un comando valido per dividerli.
     *
     * @param tokens La lista di token derivante dal comando utente.
     * @param commands La lista di comandi disponibili.
     * @return Una lista di liste di token, ognuna contenente un comando separato.
     */
    private List<List<String>> splitCommands(List<String> tokens, List<Command> commands) {
        List<List<String>> result = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i).toLowerCase();
            if (conjunctions.contains(token) && i + 1 < tokens.size()) {
                String next = tokens.get(i + 1).toLowerCase();
                if (checkForCommand(next, commands) >= 0) {
                    if (!current.isEmpty()) result.add(current);
                    current = new ArrayList<>();
                    continue;
                }
            }
            current.add(token);
        }
        if (!current.isEmpty()) result.add(current);
        return result;
    }

    /**
     * Cerca oggetti nella lista fornita e, se trova contenitori aperti,
     * cerca ricorsivamente anche dentro i loro contenuti.
     *
     * @param tokens lista di token oggetto da cercare
     * @param objects lista di oggetti (in stanza o inventario)
     * @return lista di oggetti trovati corrispondenti ai token
     */
    private List<AdvObject> findMultipleObjects(List<String> tokens, List<AdvObject> objects) {
        List<AdvObject> foundObjects = new ArrayList<>();
        for (int i = 1; i < tokens.size(); i++) {
            String token = tokens.get(i).toLowerCase();
            // Cerca negli oggetti diretti nella stanza/inventario
            for (AdvObject obj : objects) {
                if (obj.getName().equalsIgnoreCase(token) || obj.getAlias().contains(token)) {
                    if (!foundObjects.contains(obj)) {
                        foundObjects.add(obj);
                    }
                }
            }
            // Cerca nei contenitori aperti
            for (AdvObject obj : objects) {
                if (obj instanceof AdvObjectContainer && ((AdvObjectContainer) obj).isOpen()) {
                    for (AdvObject innerObj : ((AdvObjectContainer) obj).getList()) {
                        if (innerObj.getName().equalsIgnoreCase(token) || innerObj.getAlias().contains(token)) {
                            if (!foundObjects.contains(innerObj)) {
                                foundObjects.add(innerObj);
                            }
                        }
                    }
                }
            }
        }
        return foundObjects;
    }


    /**
     * Analizza un singolo comando, cercando di associare un comando, un oggetto e un oggetto dell'inventario.
     *
     * @param tokens La lista di token derivante dal comando utente.
     * @param commands La lista di comandi disponibili.
     * @param objects La lista di oggetti presenti nella stanza.
     * @param inventory La lista di oggetti nell'inventario del giocatore.
     * @return Un oggetto ParserOutput contenente il comando e gli oggetti identificati.
     */
    private ParserOutput parseSingleCommand(List<String> tokens, List<Command> commands,
                                            List<AdvObject> objects, List<AdvObject> inventory) {
        if (tokens.isEmpty()) {
            return null;
        }

        int ic = checkForCommand(tokens.get(0), commands);
        if (ic < 0) {
            return new ParserOutput(null, null);
        }

        Command cmd = commands.get(ic);
        List<AdvObject> roomObjects = findMultipleObjects(tokens, objects);
        List<AdvObject> invObjects = findMultipleObjects(tokens, inventory);


        if (!roomObjects.isEmpty() && !invObjects.isEmpty()) {
            return new ParserOutput(cmd, roomObjects.get(0), invObjects.get(0), roomObjects, invObjects);
        } else if (!roomObjects.isEmpty()) {
            return new ParserOutput(cmd, roomObjects.get(0), null, roomObjects, new ArrayList<>());
        } else if (!invObjects.isEmpty()) {
            return new ParserOutput(cmd, null, invObjects.get(0), new ArrayList<>(), invObjects);
        } else {
            return new ParserOutput(cmd, null, null);
        }
    }

    /**
     * Analizza una stringa contenente uno o più comandi separati da congiunzioni.
     * Ogni comando può avere uno o più oggetti associati.
     *
     * @param command La stringa di comando da parsare.
     * @param commands La lista di comandi disponibili.
     * @param objects La lista di oggetti nella stanza.
     * @param inventory La lista di oggetti nell'inventario del giocatore.
     * @return Una lista di ParserOutput, uno per ogni comando parsato.
     */
    public List<ParserOutput> parseMultiple(String command, List<Command> commands,
                                            List<AdvObject> objects, List<AdvObject> inventory) {
        List<String> allTokens = Utils.parseString(command, stopwords);
        List<List<String>> splitCmds = splitCommands(allTokens, commands); // <- FIX: passa i comandi

        List<ParserOutput> outputs = new ArrayList<>();

        for (List<String> cmdTokens : splitCmds) {
            ParserOutput output = parseSingleCommand(cmdTokens, commands, objects, inventory);
            if (output != null) {
                outputs.add(output);
            }
        }

        return outputs;
    }

    /**
     * Analizza una singola stringa di comando e restituisce il primo comando valido.
     *
     * @param command La stringa di comando da parsare.
     * @param commands La lista di comandi disponibili.
     * @param objects La lista di oggetti nella stanza.
     * @param inventory La lista di oggetti nell'inventario del giocatore.
     * @return Il primo ParserOutput valido.
     */
    public ParserOutput parse(String command, List<Command> commands,
                              List<AdvObject> objects, List<AdvObject> inventory) {
        List<ParserOutput> outputs = parseMultiple(command, commands, objects, inventory);
        if (!outputs.isEmpty()) {
            return outputs.get(0);
        } else {
            return null;
        }
    }
}
