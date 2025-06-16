package com.mycompany.poggioadventure.ui.gui.views;

import com.mycompany.poggioadventure.ui.UI_Abstract;
import com.mycompany.poggioadventure.core.abstracts.IFlipperCommandProcessor;
import com.mycompany.poggioadventure.core.levels.FlipperCommandProcessor;
import com.mycompany.poggioadventure.core.utils.FlipperResult;
import com.mycompany.poggioadventure.core.utils.GameContext;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * Classe che rappresenta una finestra per l'interfaccia utente del "Flipper Zero".
 * Mostra un'immagine ASCII scalata e include un campo di input con un pulsante
 * per inviare comandi. Estende la classe astratta UI_Abstract per ereditare
 * la struttura di base dell'interfaccia grafica.
 */
public class UI_Flipper extends UI_Abstract {

    // Componenti dell'interfaccia utente
    private JTextField inputField;  // Campo di testo per l'inserimento dei comandi
    private JButton sendButton;     // Pulsante per inviare i comandi
    private JLabel imageLabel;     // Etichetta per visualizzare l'immagine ASCII
    
    // Logic e contesto
    private final GameContext gameContext;
    private final IFlipperCommandProcessor commandProcessor;
    private Consumer<String> onCommandCallback;

    /**
     * Costruttore della classe con GameContext e CommandProcessor.
     */
    public UI_Flipper(GameContext gameContext, IFlipperCommandProcessor commandProcessor) {
        super();
        this.gameContext = gameContext;
        this.commandProcessor = commandProcessor != null ? commandProcessor : new FlipperCommandProcessor(gameContext);
    }

    /**
     * Imposta il callback per gestire i risultati dei comandi.
     */
    public void setCommandCallback(Consumer<String> callback) {
        this.onCommandCallback = callback;
    }

