package com.mycompany.poggioadventure.core.levels;

import java.io.Serializable;
import java.util.List;

/**
 * Rappresenta una domanda a scelta multipla per il sistema di test del gioco.
 * 
 * <p>Incapsula il testo della domanda, le opzioni di risposta disponibili
 * e l'indice della risposta corretta. Supporta la serializzazione per
 * salvataggio e caricamento dello stato di gioco.
 * 
 * <p><b>Caratteristiche:</b>
 * <ul>
 *   <li>Immutabile dopo la costruzione</li>
 *   <li>Validazione automatica delle risposte</li>
 *   <li>Supporto per serializzazione</li>
 *   <li>Indici zero-based per compatibilità con array/liste</li>
 * </ul>
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Testo della domanda da presentare al giocatore */
    private final String questionText;
    
    /** Lista delle opzioni di risposta disponibili */
    private final List<String> options;
    
    /** Indice della risposta corretta (zero-based) */
    private final int correctAnswerIndex;
    
    /**
     * Costruisce una nuova domanda con validazione dell'input.
     * 
     * @param questionText Testo della domanda
     * @param options Lista delle opzioni di risposta (non vuota)
     * @param correctAnswerIndex Indice della risposta corretta (deve essere valido)
     * @throws IllegalArgumentException se l'indice è fuori range o le opzioni sono vuote
     */
    public Question(String questionText, List<String> options, int correctAnswerIndex) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Le opzioni non possono essere vuote");
        }
        if (correctAnswerIndex < 0 || correctAnswerIndex >= options.size()) {
            throw new IllegalArgumentException("Indice risposta corretta non valido: " + correctAnswerIndex);
        }
        
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }
    
    /** Restituisce il testo della domanda */
    public String getQuestionText() {
        return questionText;
    }
    
    /** Restituisce la lista immutabile delle opzioni di risposta */
    public List<String> getOptions() {
        return options;
    }
    
    /** Restituisce l'indice della risposta corretta (zero-based) */
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    /**
     * Verifica se l'indice di risposta fornito corrisponde alla risposta corretta.
     * 
     * @param answerIndex Indice della risposta da verificare (zero-based)
     * @return true se la risposta è corretta, false altrimenti
     */
    public boolean isCorrectAnswer(int answerIndex) {
        return answerIndex == correctAnswerIndex;
    }
}