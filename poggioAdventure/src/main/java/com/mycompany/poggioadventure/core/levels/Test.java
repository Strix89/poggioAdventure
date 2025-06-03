package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.model.AdvObject;
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
    private final List<AdvObject> requiredObjects;
    
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
        this(testName, questions, maxWrongAnswers, successMessage, failureMessage, null);
    }

    /**
     * Costruttore per un test con oggetti richiesti.
     * 
     * @param testName Nome del test
     * @param questions Lista delle domande
     * @param maxWrongAnswers Numero massimo di risposte sbagliate consentite
     * @param successMessage Messaggio mostrato in caso di successo
     * @param failureMessage Messaggio mostrato in caso di fallimento
     * @param requiredObjects Lista degli oggetti richiesti (può essere null)
     */
    public Test(String testName, List<Question> questions, int maxWrongAnswers, 
                String successMessage, String failureMessage, List<AdvObject> requiredObjects) {
        this.testName = testName;
        this.questions = questions;
        this.maxWrongAnswers = maxWrongAnswers;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.requiredObjects = requiredObjects;
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
     * Restituisce la lista degli oggetti richiesti per effettuare il test.
     * 
     * @return Lista degli oggetti richiesti (può essere null)
     */
    public List<AdvObject> getRequiredObjects() {
        return requiredObjects;
    }

    /**
     * Restituisce la lista degli ID degli oggetti richiesti per compatibilità.
     * 
     * @return Lista degli ID degli oggetti richiesti (può essere null)
     */
    public List<Integer> getRequiredObjectIds() {
        if (requiredObjects == null) {
            return null;
        }
        return requiredObjects.stream()
            .map(AdvObject::getId)
            .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Verifica se il test ha oggetti richiesti.
     * 
     * @return true se ci sono oggetti richiesti
     */
    public boolean hasRequiredObjects() {
        return java.util.Optional.ofNullable(requiredObjects)
            .map(list -> !list.isEmpty())
            .orElse(false);
    }

    /**
     * Conta il numero di risposte sbagliate usando Stream API.
     * 
     * @param answers Lista degli indici delle risposte date
     * @return Numero di risposte sbagliate
     */
    public int countWrongAnswers(List<Integer> answers) {
        return (int) java.util.stream.IntStream.range(0, Math.min(questions.size(), answers.size()))
            .filter(i -> !questions.get(i).isCorrectAnswer(answers.get(i)))
            .count();
    }

    /**
     * Verifica se il test è stato superato usando lambda.
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