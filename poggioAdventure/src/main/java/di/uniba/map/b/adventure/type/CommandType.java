/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package di.uniba.map.b.adventure.type;

/* Enum che descrive ed "enumera" i vari comandi utilizzabili in GIOCO
 Dovremmo differenziare però tra COMANDI IN GIOCO e COMANDI DI GIOCO (es.
 carica partita, nuova partita, salva & esci dal gioco)
*/

/**
 *
 * @author pierpaolo
 */
public enum CommandType {

    /**
     *
     */
    SAVE,

    /**
     *
     */
    END,

    /**
     *
     */
    INVENTORY,

    /**
     *
     */
    NORD,

    /**
     *
     */
    SOUTH,

    /**
     *
     */
    EAST,

    /**
     *
     */
    WEST,

    /**
     *
     */
    OPEN,

    /**
     *
     */
    CLOSE,

    /**
     *
     */
    PUSH,

    /**
     *
     */
    PULL,

    /**
     *
     */
    WALK_TO,

    /**
     *
     */
    PICK_UP,

    /**
     *
     */
    TALK_TO,

    /**
     *
     */
    GIVE,

    /**
     *
     */
    USE,

    /**
     *
     */
    LOOK_AT,

    /**
     *
     */
    TURN_ON,

    /**
     *
     */
    TURN_OFF
}
