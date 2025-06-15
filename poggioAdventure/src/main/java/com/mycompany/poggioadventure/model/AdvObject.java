package com.mycompany.poggioadventure.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Entità base per oggetti interattivi nel mondo di gioco.
 * 
 * <p>Definisce proprietà fondamentali e comportamenti per tutti gli elementi
 * con cui il giocatore può interagire. Supporta serializzazione per salvataggio
 * stato e sistema di alias per riconoscimento comandi naturali.
 * 
 * <p><b>Caratteristiche principali:</b>
 * <ul>
 *   <li>Identificazione univoca tramite ID immutabile</li>
 *   <li>Sistema alias per riconoscimento input utente flessibile</li>
 *   <li>Proprietà comportamentali (raccoglibile, apribile, spingibile)</li>
 *   <li>Stati dinamici (aperto, spinto, visibile)</li>
 *   <li>Supporto immagini con controllo visibilità</li>
 *   <li>Serializzazione completa per persistenza</li>
 * </ul>
 * 
 */
public class AdvObject implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identificatore univoco immutabile dell'oggetto */
    private final int id;

    /** Nome principale dell'oggetto */
    private String name;

    /** Descrizione dettagliata per esaminazione */
    private String description;
    
    /** Set di alias per riconoscimento comando utente */
    private Set<String> alias;

    /** Flag: oggetto può essere aperto */
    private boolean openable = false;

    /** Flag: oggetto può essere raccolto */
    private boolean pickupable = true;

    /** Flag: oggetto può essere spinto */
    private boolean pushable = false;

    /** Stato: oggetto attualmente aperto */
    private boolean open = false;

    /** Stato: oggetto è stato spinto */
    private boolean push = false;
    
    /** Path immagine per rappresentazione visuale */
    private String imagePath = null;
    
    /** Flag: immagine deve essere oscurata (modalità CLI) */
    private boolean obscureImage = false;

    /** Flag: oggetto visibile al giocatore */
    private boolean isVisible = true;

    /**
     * Costruttore minimale con solo ID.
     * 
     * @param id Identificatore univoco oggetto
     */
    public AdvObject(int id) {
        this.id = id;
    }

    /**
     * Costruttore con ID e nome.
     * Inizializza automaticamente alias con il nome fornito.
     * 
     * @param id Identificatore univoco
     * @param name Nome oggetto
     */
    public AdvObject(int id, String name) {
        this.id = id;
        this.name = name;
        this.alias = new HashSet<>();
        this.alias.add(name);
    }
    
    /**
     * Costruttore standard con identificazione e descrizione.
     * 
     * @param id Identificatore univoco
     * @param name Nome oggetto
     * @param description Descrizione dettagliata
     */
    public AdvObject(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.alias = new HashSet<>();
        this.alias.add(name);
    }

    /**
     * Costruttore completo con supporto immagine.
     * 
     * @param id Identificatore univoco
     * @param name Nome oggetto
     * @param description Descrizione dettagliata
     * @param image Path file immagine
     */
    public AdvObject(int id, String name, String description, String image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = image;
        this.alias = new HashSet<>();
        this.alias.add(name);
    }

    /**
     * Costruttore avanzato con alias personalizzati.
     * 
     * @param id Identificatore univoco
     * @param name Nome oggetto
     * @param description Descrizione dettagliata
     * @param image Path file immagine
     * @param alias Set alias predefiniti
     */
    public AdvObject(int id, String name, String description, String image, Set<String> alias) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.alias = alias;
        this.imagePath = image;
    }

    /** Restituisce nome principale oggetto */
    public String getName() {
        return name;
    }

    /** Imposta nome principale oggetto */
    public void setName(String name) {
        this.name = name;
    }

    /** Restituisce descrizione dettagliata per comando 'osserva' */
    public String getDescription() {
        return description;
    }

    /** Aggiorna descrizione oggetto */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Verifica se oggetto può essere aperto */
    public boolean isOpenable() {
        return openable;
    }

    /** Configura capacità apertura oggetto */
    public void setOpenable(boolean openable) {
        this.openable = openable;
    }

    /** Verifica se oggetto può essere raccolto */
    public boolean isPickupable() {
        return pickupable;
    }

    /** Configura capacità raccolta oggetto */
    public void setPickupable(boolean pickupable) {
        this.pickupable = pickupable;
    }

    /** Verifica se oggetto può essere spinto */
    public boolean isPushable() {
        return pushable;
    }

    /** Configura capacità spinta oggetto */
    public void setPushable(boolean pushable) {
        this.pushable = pushable;
    }

    /** Verifica stato apertura corrente */
    public boolean isOpen() {
        return open;
    }

    /** Aggiorna stato apertura oggetto */
    public void setOpen(boolean open) {
        this.open = open;
    }

    /** Verifica se oggetto è stato spinto */
    public boolean isPush() {
        return push;
    }

    /** Aggiorna stato spinta oggetto */
    public void setPush(boolean push) {
        this.push = push;
    }

    /** Restituisce set alias per riconoscimento comandi */
    public Set<String> getAlias() {
        return alias;
    }

    /**
     * Aggiorna alias da set, preservando esistenti se presenti.
     * 
     * @param alias Nuovi alias da aggiungere
     */
    public void setAlias(Set<String> alias) {
        if (this.alias == null || this.alias.isEmpty()) {
            this.alias = alias;
        } else {
            this.alias.addAll(alias);
        }
    }
    
    /**
     * Aggiorna alias da array, preservando esistenti se presenti.
     * 
     * @param alias Array nuovi alias
     */
    public void setAlias(String[] alias) {
        if (this.alias == null || this.alias.isEmpty()) {
            this.alias = new HashSet<>(Arrays.asList(alias));
        } else {
            this.alias.addAll(Arrays.asList(alias));
        }
    }

    /** Restituisce identificatore univoco immutabile */
    public int getId() {
        return id;
    }

    /**
     * Hash code basato su ID per collezioni efficienti.
     * Due oggetti con stesso ID sono considerati identici.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.id;
        return hash;
    }

    /**
     * Uguaglianza basata esclusivamente su ID.
     * Implementazione ottimizzata per performance.
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

    /** Restituisce path file immagine per rendering */
    public String getImagePath() {
        return imagePath;
    }

    /** Configura path immagine per rappresentazione visuale */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /** Verifica se immagine deve essere oscurata (modalità CLI) */
    public boolean isObscureImage() {
        return obscureImage;
    }

    /** Configura oscuramento immagine per adattamento interfaccia */
    public void setObscureImage(boolean obscureImage) {
        this.obscureImage = obscureImage;
    }

    /** Verifica visibilità oggetto al giocatore */
    public boolean isVisible() {
        return isVisible;
    }

    /** Controlla visibilità oggetto nel mondo di gioco */
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
}
