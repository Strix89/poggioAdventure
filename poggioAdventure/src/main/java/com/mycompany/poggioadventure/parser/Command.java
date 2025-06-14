package com.mycompany.poggioadventure.parser;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entità comando con tipo, nome e sistema alias per riconoscimento input flessibile.
 * 
 * <p>Rappresenta comando di gioco identificato da tipo enum e nomi alternativi
 * per supportare variazioni linguistiche nell'input utente. Implementa
 * uguaglianza basata esclusivamente su tipo per matching efficiente.
 * 
 * <p><b>Caratteristiche:</b>
 * <ul>
 *   <li>Tipo comando immutabile per classificazione</li>
 *   <li>Nome primario e set alias per riconoscimento</li>
 *   <li>Auto-inclusione nome negli alias per coerenza</li>
 *   <li>Equality basata su tipo per performance</li>
 *   <li>Serializzazione per persistenza parsing</li>
 * </ul>
 * 
 * <p><b>Pattern:</b> Value Object per identità comando, Entity per
 * raggruppamento varianti linguistiche.
 */
public class Command implements Serializable {

    /** Tipo comando immutabile per classificazione */
    private final CommandType type;

    /** Nome primario del comando */
    private final String name;

    /** Set alias per riconoscimento varianti input */
    private Set<String> alias;

    /**
     * Costruttore base con tipo e nome.
     * Inizializza automaticamente alias contenente il nome.
     * 
     * @param type Tipo comando per classificazione
     * @param name Nome primario comando
     */
    public Command(CommandType type, String name) {
        this.type = type;
        this.name = name;
        this.alias = new HashSet<>();
        this.alias.add(name);
    }

    /**
     * Costruttore completo con alias predefiniti.
     * Aggiunge automaticamente nome agli alias per coerenza.
     * 
     * @param type Tipo comando per classificazione
     * @param name Nome primario comando
     * @param alias Set alias iniziali
     */
    public Command(CommandType type, String name, Set<String> alias) {
        this.type = type;
        this.name = name;
        this.alias = new HashSet<>(alias);
        this.alias.add(name);
    }

    /** Restituisce nome primario comando */
    public String getName() {
        return name;
    }

    /** Restituisce set completo alias per matching */
    public Set<String> getAlias() {
        return alias;
    }

    /**
     * Aggiorna alias da set preservando nome primario.
     * 
     * @param alias Nuovi alias da impostare
     */
    public void setAlias(Set<String> alias) {
        this.alias = new HashSet<>(alias);
        this.alias.add(name);
    }

    /**
     * Aggiorna alias da array preservando nome primario.
     * 
     * @param alias Array nuovi alias
     */
    public void setAlias(String[] alias) {
        this.alias = new HashSet<>(Arrays.asList(alias));
        this.alias.add(name);
    }

    /** Restituisce tipo comando immutabile */
    public CommandType getType() {
        return type;
    }

    /** Hash code basato su tipo per collezioni efficienti */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.type);
        return hash;
    }

    /**
     * Uguaglianza basata esclusivamente su tipo comando.
     * Permette matching indipendente da nome/alias specifici.
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
        final Command other = (Command) obj;
        if (this.type != other.type) {
            return false;
        }
        return true;
    }
}
