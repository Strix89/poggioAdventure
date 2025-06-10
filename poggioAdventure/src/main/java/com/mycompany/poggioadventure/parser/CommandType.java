/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.poggioadventure.parser;

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
    PUT,

    /**
     *
     */
    TALK,

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
    TURN_OFF,

    /**
     *
     */
    NONE;

    /**
     * Restituisce il comando opposto a quello corrente.
     * 
     * @return il comando opposto. Se il comando corrente è NORD, restituisce SOUTH.
     *         Se il comando corrente è SOUTH, restituisce NORD. Se il comando 
     *         corrente è EAST, restituisce WEST. Se il comando corrente è WEST, 
     *         restituisce EAST. Se nessuno di questi casi è soddisfatto, 
     *         restituisce NONE.
     */
    public CommandType getOpposite() {
        switch(this) {
            case NORD: return SOUTH;
            case SOUTH: return NORD;
            case EAST: return WEST;
            case WEST: return EAST;
            default: return NONE;
        }
    }

    public boolean isDirection() {
        return this == NORD || this == SOUTH || this == EAST || this == WEST;
    }

    /**
     * Verifica se il comando rappresenta un movimento (direzioni + WALK_TO)
     * @return true se è un comando di movimento
     */
    public boolean isMovement() {
        return isDirection() || this == WALK_TO;
    }
}
