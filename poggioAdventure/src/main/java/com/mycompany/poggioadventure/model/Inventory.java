/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.poggioadventure.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* Classe Entity che serve a definire e costruire l'invetario utilizzabile in 
    gioco. Possono essere aggiunti AdvObject oppure settare una lista di AdvObject.
    Questo significa che è possibile anche passargli un container.
*/

/**
 *
 * @author pierpaolo
 */
public class Inventory implements Serializable{

    private List<AdvObject> list = new ArrayList<>();

    /**
     *
     * @return
     */
    public List<AdvObject> getList() {
        return list;
    }

    /**
     *
     * @param list
     */
    public void setList(List<AdvObject> list) {
        this.list = list;
    }

    /**
     *
     * @param o
     */
    public void add(AdvObject o) {
        list.add(o);
        
    }

    /**
     *
     * @param o
     */
    public void remove(AdvObject o) {
        list.remove(o);
    }
}
