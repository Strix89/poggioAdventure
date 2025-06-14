/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.poggioadventure.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/* Classe di tipo Entity che serve per definire una collezione di oggetti 
    che però è anch'essa un elemento di gioco. Un esempio per chiarire: serve 
    magari a costruire una cassa presente in una stanza del gioco che contiene 
    al suo interno un'altra serie di oggetti di gioco. La stessa cassa può essere
    raccolta e portata con se nell'invetario. Infatti questa classe estende AdvObject.
*/

/**
 *
 * @author pierpaolo
 */
public class AdvObjectContainer extends AdvObject {

    private static final long serialVersionUID = 1L;
    private List<AdvObject> list = new ArrayList<>();

    /**
     *
     * @param id
     */
    public AdvObjectContainer(int id) {
        super(id);
    }

    /**
     *
     * @param id
     * @param name
     */
    public AdvObjectContainer(int id, String name) {
        super(id, name);
    }

    /**
     *
     * @param id
     * @param name
     * @param description
     */
    public AdvObjectContainer(int id, String name, String description) {
        super(id, name, description);
    }

    /**
     *
     * @param id
     * @param name
     * @param image
     * @param description
     */
    public AdvObjectContainer(int id, String name, String image, String description) {
        super(id, name, image, description);
    }

    /**
     *
     * @param id
     * @param name
     * @param image
     * @param description
     * @param alias
     */
    public AdvObjectContainer(int id, String name, String image, String description, Set<String> alias) {
        super(id, name, image, description, alias);
    }

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
