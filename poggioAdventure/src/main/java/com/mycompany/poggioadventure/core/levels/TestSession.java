package com.mycompany.poggioadventure.core.levels;

import com.mycompany.poggioadventure.ui.InputHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;
import com.mycompany.poggioadventure.ui.gui.GUIInputHandler;
import com.mycompany.poggioadventure.ui.ColorText;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Gestisce l'esecuzione di una sessione di test interattiva con stato persistente.
 * 
 * <p>Mantiene il progresso del test, raccoglie le risposte, calcola gli errori
 * e determina l'esito finale. Supporta sia interfaccia CLI che GUI con
 * formattazione appropriata per ciascuna modalità.
 * 
 * <p><b>Funzionalità principali:</b>
 * <ul>
 *   <li>Gestione stato corrente del test (domanda attiva, errori, completamento)</li>
 *   <li>Validazione input utente con feedback immediato</li>
 *   <li>Terminazione anticipata per troppi errori</li>
 *   <li>Supporto per abbandono volontario del test</li>
 *   <li>Interfaccia unificata per CLI e GUI</li>
 *   <li>Serializzazione completa per salvataggio stato</li>
 * </ul>
 * 
 * <p><b>Pattern utilizzati:</b>
 * <ul>
 *   <li>State Machine: gestisce transizioni tra stati del test</li>
 *   <li>Strategy: adatta comportamento per diversi tipi di InputHandler</li>
 *   <li>Template Method: struttura comune per esecuzione test</li>
 * </ul>
 */
