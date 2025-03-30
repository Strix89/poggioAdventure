package di.uniba.map.b.adventure.parser;

import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.Command;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe migliorata per rappresentare l'output del parser che supporta
 * comandi multipli e operazioni su più oggetti.
 *
 * @author pierpaolo
 */
public class ParserOutput implements Serializable {

    private Command command;
    private AdvObject object;
    private AdvObject invObject;
    
    // Liste di oggetti per supportare comandi multipli su più oggetti
    private List<AdvObject> objects = new ArrayList<>();
    private List<AdvObject> invObjects = new ArrayList<>();

    /**
     * Costruttore che inizializza il ParserOutput con il comando e l'oggetto
     *
     * @param command Comando riconosciuto
     * @param object Oggetto principale riconosciuto
     */
    public ParserOutput(Command command, AdvObject object) {
        this.command = command;
        this.object = object;
        if (object != null) {
            this.objects.add(object);
        }
    }

    /**
     * Costruttore che inizializza il ParserOutput con il comando, l'oggetto e l'oggetto dell'inventario
     *
     * @param command Comando riconosciuto
     * @param object Oggetto principale riconosciuto
     * @param invObject Oggetto dell'inventario riconosciuto
     */
    public ParserOutput(Command command, AdvObject object, AdvObject invObject) {
        this.command = command;
        this.object = object;
        this.invObject = invObject;
        if (object != null) {
            this.objects.add(object);
        }
        if (invObject != null) {
            this.invObjects.add(invObject);
        }
    }
    
    /**
     * Costruttore avanzato che supporta più oggetti
     * 
     * @param command Comando riconosciuto
     * @param object Oggetto principale riconosciuto
     * @param invObject Oggetto dell'inventario riconosciuto
     * @param objects Lista di oggetti riconosciuti
     * @param invObjects Lista di oggetti dell'inventario riconosciuti
     */
    public ParserOutput(Command command, AdvObject object, AdvObject invObject, 
                        List<AdvObject> objects, List<AdvObject> invObjects) {
        this.command = command;
        this.object = object;
        this.invObject = invObject;
        if (objects != null) {
            this.objects = objects;
        }
        if (invObjects != null) {
            this.invObjects = invObjects;
        }
    }

    /**
     * @return Comando riconosciuto
     */
    public Command getCommand() {
        return command;
    }

    /**
     * @param command Comando da impostare
     */
    public void setCommand(Command command) {
        this.command = command;
    }

    /**
     * @return Oggetto principale riconosciuto
     */
    public AdvObject getObject() {
        return object;
    }

    /**
     * @param object Oggetto da impostare
     */
    public void setObject(AdvObject object) {
        this.object = object;
    }

    /**
     * @return Oggetto dell'inventario riconosciuto
     */
    public AdvObject getInvObject() {
        return invObject;
    }

    /**
     * @param invObject Oggetto dell'inventario da impostare
     */
    public void setInvObject(AdvObject invObject) {
        this.invObject = invObject;
    }
    
    /**
     * @return Lista di tutti gli oggetti riconosciuti
     */
    public List<AdvObject> getObjects() {
        return objects;
    }
    
    /**
     * @param objects Lista di oggetti da impostare
     */
    public void setObjects(List<AdvObject> objects) {
        this.objects = objects;
    }
    
    /**
     * @return Lista di tutti gli oggetti dell'inventario riconosciuti
     */
    public List<AdvObject> getInvObjects() {
        return invObjects;
    }
    
    /**
     * @param invObjects Lista di oggetti dell'inventario da impostare
     */
    public void setInvObjects(List<AdvObject> invObjects) {
        this.invObjects = invObjects;
    }
    
    /**
     * Controlla se sono stati riconosciuti più oggetti
     * 
     * @return true se ci sono più oggetti, false altrimenti
     */
    public boolean hasMultipleObjects() {
        return objects.size() > 1;
    }
    
    /**
     * Controlla se sono stati riconosciuti più oggetti dall'inventario
     * 
     * @return true se ci sono più oggetti dall'inventario, false altrimenti
     */
    public boolean hasMultipleInvObjects() {
        return invObjects.size() > 1;
    }
}