/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.map.b.adventure.parser;

import di.uniba.map.b.adventure.Utils;
import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.Command;
import java.util.List;
import java.util.Set;

/**
 *
 * @author pierpaolo
 */
public class Parser {

    private final Set<String> stopwords;

    // Costruttore, setta l'insieme di stringhe che non devono essere considerate
    public Parser(Set<String> stopwords) { 
        this.stopwords = stopwords;
    }

    /*
    Controlla se il comando dato in input è presente nella lista di comandi o è associato ad un alias, se si ritorna la posizione del 
    comando nella lista, altrimenti -1
    */ 
    private int checkForCommand(String token, List<Command> commands) {
        for (int i = 0; i < commands.size(); i++) {
            if (commands.get(i).getName().equals(token) || commands.get(i).getAlias().contains(token)) {
                return i;
            }
        }
        return -1;
    }

    /*
    Controlla se l'oggetto dato in input è presente nella lista di oggetti o è associato ad un alias, 
    se si ritorna la posizione dell'oggetto nella lista, altrimenti -1
    */
    private int checkForObject(String token, List<AdvObject> obejcts) {
        for (int i = 0; i < obejcts.size(); i++) {
            if (obejcts.get(i).getName().equals(token) || obejcts.get(i).getAlias().contains(token)) {
                return i;
            }
        }
        return -1;
    }

    /* ATTENZIONE: il parser è implementato in modo abbastanza independete dalla lingua, ma riconosce solo 
    * frasi semplici del tipo <azione> <oggetto> <oggetto>. Eventuali articoli o preposizioni vengono semplicemente
    * rimossi.
     */
    public ParserOutput parse(String command, List<Command> commands, List<AdvObject> objects, List<AdvObject> inventory) {
        List<String> tokens = Utils.parseString(command, stopwords); // Rimuove le parole non significative
        if (!tokens.isEmpty()) { 
            int ic = checkForCommand(tokens.get(0), commands); // Controlla se il primo token è un comando
            if (ic > -1) { // Se il comando è presente
                if (tokens.size() > 1) { // Se ci sono altri token
                    int io = checkForObject(tokens.get(1), objects); // Controlla se il secondo token è un oggetto se si restituisce la posizione dell'oggetto nella lista degli oggetti
                    int ioinv = -1;  // Inizializza la posizione dell'oggetto nell'inventario a -1
                    if (io < 0 && tokens.size() > 2) { // se l'oggetto non è presente e ci sono più di due token
                        io = checkForObject(tokens.get(2), objects); // Controlla se il terzo token è un oggetto
                    }
                    if (io < 0) { // Se l'oggetto non è presente
                        ioinv = checkForObject(tokens.get(1), inventory); // Controlla se il secondo token è un oggetto dell'inventario
                        if (ioinv < 0 && tokens.size() > 2) { // Se l'oggetto dell'inventario non è presente e ci sono più di due token
                            ioinv = checkForObject(tokens.get(2), inventory); // Controlla se il terzo token è un oggetto dell'inventario
                        }
                    }
                    if (io > -1 && ioinv > -1) { // Se l'oggetto esiste ed è presente nell'inventario
                        return new ParserOutput(commands.get(ic), objects.get(io), inventory.get(ioinv)); // Restituisce il comando, l'oggetto e l'oggetto dell'inventario
                    } else if (io > -1) { // Se l'oggetto esiste
                        return new ParserOutput(commands.get(ic), objects.get(io), null); // Restituisce il comando e l'oggetto
                    } else if (ioinv > -1) { // Se l'oggetto dell'inventario esiste
                        return new ParserOutput(commands.get(ic), null, inventory.get(ioinv)); // Restituisce il comando e l'oggetto dell'inventario
                    } else {
                        return new ParserOutput(commands.get(ic), null, null); // Restituisce solo il comando
                    }
                } else {
                    return new ParserOutput(commands.get(ic), null);
                }
            } else {
                return new ParserOutput(null, null); // Se il comando non è presente
            }
        } else {
            return null; // Se non ci sono token
        }
    }

}