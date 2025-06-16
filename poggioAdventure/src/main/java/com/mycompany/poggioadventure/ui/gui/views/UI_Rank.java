package com.mycompany.poggioadventure.ui.gui.views;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.poggioadventure.core.utils.ApiClientResult;
import com.mycompany.poggioadventure.core.utils.PoggioClientJersey;
import com.mycompany.poggioadventure.ui.UI_Abstract;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.persistence.RankingEntryDTO;
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.ui.gui.GUIErrorHandler;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;

/**
 * Schermata per la visualizzazione della classifica dei giocatori.
 * 
 * Questa interfaccia si occupa di recuperare e presentare i dati della classifica
 * dal server backend, mostrando i punteggi in una lista scrollabile.
 * Implementa anche la funzionalità di download dei log di gioco degli utenti
 * presenti in classifica tramite selezione e pressione della barra spaziatrice.
 * 
 * La UI è progettata per essere responsiva e non bloccare il thread EDT durante
 * le operazioni di rete, utilizzando thread separati per i caricamenti.
 */
public class UI_Rank extends UI_Abstract {

    /** Lista scrollabile per visualizzare le voci della classifica */
    private JList<String> rankingList;
    
    /** Etichetta per il titolo della schermata */
    private JLabel titleLabel;
    
    /** Modello dati per la lista di classifica */
    private DefaultListModel<String> rankingModel;

    /**
     * Costruisce una nuova finestra della classifica.
     */
    public UI_Rank() {
        super();
    }

    /**
     * Inizializza tutti i componenti dell'interfaccia e avvia
     * il caricamento dei dati della classifica.
     */
    @Override
    protected void initComponents() {
        configureFrame();
        createComponents();
        setupLayout();
        loadRankingData();
        pack();
    }

    /**
     * Configura le proprietà di base della finestra.
     */
    private void configureFrame() {
        setPreferredSize(calculateWindowSize());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        getContentPane().setLayout(new BorderLayout());
    }

