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
 * Gestisce una sessione di test in corso, incluso lo stato corrente e il conteggio errori.
 */
public class TestSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Test test;
    private int currentQuestionIndex;
    private final List<Integer> answers;
    private int wrongAnswersCount;
    private boolean isActive;
    private boolean isCompleted;
    private boolean isFailed;
    
    public TestSession(Test test) {
        this.test = test;
        this.currentQuestionIndex = 0;
        this.answers = new ArrayList<>();
        this.wrongAnswersCount = 0;
        this.isActive = true;
        this.isCompleted = false;
        this.isFailed = false;
    }
    
    public Test getTest() {
        return test;
    }
    
    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }
    
    public Question getCurrentQuestion() {
        if (currentQuestionIndex < test.getQuestions().size()) {
            return test.getQuestions().get(currentQuestionIndex);
        }
        return null;
    }
    
    public boolean hasNextQuestion() {
        return currentQuestionIndex < test.getQuestions().size();
    }
    
    public int getWrongAnswersCount() {
        return wrongAnswersCount;
    }
    
    public int getRemainingErrors() {
        return test.getMaxWrongAnswers() - wrongAnswersCount;
    }
    
    /**
     * Aggiunge una risposta e controlla se il test continua o termina.
     * 
     * @param answerIndex Indice della risposta data (0-based)
     * @return true se la risposta era corretta, false altrimenti
     */
    public boolean addAnswer(int answerIndex) {
        answers.add(answerIndex);
        Question currentQuestion = test.getQuestions().get(currentQuestionIndex);
        boolean isCorrect = currentQuestion.isCorrectAnswer(answerIndex);
        
        if (!isCorrect) {
            wrongAnswersCount++;
            
            if (test.isFailed(wrongAnswersCount)) {
                isFailed = true;
                isActive = false;
                return false;
            }
        }
        
        currentQuestionIndex++;
        
        if (!hasNextQuestion()) {
            isCompleted = true;
            isActive = false;
        }
        
        return isCorrect;
    }
    
    public List<Integer> getAnswers() {
        return new ArrayList<>(answers);
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public boolean isFailed() {
        return isFailed;
    }
    
    public void deactivate() {
        isActive = false;
    }
    
    /**
     * Genera il testo formattato per la domanda corrente con opzioni numerate.
     */
    public String getCurrentQuestionText() {
        if (!hasNextQuestion()) {
            return "Test completato!";
        }
        
        Question question = getCurrentQuestion();
        StringBuilder sb = new StringBuilder();
        
        sb.append("Domanda ").append(currentQuestionIndex + 1)
          .append(" di ").append(test.getQuestions().size()).append("\n");
        sb.append("Errori: ").append(wrongAnswersCount)
          .append("/").append(test.getMaxWrongAnswers()).append("\n\n");
        
        sb.append(question.getQuestionText()).append("\n\n");
        
        // Aggiungi le opzioni numerate
        for (int i = 0; i < question.getOptions().size(); i++) {
            sb.append(i + 1).append(" - ").append(question.getOptions().get(i)).append("\n");
        }
        
        sb.append("\nDigita il numero della tua risposta (1-")
          .append(question.getOptions().size()).append("): ");
        
        return sb.toString();
    }
    
    /**
     * Esegue il test completo bloccando il gioco fino al completamento.
     * 
     * @param outputHandler Gestore dell'output per mostrare domande e risultati
     * @param inputHandler Gestore dell'input per ricevere le risposte
     * @return true se il test è stato superato, false altrimenti
     */
    public boolean executeTest(OutputHandler outputHandler, InputHandler inputHandler, String npcName) {
        outputHandler.writeln("=".repeat(50), ColorText.LIGHT_ORANGE);
        outputHandler.writeln("\t" + test.getTestName(), ColorText.LIGHT_ORANGE);
        outputHandler.writeln("=".repeat(50), ColorText.LIGHT_ORANGE);
        outputHandler.writeln("[NPC]" + npcName + "[/]: Cominciamo!", ColorText.WHITE);
        outputHandler.writeln("Puoi commettere massimo " + test.getMaxWrongAnswers() + " errori.", ColorText.WARNING);
        outputHandler.writeln("Digita '[RED]q[/]', '[RED]quit[/]' o '[RED]esci[/]' per abbandonare il test.\n", ColorText.WHITE);
        
        while (hasNextQuestion() && isActive) {
            Question currentQuestion = getCurrentQuestion();
            String userInput;

            if (inputHandler instanceof GUIInputHandler) {
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
                    userInput = "quit";
                } else {
                    userInput = userInput.trim();
                }
            } else {
                outputHandler.writeln(getCurrentQuestionText(), ColorText.SUNFLOWER);
                userInput = inputHandler.getInput().trim();
            }
            
            if ("quit".equalsIgnoreCase(userInput) || "esci".equalsIgnoreCase(userInput) || "q".equalsIgnoreCase(userInput)) {
                outputHandler.writeln("Hai abbandonato il test.", ColorText.ERROR);
                deactivate();
                return false;
            }
            
            try {
                int answerNumber = Integer.parseInt(userInput);
                
                if (answerNumber < 1 || answerNumber > currentQuestion.getOptions().size()) {
                    outputHandler.writeln("Risposta non valida! Inserisci un numero da 1 a " + 
                                        currentQuestion.getOptions().size() + ".", ColorText.ERROR);
                    continue;
                }
                
                int answerIndex = answerNumber - 1;
                boolean isCorrect = addAnswer(answerIndex);
                
                if (isCorrect) {
                    outputHandler.writeln("Risposta corretta!", ColorText.SUCCESS);
                } else {
                    outputHandler.writeln("Risposta sbagliata.", ColorText.ERROR);
                    outputHandler.writeln("Errori: " + wrongAnswersCount + "/" + test.getMaxWrongAnswers(), 
                                        ColorText.WARNING);
                }
                
                outputHandler.writeln("");
                
                if (isFailed()) {
                    outputHandler.writeln("🚫 TROPPI ERRORI! Il test è terminato.", ColorText.ERROR);
                    break;
                }
                
            } catch (NumberFormatException e) {
                outputHandler.writeln("Input non valido!", 
                                    ColorText.ERROR);
            }
        }
        
        showFinalResult(outputHandler, npcName);
        return isCompleted && !isFailed;
    }

    /**
     * Mostra il risultato finale del test.
     */
    private void showFinalResult(OutputHandler outputHandler, String npcName) {
        outputHandler.writeln("\n" + "=".repeat(60), ColorText.LIGHT_ORANGE);
        outputHandler.writeln("RISULTATO FINALE", ColorText.LIGHT_ORANGE);
        outputHandler.writeln("=".repeat(60), ColorText.LIGHT_ORANGE);
        outputHandler.writeln("[RED]Errori[/] commessi: [RED]" + wrongAnswersCount + "[/]/" + test.getMaxWrongAnswers(), 
                            ColorText.WHITE);
        
        if (isFailed) {
            outputHandler.writeln("[ERROR]❌[/] FALLITO! [NPC]" + npcName + "[/]: " + test.getFailureMessage(), ColorText.WHITE);
        } else if (isCompleted) {
            outputHandler.writeln("[SUCCESS]✅[/] [NPC]" + npcName + "[/]: " + test.getSuccessMessage(), ColorText.WHITE);
        } else {
            outputHandler.writeln("ℹ️ TEST NON COMPLETATO.", ColorText.WARNING);
        }
        outputHandler.writeln("=".repeat(60) + "\n", ColorText.LIGHT_ORANGE);
    }
}
