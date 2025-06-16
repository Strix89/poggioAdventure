package com.mycompany.poggioadventure.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility per la creazione di copie profonde (deep clone) di oggetti attraverso serializzazione.
 * 
 * Questa classe fornisce metodi statici per clonare oggetti garantendo che tutte le
 * riferimenti annidati vengano duplicati correttamente, creando istanze completamente
 * indipendenti dall'originale. Particolarmente utile per:
 * - Isolamento di stato durante salvataggi
 * - Creazione di snapshot per checkpoint
 * - Prevenzione di modifiche accidentali a oggetti condivisi
 */
public class CloneUtils {
    
    /**
     * Costruttore privato per impedire l'istanziazione di questa classe utility.
     * Pattern standard per classi con soli metodi statici.
     */
    private CloneUtils() {
        throw new UnsupportedOperationException("Questa classe utility non può essere istanziata");
    }
    
    /**
     * Esegue una copia profonda di un oggetto serializzabile.
     * 
     * Il processo utilizza la serializzazione Java per creare una copia
     * completamente indipendente dall'originale, con tutti i riferimenti
     * interni correttamente duplicati.
     * 
     * @param <T> Tipo dell'oggetto da clonare (deve implementare Serializable)
     * @param original Oggetto originale da clonare
     * @return Copia profonda dell'oggetto
     * @throws CloneException se si verificano errori durante la serializzazione/deserializzazione
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepClone(T original) {
        if (original == null) {
            return null;
        }
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(original);
            oos.close();
            
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            T cloned = (T) ois.readObject();
            ois.close();
            
            return cloned;
        } catch (IOException | ClassNotFoundException e) {
            throw new CloneException("Errore durante la clonazione dell'oggetto: " + e.getMessage(), e);
        }
    }
    
    /**
     * Crea una copia profonda di una lista di oggetti serializzabili.
     * 
     * Clona ogni elemento della lista individualmente, mantenendo la stessa
     * sequenza ma con oggetti completamente indipendenti dagli originali.
     * 
     * @param <T> Tipo degli elementi nella lista (devono implementare Serializable)
     * @param originalList Lista originale da clonare
     * @return Nuova lista contenente copie profonde di tutti gli elementi
     * @throws CloneException se la clonazione di un elemento fallisce
     */
    public static <T extends Serializable> List<T> deepCloneList(List<T> originalList) {
        if (originalList == null) {
            return null;
        }
        
        List<T> clonedList = new ArrayList<>();
        for (T item : originalList) {
            clonedList.add(deepClone(item));
        }
        return clonedList;
    }
    
    /**
     * Versione fault-tolerant del deep clone che non propaga eccezioni.
     * 
     * Tenta di clonare l'oggetto, ma in caso di errore ritorna l'originale invece
     * di propagare l'eccezione. Utile in contesti dove la clonazione è preferibile
     * ma non critica per la funzionalità dell'applicazione.
     * 
     * @param <T> Tipo dell'oggetto da clonare (deve implementare Serializable)
     * @param original Oggetto originale da clonare
     * @return Copia profonda dell'oggetto o l'originale in caso di errore
     */
    public static <T extends Serializable> T safeDeepClone(T original) {
        try {
            return deepClone(original);
        } catch (CloneException e) {
            // In un contesto reale, qui si potrebbe loggare l'errore
            return original; // Fallback all'originale in caso di problemi
        }
    }
    
    /**
     * Eccezione specializzata per errori durante il processo di clonazione.
     * 
     * Fornisce informazioni dettagliate sul motivo del fallimento della clonazione,
     * incapsulando l'eccezione originale come causa.
     */
    public static class CloneException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
        /**
         * Crea una nuova eccezione con il messaggio specificato.
         * 
         * @param message Descrizione dell'errore
         */
        public CloneException(String message) {
            super(message);
        }
        
        /**
         * Crea una nuova eccezione con messaggio e causa.
         * 
         * @param message Descrizione dell'errore
         * @param cause Eccezione originale che ha causato il problema
         */
        public CloneException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