    /**
     * Calcola le dimensioni appropriate per questa finestra.
     * 
     * @return Dimensione calcolata in base alla risoluzione dello schermo
     */
    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension(
                (int) (screenSize.width * UI_Config.WINDOW_WIDTH_RATIO),
                (int) (screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO)
        );
    }

    /**
     * Crea i componenti principali dell'interfaccia.
     */
    private void createComponents() {
        createTitleLabel();
        createRankingList();
    }

    /**
     * Crea e configura l'etichetta del titolo.
     */
    private void createTitleLabel() {
        titleLabel = new JLabel("CLASSIFICA");
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
                getPreferredSize().height * UI_Config.TITLE_FONT_RATIO));
        titleLabel.setForeground(UI_Config.TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Crea e configura la lista per visualizzare la classifica.
     * Include l'inizializzazione del modello dati e la configurazione
     * dell'aspetto grafico e dei listener.
     */
    private void createRankingList() {
        // Inizializzazione base
        rankingModel = new DefaultListModel<>();
        rankingList = new JList<>(rankingModel);
        
        // Configurazione aspetto
        rankingList.setFont(UI_Config.getNormalFont().deriveFont(18f));
        rankingList.setBackground(UI_Config.BUTTON_BASE_COLOR);
        rankingList.setSelectionBackground(UI_Config.BUTTON_HOVER_COLOR);
        rankingList.setBorder(createListBorder());
        rankingList.setForeground(UI_Config.TEXT_COLOR);
        
        // Configurazione comportamento
        configureListRenderer();
        setupKeyListener();
    }

    /**
     * Configura un listener per la tastiera che permette di scaricare
     * i log degli utenti in classifica premendo la barra spaziatrice.
     */
    private void setupKeyListener() {
        rankingList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    downloadSelectedUserLog();
                }
            }
        });
    }

    /**
     * Crea un dialog modale per mostrare lo stato del download in corso.
     * 
     * @param username Nome dell'utente di cui si sta scaricando il log
     * @return Il dialog configurato e pronto per essere visualizzato
     */
    private JDialog createProgressDialog(String username) {
        JDialog dialog = new JDialog(this, "Download in corso...", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UI_Config.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel("Scaricando il log di: " + username);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(UI_Config.TEXT_COLOR);
        label.setFont(UI_Config.getNormalFont().deriveFont(14f));
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setBackground(UI_Config.BUTTON_BASE_COLOR);
        progressBar.setForeground(UI_Config.BUTTON_HOVER_COLOR);
        
        panel.add(label, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.CENTER);
        
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        
        return dialog;
    }

    /**
     * Mostra un messaggio con il risultato dell'operazione di download.
     * Fornisce informazioni dettagliate e suggerimenti in caso di errore.
     * 
     * @param username Nome dell'utente di cui si è tentato il download
     * @param success Esito dell'operazione (true = successo)
     * @param errorMessage Messaggio di errore in caso di fallimento
     */
    private void showDownloadResult(String username, boolean success, String errorMessage) {
        
        if (success) {
            JOptionPane.showMessageDialog(this,
                "Log di '" + username + "' scaricato con successo!\n" +
                "Il file è stato salvato nella cartella:\n" + ResourceLoader.LOGS_DW_DIRECTORY,
                "Download Completato",
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            String detailedMessage = "❌ Impossibile scaricare il log di '" + username + "'.\n\n" +
                "Motivo: " + errorMessage;
            
            // Aggiunge suggerimenti contestuali in base al tipo di errore
            if (errorMessage.contains("Timeout")) {
                detailedMessage += "\n\n💡 Suggerimenti:\n" +
                    "• Verifica la connessione internet\n" +
                    "• Riprova più tardi\n" +
                    "• Il server potrebbe essere sovraccarico";
            } else if (errorMessage.contains("connessione")) {
                detailedMessage += "\n\n💡 Suggerimenti:\n" +
                    "• Controlla la connessione internet\n" +
                    "• Verifica che il server sia raggiungibile";
            }
            
            JOptionPane.showMessageDialog(this,
                detailedMessage,
                "Errore Download",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Gestisce il download del log dell'utente selezionato nella classifica.
     * Verifica la selezione, estrae il nome utente e avvia il processo di download.
     */
    private void downloadSelectedUserLog() {
        int selectedIndex = rankingList.getSelectedIndex();

        // Verifica che ci sia una selezione valida
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleziona prima un utente dalla classifica!",
                    "Nessuna Selezione",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedEntry = rankingModel.getElementAt(selectedIndex);

        // Verifica che la selezione non sia un messaggio di stato/errore
        if (selectedEntry.contains("Impossibile caricare") ||
                selectedEntry.contains("Nessun punteggio") ||
                selectedEntry.contains("Attendere prego")) {
            JOptionPane.showMessageDialog(this,
                    "Impossibile scaricare il log per questa voce.",
                    "Selezione Non Valida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Estrae lo username dalla voce selezionata
        String username = extractUsernameFromEntry(selectedEntry);

        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Impossibile identificare l'utente selezionato.",
                    "Errore Parsing",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Avvia il processo di download
        downloadLogInBackground(username);
    }

    /**
     * Estrae il nome utente da una riga della classifica.
     * Il formato atteso è: "# username Punti: #### (data ora)"
     * 
     * @param entry Riga della classifica da cui estrarre lo username
     * @return Nome utente estratto o null in caso di errore
     */
    private String extractUsernameFromEntry(String entry) {
        if (entry == null || entry.trim().isEmpty()) {
            return null;
        }

        try {
            String cleanEntry = entry.trim();
            
            // Cerca la posizione della parola "Punti:"
            int puntiIndex = cleanEntry.indexOf("Punti:");
            if (puntiIndex == -1) {
                return null;
            }
            
            // Estrae la parte prima di "Punti:"
            String beforePunti = cleanEntry.substring(0, puntiIndex).trim();
            
            // Divide per spazi e prende dal secondo elemento in poi
            String[] parts = beforePunti.split("\\s+");
            if (parts.length < 2) {
                return null;
            }
            
            // Ricostruisce lo username (può contenere spazi)
            StringBuilder username = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) username.append(" ");
                username.append(parts[i]);
            }
            
            return username.toString().trim();
            
        } catch (Exception e) {
            System.err.println("Errore durante estrazione username da: " + entry + " - " + e.getMessage());
            return null;
        }
    }

    /**
     * Esegue il download del file di log in un thread background.
     * Mostra un dialog di progresso durante l'operazione e implementa
     * un meccanismo di timeout per evitare blocchi indefiniti.
     * 
     * @param username Nome dell'utente di cui scaricare il log
     */
    private void downloadLogInBackground(String username) {
        // Dialog di progresso
        JDialog progressDialog = createProgressDialog(username);
        
        // Timer per timeout (30 secondi)
        Timer timeoutTimer = new Timer(30000, e -> {
            SwingUtilities.invokeLater(() -> {
                if (progressDialog.isDisplayable()) {
                    progressDialog.dispose();
                }
                showDownloadResult(username, false, "Timeout: Il download ha impiegato troppo tempo (>30 secondi)");
            });
        });
        timeoutTimer.setRepeats(false);
        timeoutTimer.start();

        // Mostra il dialog dopo la configurazione del timer
        SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));

        // Thread di download
        new Thread(() -> {
            PoggioClientJersey client = null;
            boolean success = false;
            String errorMessage = null;
            boolean wasTimeout = false;

            try {
                client = new PoggioClientJersey();
                
                // Verifica timeout
                if (!timeoutTimer.isRunning()) {
                    wasTimeout = true;
                    errorMessage = "Operazione interrotta per timeout";
                } else {
                    // Esegue il download
                    ApiClientResult result = client.downloadLogFile(username);

                    // Gestisce il risultato
                    switch (result) {
                        case SUCCESS_OK:
                            success = true;
                            break;
                        case USER_NOT_FOUND:
                            errorMessage = "Utente '" + username + "' non trovato sul server.";
                            break;
                        case FILE_ERROR:
                            errorMessage = "Errore durante il salvataggio del file.";
                            break;
                        case CONNECTION_ERROR:
                            errorMessage = "Errore di connessione al server. Verifica la connessione internet.";
                            break;
                        default:
                            errorMessage = "Errore sconosciuto: " + result.toString();
                            break;
                    }
                }

            } catch (Exception e) {
                if (Thread.currentThread().isInterrupted()) {
                    errorMessage = "Download interrotto";
                } else {
                    errorMessage = "Errore imprevisto: " + e.getMessage();
                    e.printStackTrace();
                }
            } finally {
                // Arresta il timer
                if (timeoutTimer.isRunning()) {
                    timeoutTimer.stop();
                }
                
                // Chiude il client API
                if (client != null) {
                    try {
                        client.close();
                    } catch (Exception e) {
                        System.err.println("Errore chiusura client: " + e.getMessage());
                    }
                }
            }

            // Aggiornamento UI sul thread EDT
            final boolean finalSuccess = success;
            final String finalErrorMessage = errorMessage;
            final boolean finalWasTimeout = wasTimeout;

            SwingUtilities.invokeLater(() -> {
                // Chiude il dialog se ancora visibile
                if (progressDialog.isDisplayable()) {
                    progressDialog.dispose();
                }
                
                // Mostra risultato solo se non c'è stato timeout
                if (!finalWasTimeout) {
                    showDownloadResult(username, finalSuccess, finalErrorMessage);
                }
            });

        }, "DownloadThread-" + username).start();
    }

    /**
     * Carica in modo asincrono i dati della classifica dal server.
     * Mostra un messaggio di attesa durante il caricamento e aggiorna
     * l'interfaccia una volta completata l'operazione.
     */
    private void loadRankingData() {
        // Messaggio di attesa
        rankingModel.clear();
        rankingModel.addElement("Attendere prego...");

        // Thread di caricamento dati
        new Thread(() -> {
            PoggioClientJersey client = null;
            List<RankingEntryDTO> rankingData = Collections.emptyList();
            boolean errorOccurred = false;

            try {
                client = new PoggioClientJersey();
                List<RankingEntryDTO> result = client.getRanking();

                if (result != null) {
                    rankingData = result;
                } else {
                    errorOccurred = true;
                }
            } catch (Exception e) {
                new GUIErrorHandler().handleFatalError("Errore recupero classifica: ", e);
                errorOccurred = true;
            } finally {
                if (client != null) {
                    client.close();
                }
            }

            // Prepara variabili per lambda
            final List<RankingEntryDTO> finalRanking = rankingData;
            final boolean finalErrorOccurred = errorOccurred;

            // Aggiorna UI sul thread EDT
            SwingUtilities.invokeLater(() -> {
                updateRankingList(finalRanking, finalErrorOccurred);
            });

        }).start();
    }

    /**
     * Aggiorna la lista della classifica con i dati ricevuti.
     * Gestisce i diversi casi: errore, lista vuota, o dati validi.
     * 
     * @param ranking Lista delle voci di classifica
     * @param errorOccurred Flag che indica se è avvenuto un errore durante il recupero
     */
    private void updateRankingList(List<RankingEntryDTO> ranking, boolean errorOccurred) {
        rankingModel.clear();

        if (errorOccurred) {
            // Errore di comunicazione
            rankingModel.addElement("Impossibile caricare i dati.");
            new GUIErrorHandler().handleRecoverableError("Errore di comunicazione con il server per la classifica.");

        } else if (ranking.isEmpty()) {
            // Nessun dato
            rankingModel.addElement("Nessun punteggio registrato.");

        } else {
            // Visualizza i dati
            int position = 1;
            for (RankingEntryDTO entry : ranking) {
                // Gestione sicura di valori null
                String dateStr = (entry.getData() != null) ? entry.getData().toString() : "N/D";
                String timeStr = (entry.getOra() != null) ? entry.getOra().toString() : "N/D";
                String scoreStr = (entry.getPunteggio() != null) ? entry.getPunteggio().toString() : "N/A";

                // Formattazione per visualizzazione
                String displayString = String.format("%-4d %-25s Punti: %-8s (%s %s)",
                        position++, entry.getUsername(), scoreStr, dateStr, timeStr);
                rankingModel.addElement(displayString);
            }
        }
    }

    /**
     * Configura il renderer personalizzato per le celle della lista.
     * Modifica l'aspetto di ogni cella per migliorare leggibilità e stile.
     */
    private void configureListRenderer() {
        rankingList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
                return label;
            }
        });
    }

    /**
     * Organizza i componenti nella finestra secondo il layout desiderato.
     */
    private void setupLayout() {
        // Pannello principale
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
                UI_Config.BUTTON_INSETS.top,
                UI_Config.BUTTON_INSETS.left,
                UI_Config.BUTTON_INSETS.bottom,
                UI_Config.BUTTON_INSETS.right));

        // Posizionamento titolo
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Configurazione area scrollabile per la lista
        JScrollPane scrollPane = new JScrollPane(rankingList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UI_Config.BACKGROUND_COLOR);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Aggiunta pannello al frame
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Crea un bordo personalizzato per la lista della classifica.
     * 
     * @return Bordo configurato con linea esterna e padding interno
     */
    private Border createListBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(
                        UI_Config.BUTTON_INSETS.top,
                        UI_Config.BUTTON_INSETS.left,
                        UI_Config.BUTTON_INSETS.bottom,
                        UI_Config.BUTTON_INSETS.right));
    }

    /**
     * Restituisce il titolo per la barra della finestra.
     * 
     * @return Titolo della finestra
     */
    @Override
    protected String getWindowTitle() {
        return "Classifica - PoggioAdventure";
    }

    /**
     * Entry point per test standalone della finestra.
     * 
     * @param args Parametri da linea di comando (non utilizzati)
     */
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();
            EventQueue.invokeLater(() -> {
                new UI_Rank().setVisible(true);
            });
        } catch (Exception ex) {
            new GUIErrorHandler().handleFatalError("Errore apertura classifica:", ex);
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
        }
    }
}