    /**
     * Implementazione del metodo astratto initComponents() della superclasse UI_Abstract.
     * Inizializza tutti i componenti dell'interfaccia utente, inclusi l'immagine ASCII,
     * il campo di input e il pulsante di invio.
     */
    @Override
    protected void initComponents() {
        setLayout(new BorderLayout(10, 10));  // Imposta il layout principale della finestra

        // 1. PANNELLO CENTRALE - IMMAGINE ASCII
        BufferedImage originalImage = UI_Config.getAsciiImage();  // Ottiene l'immagine ASCII dalla configurazione

        // Scala l'immagine mantenendo le proporzioni (larghezza massima 600px)
        int maxWidth = 600;  // Larghezza massima dell'immagine
        int scaledWidth = Math.min(originalImage.getWidth(), maxWidth);  // Calcola la larghezza scalata
        int scaledHeight = (int) ((double) originalImage.getHeight() / originalImage.getWidth() * scaledWidth);  // Calcola l'altezza proporzionale

        // Ridimensiona l'immagine in modo fluido
        Image scaledImage = originalImage.getScaledInstance(
            scaledWidth, 
            scaledHeight, 
            Image.SCALE_SMOOTH  // Algoritmo di ridimensionamento ad alta qualità
        );

        // Crea un'etichetta per visualizzare l'immagine scalata
        imageLabel = new JLabel(new ImageIcon(scaledImage));
        imageLabel.setHorizontalAlignment(JLabel.CENTER);  // Allinea l'immagine al centro

        // Aggiunge l'immagine a uno JScrollPane per supportare lo scrolling se necessario
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setBorder(null);  // Rimuove il bordo predefinito
        scrollPane.getViewport().setBackground(UI_Config.BACKGROUND_COLOR);  // Imposta il colore di sfondo
        add(scrollPane, BorderLayout.CENTER);  // Aggiunge il pannello al centro della finestra

        // 2. PANNELLO INFERIORE - INPUT E PULSANTE
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));  // Crea un pannello per input e pulsante
        bottomPanel.setBackground(UI_Config.BACKGROUND_COLOR);  // Imposta il colore di sfondo
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));  // Aggiunge un padding interno

        // Configurazione del campo di input
        inputField = new JTextField();
        inputField.setFont(UI_Config.getNormalFont().deriveFont(14f));  // Imposta il font
        inputField.setForeground(Color.ORANGE);  // Colore del testo
        inputField.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Colore di sfondo
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 1),  // Bordo esterno
            BorderFactory.createEmptyBorder(5, 5, 5, 5)  // Padding interno
        ));
        inputField.setToolTipText("Inserisci: [frequenza] [comando] (es: 433.92 GoToRecharge)");
        
        // Abilita invio con Enter
        inputField.addActionListener(e -> processCommand());
        
        bottomPanel.add(inputField, BorderLayout.CENTER);  // Aggiunge il campo di input al pannello

        // Configurazione del pulsante di invio
        sendButton = new JButton("Invia");
        sendButton.setFont(UI_Config.getBoldFont().deriveFont(14f));  // Imposta il font in grassetto
        sendButton.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        sendButton.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Colore di sfondo
        sendButton.setFocusPainted(false);  // Disabilita l'effetto di focus
        sendButton.addActionListener(e -> processCommand());  // Aggiunge l'azione al pulsante
        bottomPanel.add(sendButton, BorderLayout.EAST);  // Aggiunge il pulsante al pannello

        add(bottomPanel, BorderLayout.SOUTH);  // Aggiunge il pannello inferiore alla finestra

        // Imposta le dimensioni della finestra in base all'immagine scalata
        setSize(new Dimension(
            scaledWidth + 40,  // Larghezza immagine + padding laterale
            scaledHeight + 100 // Altezza immagine + spazio per il pannello inferiore
        ));
    }

    /**
     * Processa il comando inserito dall'utente usando il CommandProcessor.
     * Mostra il risultato tramite JOptionPane.
     */
    private void processCommand() {
        String input = inputField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Inserisci un comando!\n\nFormato: [frequenza] [comando]\nEsempio: 433.92 GoToRecharge",
                "Input Vuoto",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        gameContext.getTemplog().add("[FLIPPER]: " + input);

        // Processa il comando tramite CommandProcessor
        FlipperResult result = commandProcessor.processCommand(input);
        
        // Mostra risultato tramite dialog
        showResultDialog(result);
        
        // Verifica se il comando richiede la chiusura dell'interfaccia
        boolean shouldClose = result.isGameCompleted() || 
                            (result.getType() == FlipperResult.ResultType.ERROR && 
                            result.getMessage().contains("GAME OVER"));
        
        if (shouldClose) {
            // Notifica il callback se presente
            if (onCommandCallback != null) {
                if (result.isGameCompleted()) {
                    onCommandCallback.accept("GAME_COMPLETED");
                } else {
                    onCommandCallback.accept("GAME_OVER");
                }
            }
            
            // Chiudi la finestra dopo un breve delay
            javax.swing.Timer timer = new javax.swing.Timer(2000, e -> {
                this.setVisible(false);
                this.dispose();
            });
            timer.setRepeats(false);
            timer.start();
        }
        
        inputField.setText("");
    }

    /**
     * Mostra il risultato del comando tramite JOptionPane con icona appropriata.
     */
    private void showResultDialog(FlipperResult result) {
        String title;
        int messageType;
        
        switch (result.getType()) {
            case SUCCESS:
                title = "✅ Comando Eseguito con Successo";
                messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
            case ERROR:
                title = "❌ Errore nell'Esecuzione";
                messageType = JOptionPane.ERROR_MESSAGE;
                break;
            case WARNING:
                title = "⚠️ Comando Eseguito con Avvisi";
                messageType = JOptionPane.WARNING_MESSAGE;
                break;
            case INFO:
                title = "ℹ️ Informazione";
                messageType = JOptionPane.INFORMATION_MESSAGE;
                break;
            default:
                title = "Risultato Comando";
                messageType = JOptionPane.PLAIN_MESSAGE;
        }
        
        // Aggiungi informazioni su modifiche al tempo se presenti
        String message = result.getMessage();
        
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    

    /**
     * Implementazione del metodo astratto getWindowTitle() della superclasse UI_Abstract.
     * Restituisce il titolo della finestra.
     *
     * @return Stringa contenente il titolo della finestra
     */
    @Override
    protected String getWindowTitle() {
        return "Flipper Zero - Robot Control Interface";
    }

    /**
     * Metodo main per testare UI_Flipper in modalità standalone.
     */
    public static void main(String[] args) {

        // Crea un GameContext mock per il test
        GameContext mockGameContext = createMockGameContext();
        
        // Crea il CommandProcessor
        IFlipperCommandProcessor commandProcessor = new FlipperCommandProcessor(mockGameContext);
        
        // Crea l'interfaccia UI_Flipper
        SwingUtilities.invokeLater(() -> {
            try {
                UI_Flipper flipperUI = new UI_Flipper(mockGameContext, commandProcessor);
                
                // Imposta callback per vedere i risultati
                flipperUI.setCommandCallback(result -> {
                    System.out.println("CALLBACK RICEVUTO: " + result);
                    
                    // Simula reazioni del gioco
                    if (result.contains("SUCCESSO")) {
                        System.out.println("🎉 GIOCO COMPLETATO!");
                    } else if (result.contains("ATTENZIONE")) {
                        System.out.println("⚠️ PENALITÀ APPLICATA!");
                    } else if (result.contains("INFO")) {
                        System.out.println("ℹ️ BONUS RICEVUTO!");
                    }
                });
                
                // Configura la finestra
                flipperUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                flipperUI.setLocationRelativeTo(null); // Centra la finestra
                flipperUI.setVisible(true);
                
                System.out.println("UI_Flipper avviata in modalità test!");
                System.out.println("Prova questi comandi:");
                System.out.println("✅ 433.92 GoToRecharge");
                System.out.println("⚠️ 868.0 Override");
                System.out.println("ℹ️ 915.0 Stop");
                System.out.println("❌ 999.0 GoToRecharge (errore)");
                
            } catch (Exception e) {
                System.err.println("Errore durante l'avvio di UI_Flipper: " + e.getMessage());
                e.printStackTrace();
                
                JOptionPane.showMessageDialog(null, 
                    "Errore durante l'avvio di UI_Flipper:\n" + e.getMessage(),
                    "Errore di Test",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    /**
     * Crea un GameContext mock per i test.
     */
    private static GameContext createMockGameContext() {
        return new GameContext(null, null, null, null, null, null, null) {
            @Override
            public String toString() {
                return "MockGameContext per test UI_Flipper";
            }
        };
    }
}