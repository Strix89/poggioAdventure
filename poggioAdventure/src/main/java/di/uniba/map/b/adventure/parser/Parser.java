package di.uniba.map.b.adventure.parser;

import di.uniba.map.b.adventure.Utils;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.Command;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parser migliorato che supporta comandi multipli e operazioni su più oggetti.
 * 
 * @author pierpaolo
 */
public class Parser {

    private final Set<String> stopwords;
    // Congiunzioni che possono separare comandi
    private final Set<String> conjunctions = new HashSet<>(Arrays.asList("e", "poi", "dopo", "quindi", "inoltre", "successivamente"));

    /**
     * Costruttore, setta l'insieme di stringhe che non devono essere considerate
     *
     * @param stopwords Lista di parole da ignorare durante il parsing
     */
    public Parser(Set<String> stopwords) {
        this.stopwords = stopwords;
    }

    /**
     * Controlla se il comando dato in input è presente nella lista di comandi o
     * è associato ad un alias, se si ritorna la posizione del comando nella
     * lista, altrimenti -1
     *
     * @param token Token da verificare
     * @param commands Lista dei comandi disponibili
     * @return Indice del comando nella lista o -1 se non trovato
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
     * Controlla se l'oggetto dato in input è presente nella lista di oggetti o
     * è associato ad un alias, se si ritorna la posizione dell'oggetto nella
     * lista, altrimenti -1
     *
     * @param token Token da verificare
     * @param objects Lista degli oggetti
     * @return Indice dell'oggetto nella lista o -1 se non trovato
     */
    private int checkForObject(String token, List<AdvObject> objects) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equals(token) || objects.get(i).getAlias().contains(token)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Divide la stringa di input in più comandi separati da congiunzioni
     *
     * @param tokens Lista di token da analizzare
     * @return Lista di liste di token, dove ogni lista rappresenta un comando
     */
    private List<List<String>> splitCommands(List<String> tokens) {
        List<List<String>> commands = new ArrayList<>();
        List<String> currentCommand = new ArrayList<>();

        for (String token : tokens) {
            if (conjunctions.contains(token.toLowerCase())) {
                if (!currentCommand.isEmpty()) {
                    commands.add(new ArrayList<>(currentCommand));
                    currentCommand.clear();
                }
            } else {
                currentCommand.add(token);
            }
        }

        if (!currentCommand.isEmpty()) {
            commands.add(currentCommand);
        }

        return commands;
    }

    /**
     * Cerca più oggetti all'interno di una frase
     *
     * @param tokens Lista di token da analizzare
     * @param objects Lista degli oggetti disponibili
     * @return Lista degli oggetti trovati
     */
    private List<AdvObject> findMultipleObjects(List<String> tokens, List<AdvObject> objects) {
        List<AdvObject> foundObjects = new ArrayList<>();
        for (int i = 1; i < tokens.size(); i++) {
            int objectIndex = checkForObject(tokens.get(i), objects);
            if (objectIndex >= 0) {
                foundObjects.add(objects.get(objectIndex));
            }
        }
        return foundObjects;
    }

    /**
     * Analizza un singolo comando e restituisce il ParserOutput corrispondente
     *
     * @param tokens Token che compongono il comando
     * @param commands Lista dei comandi disponibili
     * @param objects Lista degli oggetti nella stanza
     * @param inventory Lista degli oggetti nell'inventario
     * @return ParserOutput con comando e oggetti identificati
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
        
        // Troviamo oggetti multipli nella stanza
        List<AdvObject> roomObjects = findMultipleObjects(tokens, objects);
        // Troviamo oggetti multipli nell'inventario
        List<AdvObject> invObjects = findMultipleObjects(tokens, inventory);
        
        // Se abbiamo trovato almeno un oggetto nella stanza e uno nell'inventario
        if (!roomObjects.isEmpty() && !invObjects.isEmpty()) {
            return new ParserOutput(cmd, roomObjects.get(0), invObjects.get(0), roomObjects, invObjects);
        } 
        // Se abbiamo trovato solo oggetti nella stanza
        else if (!roomObjects.isEmpty()) {
            return new ParserOutput(cmd, roomObjects.get(0), null, roomObjects, new ArrayList<>());
        } 
        // Se abbiamo trovato solo oggetti nell'inventario
        else if (!invObjects.isEmpty()) {
            return new ParserOutput(cmd, null, invObjects.get(0), new ArrayList<>(), invObjects);
        } 
        // Nessun oggetto trovato
        else {
            return new ParserOutput(cmd, null, null);
        }
    }

    /**
     * Metodo principale per l'analisi dei comandi dell'utente.
     * Supporta comandi multipli separati da congiunzioni e operazioni su più oggetti.
     *
     * @param command Comando inserito dall'utente
     * @param commands Lista dei comandi disponibili
     * @param objects Lista degli oggetti nella stanza
     * @param inventory Lista degli oggetti nell'inventario
     * @return ParserOutput con i risultati dell'analisi
     */
    public List<ParserOutput> parseMultiple(String command, List<Command> commands, 
                                          List<AdvObject> objects, List<AdvObject> inventory) {
        // Rimuovi le stopword ma conserva le congiunzioni
        List<String> allTokens = Utils.parseString(command, stopwords);
        // Dividi in comandi separati
        List<List<String>> splitCmds = splitCommands(allTokens);
        
        List<ParserOutput> outputs = new ArrayList<>();
        
        // Analizza ogni comando separatamente
        for (List<String> cmdTokens : splitCmds) {
            ParserOutput output = parseSingleCommand(cmdTokens, commands, objects, inventory);
            if (output != null) {
                outputs.add(output);
            }
        }
        
        return outputs;
    }

    /**
     * Metodo di compatibilità con il vecchio parser.
     * Restituisce solo il primo ParserOutput per mantenere la compatibilità.
     *
     * @param command Comando inserito dall'utente
     * @param commands Lista dei comandi disponibili
     * @param objects Lista degli oggetti nella stanza
     * @param inventory Lista degli oggetti nell'inventario
     * @return ParserOutput con i risultati dell'analisi del primo comando
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