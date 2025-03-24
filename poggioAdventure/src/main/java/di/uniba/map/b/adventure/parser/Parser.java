package di.uniba.map.b.adventure.parser;

import di.uniba.map.b.adventure.Utils;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.Command;

import java.util.*;

public class Parser {

    private final Set<String> stopwords;
    private final Set<String> conjunctions = new HashSet<>(Arrays.asList("e", "poi", "dopo", "quindi", "inoltre", "successivamente"));

    public Parser(Set<String> stopwords) {
        this.stopwords = stopwords;
    }

    private int checkForCommand(String token, List<Command> commands) {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).getName().equals(token) || commands.get(i).getAlias().contains(token)) {
                return i;
            }
        }
        return -1;
    }

    private int checkForObject(String token, List<AdvObject> objects) {
        for (int i = 0; i < objects.size(); i++) {
            if (objects.get(i).getName().equals(token) || objects.get(i).getAlias().contains(token)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Divide i token in più comandi solo se la congiunzione è seguita da un comando valido.
     */
    private List<List<String>> splitCommands(List<String> tokens, List<Command> commands) {
        List<List<String>> result = new ArrayList<>();
        List<String> current = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i).toLowerCase();

            if (conjunctions.contains(token)) {
                if (i + 1 < tokens.size()) {
                    String next = tokens.get(i + 1).toLowerCase();
                    if (checkForCommand(next, commands) >= 0) {
                        if (!current.isEmpty()) {
                            result.add(new ArrayList<>(current));
                            current.clear();
                        }
                        continue; // Salta la congiunzione
                    }
                }
            }

            current.add(token);
        }

        if (!current.isEmpty()) {
            result.add(current);
        }

        return result;
    }

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
     * Analizza una stringa che può contenere più comandi separati da congiunzioni
     * e oggetti multipli all'interno di ciascun comando.
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
