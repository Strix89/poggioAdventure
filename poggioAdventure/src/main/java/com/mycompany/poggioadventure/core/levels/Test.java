package com.mycompany.poggioadventure.core.levels;

import java.io.Serializable;
import java.util.List;

/**
 * Rappresenta un test composto da più domande con limite di errori.
 */
public class Test implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String testName;
    private final List<Question> questions;
    private final int maxWrongAnswers;
    private final String successMessage;
    private final String failureMessage;
    
    /**
     * Costruttore per un test.
     * 
     * @param testName Nome del test
     * @param questions Lista delle domande
     * @param maxWrongAnswers Numero massimo di risposte sbagliate consentite
     * @param successMessage Messaggio mostrato in caso di successo
     * @param failureMessage Messaggio mostrato in caso di fallimento
     */
    public Test(String testName, List<Question> questions, int maxWrongAnswers, 
                String successMessage, String failureMessage) {
        this.testName = testName;
        this.questions = questions;
        this.maxWrongAnswers = maxWrongAnswers;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
    }
    
    public String getTestName() {
        return testName;
    }
    
    public List<Question> getQuestions() {
        return questions;
    }
    
    public int getMaxWrongAnswers() {
        return maxWrongAnswers;
    }
    
    public String getSuccessMessage() {
        return successMessage;
    }
    
    public String getFailureMessage() {
        return failureMessage;
    }
    
    /**
     * Conta il numero di risposte sbagliate.
     * 
     * @param answers Lista degli indici delle risposte date
     * @return Numero di risposte sbagliate
     */
    public int countWrongAnswers(List<Integer> answers) {
        int wrongCount = 0;
        for (int i = 0; i < Math.min(questions.size(), answers.size()); i++) {
            if (!questions.get(i).isCorrectAnswer(answers.get(i))) {
                wrongCount++;
            }
        }
        return wrongCount;
    }
    
    /**
     * Verifica se il test è stato superato (errori <= limite).
     * 
     * @param answers Lista degli indici delle risposte date
     * @return true se il test è stato superato
     */
    public boolean isPassed(List<Integer> answers) {
        return countWrongAnswers(answers) <= maxWrongAnswers;
    }
    
    /**
     * Verifica se il test è fallito (troppi errori).
     * 
     * @param wrongAnswersCount Numero di risposte sbagliate attuali
     * @return true se il test è fallito
     */
    public boolean isFailed(int wrongAnswersCount) {
        return wrongAnswersCount > maxWrongAnswers;
    }
}