public class TestSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Riferimento al test da eseguire con domande e criteri */
    private final Test test;
    
    /** Indice della domanda corrente (zero-based) */
    private int currentQuestionIndex;
    
    /** Lista delle risposte fornite dall'utente */
    private final List<Integer> answers;
    
    /** Contatore degli errori commessi durante il test */
    private int wrongAnswersCount;
    
    /** Flag che indica se il test è ancora in corso */
    private boolean isActive;
    
    /** Flag che indica se il test è stato completato con successo */
    private boolean isCompleted;
    
    /** Flag che indica se il test è fallito per troppi errori */
    private boolean isFailed;
    
    /**
     * Inizializza una nuova sessione di test con stato pulito.
     * 
     * @param test Test da eseguire con domande e configurazione
     */
    public TestSession(Test test) {
        this.test = test;
        this.currentQuestionIndex = 0;
        this.answers = new ArrayList<>();
        this.wrongAnswersCount = 0;
        this.isActive = true;
        this.isCompleted = false;
        this.isFailed = false;
    }
    
    /** Restituisce il test associato a questa sessione */
    public Test getTest() {
        return test;
    }
    
    /** Restituisce l'indice della domanda corrente (zero-based) */
    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }
    
    /**
     * Restituisce la domanda corrente o null se il test è terminato.
     * 
     * @return Domanda corrente o null se non ci sono più domande
     */
    public Question getCurrentQuestion() {
        if (currentQuestionIndex < test.getQuestions().size()) {
            return test.getQuestions().get(currentQuestionIndex);
        }
        return null;
    }
    
    /** Verifica se ci sono ancora domande da porre */
    public boolean hasNextQuestion() {
        return currentQuestionIndex < test.getQuestions().size();
    }
    
    /** Restituisce il numero totale di errori commessi */
    public int getWrongAnswersCount() {
        return wrongAnswersCount;
    }
    
    /** Calcola il numero di errori rimanenti prima del fallimento */
    public int getRemainingErrors() {
        return test.getMaxWrongAnswers() - wrongAnswersCount;
    }
    
    /**
     * Registra una risposta e aggiorna lo stato del test.
     * Gestisce la logica di avanzamento, fallimento e completamento.
     * 
     * @param answerIndex Indice della risposta fornita (zero-based)
     * @return true se la risposta era corretta, false altrimenti
     */
    public boolean addAnswer(int answerIndex) {
        answers.add(answerIndex);
        Question currentQuestion = test.getQuestions().get(currentQuestionIndex);
        boolean isCorrect = currentQuestion.isCorrectAnswer(answerIndex);
        
        if (!isCorrect) {
            wrongAnswersCount++;
            
            // Verifica fallimento immediato per troppi errori
            if (test.isFailed(wrongAnswersCount)) {
                isFailed = true;
                isActive = false;
                return false;
            }
        }
        
        currentQuestionIndex++;
        
        // Verifica completamento del test
        if (!hasNextQuestion()) {
            isCompleted = true;
            isActive = false;
        }
        
        return isCorrect;
    }
    
    /** Restituisce copia difensiva delle risposte fornite */
    public List<Integer> getAnswers() {
        return new ArrayList<>(answers);
    }
    
    /** Indica se il test è ancora in corso */
    public boolean isActive() {
        return isActive;
    }
    
    /** Indica se il test è stato completato con tutte le domande */
    public boolean isCompleted() {
        return isCompleted;
    }
    
    /** Indica se il test è fallito per troppi errori */
    public boolean isFailed() {
        return isFailed;
    }
    
    /** Termina forzatamente il test (per abbandono utente) */
    public void deactivate() {
        isActive = false;
    }
    
    /**
     * Genera testo formattato per la domanda corrente con progressione e opzioni.
     * Include contatori di progresso e istruzioni per l'utente.
     */
    public String getCurrentQuestionText() {
        if (!hasNextQuestion()) {
            return "Test completato!";
        }
        
        Question question = getCurrentQuestion();
        StringBuilder sb = new StringBuilder();
        
        // Header con progresso
        sb.append("Domanda ").append(currentQuestionIndex + 1)
          .append(" di ").append(test.getQuestions().size()).append("\n");
        sb.append("Errori: ").append(wrongAnswersCount)
          .append("/").append(test.getMaxWrongAnswers()).append("\n\n");
        
        // Testo della domanda
        sb.append(question.getQuestionText()).append("\n\n");
        
        // Opzioni numerate (1-based per utente)
        for (int i = 0; i < question.getOptions().size(); i++) {
            sb.append(i + 1).append(" - ").append(question.getOptions().get(i)).append("\n");
        }
        
        // Prompt per input
        sb.append("\nDigita il numero della tua risposta (1-")
          .append(question.getOptions().size()).append("): ");
        
        return sb.toString();
    }
    
    /**
     * Esegue l'intero test in modalità bloccante con gestione input/output.
     * Supporta sia CLI che GUI con interfacce appropriate per ciascuna modalità.
     * 
     * @param outputHandler Gestore output per messaggi e domande
     * @param inputHandler Gestore input per raccolta risposte
     * @param npcName Nome dell'NPC che conduce il test per messaggi personalizzati
     * @return true se il test è stato superato, false per fallimento o abbandono
     */
    public boolean executeTest(OutputHandler outputHandler, InputHandler inputHandler, String npcName) {
        // Header introduttivo del test
        outputHandler.writeln("=".repeat(50), ColorText.LIGHT_ORANGE);
        outputHandler.writeln("\t" + test.getTestName(), ColorText.LIGHT_ORANGE);
        outputHandler.writeln("=".repeat(50), ColorText.LIGHT_ORANGE);
        outputHandler.writeln("Puoi commettere massimo " + test.getMaxWrongAnswers() + " errori.", ColorText.WARNING);
        outputHandler.writeln("Digita '[RED]q[/]', '[RED]quit[/]' o '[RED]esci[/]' per abbandonare il test.\n", ColorText.WHITE);
        
        // Loop principale delle domande
        while (hasNextQuestion() && isActive) {
            Question currentQuestion = getCurrentQuestion();
            String userInput;

            // Gestione input differenziata per GUI vs CLI
            if (inputHandler instanceof GUIInputHandler) {
                // Modalità GUI: usa JOptionPane per input
                StringBuilder questionPromptBuilder = new StringBuilder();
                questionPromptBuilder.append("Domanda ").append(currentQuestionIndex + 1)
                    .append(" di ").append(test.getQuestions().size()).append("\n");
                questionPromptBuilder.append("Errori: ").append(wrongAnswersCount)
                    .append("/").append(test.getMaxWrongAnswers()).append("\n\n");
                questionPromptBuilder.append(currentQuestion.getQuestionText()).append("\n\n");
                for (int i = 0; i < currentQuestion.getOptions().size(); i++) {
                    questionPromptBuilder.append(i + 1).append(" - ").append(currentQuestion.getOptions().get(i)).append("\n");
                }
                questionPromptBuilder.append("\nDigita il numero della tua risposta (1-")
                    .append(currentQuestion.getOptions().size()).append("):");
                
                userInput = JOptionPane.showInputDialog(null, questionPromptBuilder.toString(), test.getTestName(), JOptionPane.QUESTION_MESSAGE);
                if (userInput == null) {
                    userInput = "quit"; // Gestione cancellazione dialog
                } else {
                    userInput = userInput.trim();
                }
            } else {
                // Modalità CLI: usa console standard
                outputHandler.writeln(getCurrentQuestionText(), ColorText.SUNFLOWER);
                userInput = inputHandler.getInput().trim();
            }
            
            // Gestione comandi di abbandono
            if ("quit".equalsIgnoreCase(userInput) || "esci".equalsIgnoreCase(userInput) || "q".equalsIgnoreCase(userInput)) {
                outputHandler.writeln("Hai abbandonato il test.", ColorText.ERROR);
                deactivate();
                return false;
            }
            
            // Validazione e processamento risposta numerica
            try {
                int answerNumber = Integer.parseInt(userInput);
                
                // Validazione range risposta
                if (answerNumber < 1 || answerNumber > currentQuestion.getOptions().size()) {
                    outputHandler.writeln("Risposta non valida! Inserisci un numero da 1 a " + 
                                        currentQuestion.getOptions().size() + ".", ColorText.ERROR);
                    continue;
                }
                
                // Conversione a zero-based e registrazione risposta
                int answerIndex = answerNumber - 1;
                boolean isCorrect = addAnswer(answerIndex);
                
                // Feedback immediato sulla risposta
                if (isCorrect) {
                    outputHandler.writeln("Risposta corretta!", ColorText.SUCCESS);
                } else {
                    outputHandler.writeln("Risposta sbagliata.", ColorText.ERROR);
                    outputHandler.writeln("Errori: " + wrongAnswersCount + "/" + test.getMaxWrongAnswers(), 
                                        ColorText.WARNING);
                }
                
                outputHandler.writeln("");
                
                // Verifica fallimento per troppi errori
                if (isFailed()) {
                    outputHandler.writeln("🚫 TROPPI ERRORI! Il test è terminato.", ColorText.ERROR);
                    break;
                }
                
            } catch (NumberFormatException e) {
                outputHandler.writeln("Input non valido!", ColorText.ERROR);
            }
        }
        
        showFinalResult(outputHandler, npcName);
        return isCompleted && !isFailed;
    }

    /**
     * Visualizza il risultato finale del test con statistiche e messaggi personalizzati.
     * Include conteggio errori, esito e messaggio appropriato dall'NPC.
     */
    private void showFinalResult(OutputHandler outputHandler, String npcName) {
        outputHandler.writeln("\n" + "=".repeat(60), ColorText.LIGHT_ORANGE);
        outputHandler.writeln("RISULTATO FINALE", ColorText.LIGHT_ORANGE);
        outputHandler.writeln("=".repeat(60), ColorText.LIGHT_ORANGE);
        outputHandler.writeln("[RED]Errori[/] commessi: [RED]" + wrongAnswersCount + "[/]/" + test.getMaxWrongAnswers(), 
                            ColorText.WHITE);
        
        // Messaggio esito specifico
        if (isFailed) {
            outputHandler.writeln("[ERROR]❌[/] FALLITO! [NPC]" + npcName + "[/]: " + test.getFailureMessage(), ColorText.WHITE);
        } else if (isCompleted) {
            outputHandler.writeln("[SUCCESS]✅[/] SUPERATO! [NPC]" + npcName + "[/]: " + test.getSuccessMessage(), ColorText.WHITE);
        } else {
            outputHandler.writeln("ℹ️ TEST NON COMPLETATO.", ColorText.WARNING);
        }
        outputHandler.writeln("=".repeat(60) + "\n", ColorText.LIGHT_ORANGE);
    }
}
