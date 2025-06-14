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
 * Utility class per il deep cloning di oggetti Serializable.
 * 
 * <p>Fornisce metodi per creare copie profonde di oggetti e collezioni
 * utilizzando la serializzazione Java. Gestisce automaticamente gli errori
 * e fornisce fallback appropriati.
 * 
 * @author Strix89
 * @version 1.0
 * @since 1.0
 */
public class CloneUtils {
    
    /**
     * Costruttore privato per prevenire istanziazione di classe utility.
     */
    private CloneUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Crea una copia profonda di un oggetto Serializable.
     * 
     * @param <T> Tipo dell'oggetto da clonare
     * @param original Oggetto originale da clonare
     * @return Copia profonda dell'oggetto, o null se original è null
     * @throws CloneException se la clonazione fallisce
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
     * Crea una copia profonda di una lista di oggetti Serializable.
     * 
     * @param <T> Tipo degli oggetti nella lista
     * @param originalList Lista originale da clonare
     * @return Nuova lista con copie profonde degli elementi
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
     * Crea una copia profonda di un oggetto con fallback sicuro.
     * In caso di errore durante la clonazione, restituisce l'oggetto originale.
     * 
     * @param <T> Tipo dell'oggetto da clonare
     * @param original Oggetto originale da clonare
     * @return Copia profonda dell'oggetto, o l'originale se la clonazione fallisce
     */
    public static <T extends Serializable> T safeDeepClone(T original) {
        try {
            return deepClone(original);
        } catch (CloneException e) {
            // Log dell'errore se necessario
            return original; // Fallback sicuro
        }
    }
    
    /**
     * Eccezione personalizzata per errori di clonazione.
     */
    public static class CloneException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
        public CloneException(String message) {
            super(message);
        }
        
        public CloneException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
