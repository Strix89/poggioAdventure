package com.mycompany.poggioadventure.core.levels;

import java.io.Serializable;
import java.util.List;

/**
 * Rappresenta una domanda con risposte multiple nel sistema di test del gioco.
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String questionText;
    private final List<String> options;
    private final int correctAnswerIndex;
    
    /**
     * Costruttore per una domanda con risposte multiple.
     * 
     * @param questionText Il testo della domanda
     * @param options Lista delle opzioni di risposta
     * @param correctAnswerIndex Indice della risposta corretta (0-based)
     */
    public Question(String questionText, List<String> options, int correctAnswerIndex) {
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public List<String> getOptions() {
        return options;
    }
    
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    /**
     * Verifica se la risposta data è corretta.
     * 
     * @param answerIndex Indice della risposta data
     * @return true se la risposta è corretta
     */
    public boolean isCorrectAnswer(int answerIndex) {
        return answerIndex == correctAnswerIndex;
    }
}