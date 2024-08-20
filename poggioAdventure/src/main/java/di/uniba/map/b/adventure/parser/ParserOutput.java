/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.map.b.adventure.parser;

import di.uniba.map.b.adventure.type.AdvObject;
import di.uniba.map.b.adventure.type.Command;

/**
 *
 * @author pierpaolo
 */
public class ParserOutput {

    private Command command;

    private AdvObject object;
    
    private AdvObject invObject; // oggetto di tipo AdvObject che rappresenta l'inventario

    /*
     * Costruttore che inizializza il ParserOutput con il comando e l'oggetto
    */
    public ParserOutput(Command command, AdvObject object) {
        this.command = command;
        this.object = object;
    }

    /*
     * Costruttore che inizializza il ParserOutput con il comando, l'oggetto e l'oggetto dell'inventario
    */
    public ParserOutput(Command command, AdvObject object, AdvObject invObejct) {
        this.command = command;
        this.object = object;
        this.invObject = invObejct;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public AdvObject getObject() {
        return object;
    }

    public void setObject(AdvObject object) {
        this.object = object;
    }

    public AdvObject getInvObject() {
        return invObject;
    }

    public void setInvObject(AdvObject invObject) {
        this.invObject = invObject;
    }

}