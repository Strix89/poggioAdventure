package com.mycompany.poggioadventure.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/* Sembra essere una EntityClass per definire e costruire gli oggetti di gioco
    descrizione, alias e operazioni e azioni (collegate ai comandi di gioco) 
    applicabili sull'oggetto.
*/

/**
 *
 * @author pierpaolo
 */
public class AdvObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;

    private String name;

    private String description;
    
    private Set<String> alias;

    private boolean openable = false;

    private boolean pickupable = true;

    private boolean pushable = false;

    private boolean open = false;

    private boolean push = false;
    
    private String imagePath = null;
    
    private boolean obscureImage = false;


    /**
     *
     * @param id
     */
    public AdvObject(int id) {
        this.id = id;
    }

    /**
     *
     * @param id
     * @param name
     */
    public AdvObject(int id, String name) {
        this.id = id;
        this.name = name;
        this.alias = new HashSet<>();
        this.alias.add(name);
    }
    
    /**
     *
     * @param id
     * @param name
     * @param description
     */
    public AdvObject(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.alias = new HashSet<>();
        this.alias.add(name);
    }

    /**
     *
     * @param id
     * @param name
     * @param image
     * @param description
     */
    public AdvObject(int id, String name, String image, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = image;
        this.alias = new HashSet<>();
        this.alias.add(name);
    }


    /**
     *
     * @param id
     * @param name
     * @param image
     * @param description
     * @param alias
     */
    public AdvObject(int id, String name, String image, String description, Set<String> alias) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.alias = alias;
        this.imagePath = image;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     */
    public boolean isOpenable() {
        return openable;
    }

    /**
     *
     * @param openable
     */
    public void setOpenable(boolean openable) {
        this.openable = openable;
    }

    /**
     *
     * @return
     */
    public boolean isPickupable() {
        return pickupable;
    }

    /**
     *
     * @param pickupable
     */
    public void setPickupable(boolean pickupable) {
        this.pickupable = pickupable;
    }

    /**
     *
     * @return
     */
    public boolean isPushable() {
        return pushable;
    }

    /**
     *
     * @param pushable
     */
    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }

    /**
     *
     * @return
     */
    public boolean isOpen() {
        return open;
    }

    /**
     *
     * @param open
     */
    public void setOpen(boolean open) {
        this.open = open;
    }

    /**
     *
     * @return
     */
    public boolean isPush() {
        return push;
    }

    /**
     *
     * @param push
     */
    public void setPush(boolean push) {
        this.push = push;
    }

    /**
     *
     * @return
     */
    public Set<String> getAlias() {
        return alias;
    }

    /**
     *
     * @param alias
     */
    public void setAlias(Set<String> alias) {
        if (this.alias == null || this.alias.isEmpty()) {
            this.alias = alias;
        } else {
            this.alias.addAll(alias);
        }
    }
    
    /**
     *
     * @param alias
     */
    public void setAlias(String[] alias) {
        if (this.alias == null || this.alias.isEmpty()) {
            this.alias = new HashSet<>(Arrays.asList(alias));
        } else {
            this.alias.addAll(Arrays.asList(alias));
        }
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.id;
        return hash;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AdvObject other = (AdvObject) obj;
        return this.id == other.id;
    }   

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isObscureImage() {
        return obscureImage;
    }

    public void setObscureImage(boolean obscureImage) {
        this.obscureImage = obscureImage;
    }
}
