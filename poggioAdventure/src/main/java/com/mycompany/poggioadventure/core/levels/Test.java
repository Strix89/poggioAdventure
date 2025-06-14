package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.model.AdvObject;
import java.io.Serializable;
import java.util.List;

/**
 * Rappresenta un test a scelta multipla con sistema di valutazione basato su soglia di errori.
 * 
 * <p>Gestisce l'intera struttura di un test composto da domande, criteri di superamento
 * e prerequisiti. Supporta validazione delle risposte e messaggi personalizzati per
 * feedback all'utente.
 * 
 * <p><b>Caratteristiche principali:</b>
 * <ul>
 *   <li>Sistema di valutazione tollerante agli errori</li>
 *   <li>Prerequisiti opzionali (oggetti richiesti)</li>
 *   <li>Messaggi personalizzati per successo/fallimento</li>
 *   <li>Validazione automatica delle risposte</li>
 *   <li>Supporto per serializzazione completa</li>
 * </ul>
 */
public class Test implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Nome identificativo del test */
    private final String testName;
    
    /** Collezione di domande che compongono il test */
    private final List<Question> questions;
    
    /** Numero massimo di errori consentiti per superare il test */
    private final int maxWrongAnswers;
    
    /** Messaggio visualizzato in caso di superamento del test */
    private final String successMessage;
    
    /** Messaggio visualizzato in caso di fallimento del test */
    private final String failureMessage;
    
    /** Oggetti necessari per sostenere il test (opzionale) */
    private final List<AdvObject> requiredObjects;
    
    /**
     * Costruttore semplificato per test senza prerequisiti.
     * Delega al costruttore completo con oggetti richiesti null.
     */
    public Test(String testName, List<Question> questions, int maxWrongAnswers, 
                String successMessage, String failureMessage) {
        this(testName, questions, maxWrongAnswers, successMessage, failureMessage, null);
    }

    /**
     * Costruttore completo con validazione input e configurazione prerequisiti.
     * 
     * @param testName Nome identificativo del test
     * @param questions Lista delle domande (non può essere vuota)
     * @param maxWrongAnswers Numero massimo di errori consentiti (non negativo)
     * @param successMessage Messaggio per superamento test
     * @param failureMessage Messaggio per fallimento test
     * @param requiredObjects Oggetti necessari per sostenere il test (opzionale)
     * @throws IllegalArgumentException se i parametri non sono validi
     */
    public Test(String testName, List<Question> questions, int maxWrongAnswers, 
                String successMessage, String failureMessage, List<AdvObject> requiredObjects) {
        if (questions == null || questions.isEmpty()) {
            throw new IllegalArgumentException("Il test deve contenere almeno una domanda");
        }
        if (maxWrongAnswers < 0) {
            throw new IllegalArgumentException("Il numero massimo di errori non può essere negativo");
        }
        
        this.testName = testName;
        this.questions = questions;
        this.maxWrongAnswers = maxWrongAnswers;
        this.successMessage = successMessage;
        this.failureMessage = failureMessage;
        this.requiredObjects = requiredObjects;
    }
    
    /** Restituisce il nome identificativo del test */
    public String getTestName() {
        return testName;
    }
    
    /** Restituisce la lista immutabile delle domande del test */
    public List<Question> getQuestions() {
        return questions;
    }

    /** Restituisce il numero massimo di errori consentiti */
    public int getMaxWrongAnswers() {
        return maxWrongAnswers;
    }
    
    /** Restituisce il messaggio di feedback per superamento test */
    public String getSuccessMessage() {
        return successMessage;
    }
    
    /** Restituisce il messaggio di feedback per fallimento test */
    public String getFailureMessage() {
        return failureMessage;
    }

    /** Restituisce la lista degli oggetti richiesti per sostenere il test */
    public List<AdvObject> getRequiredObjects() {
        return requiredObjects;
    }

    /**
     * Estrae gli ID degli oggetti richiesti per compatibilità con sistemi legacy.
     * Utilizza Stream API per mappatura efficiente.
     * 
     * @return Lista di ID o null se non ci sono oggetti richiesti
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
     * Verifica presenza di prerequisiti per il test.
     * Utilizza Optional per gestione null-safe.
     * 
     * @return true se esistono oggetti richiesti non vuoti
     */
    public boolean hasRequiredObjects() {
        return java.util.Optional.ofNullable(requiredObjects)
            .map(list -> !list.isEmpty())
            .orElse(false);
    }

    /**
     * Calcola il numero di risposte errate confrontando con le soluzioni corrette.
     * Utilizza Stream API per elaborazione funzionale e gestisce liste di dimensioni diverse.
     * 
     * @param answers Lista degli indici delle risposte fornite
     * @return Numero totale di risposte sbagliate
     */
    public int countWrongAnswers(List<Integer> answers) {
        return (int) java.util.stream.IntStream.range(0, Math.min(questions.size(), answers.size()))
            .filter(i -> !questions.get(i).isCorrectAnswer(answers.get(i)))
            .count();
    }

    /**
     * Determina se il test è stato superato basandosi sulla soglia di errori.
     * 
     * @param answers Lista completa delle risposte fornite
     * @return true se gli errori sono entro la soglia consentita
     */
    public boolean isPassed(List<Integer> answers) {
        return countWrongAnswers(answers) <= maxWrongAnswers;
    }
    
    /**
     * Verifica fallimento immediato durante l'esecuzione del test.
     * Utile per interrompere il test non appena si supera la soglia di errori.
     * 
     * @param wrongAnswersCount Numero corrente di risposte sbagliate
     * @return true se il test deve essere considerato fallito
     */
    public boolean isFailed(int wrongAnswersCount) {
        return wrongAnswersCount > maxWrongAnswers;
    }
}