package com.mycompany.poggioadventure.ui.gui.views;

import com.formdev.flatlaf.FlatLightLaf; // Look and Feel FlatLaf leggero
import com.mycompany.poggioadventure.core.utils.ApiClientResult;
import com.mycompany.poggioadventure.core.utils.PoggioClientJersey; // Client per interagire con l'API del backend
import com.mycompany.poggioadventure.ui.UI_Abstract; // Classe astratta base per le interfacce UI
import com.mycompany.poggioadventure.core.utils.Utils; // Utilità generiche dell'applicazione
import com.mycompany.poggioadventure.persistence.RankingEntryDTO; // Data Transfer Object per le voci della classifica
import com.mycompany.poggioadventure.persistence.ResourceLoader;
import com.mycompany.poggioadventure.ui.gui.GUIErrorHandler; // Gestore centralizzato per gli errori della GUI
import javax.swing.*; // Componenti base di Swing
import javax.swing.border.Border; // Interfaccia per la gestione dei bordi dei componenti
import java.awt.*; // Classi base AWT (dimensioni, colori, layout, ecc.)
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collections; // Utilità per le collezioni (usato per lista vuota)
import java.util.List; // Interfaccia per le liste

/**
 * Interfaccia grafica (JFrame) per la visualizzazione della classifica
 * dei giocatori in PoggioAdventure.
 *
 * <p>
 * Responsabilità principali:
 * <ul>
 * <li>Recuperare i dati della classifica tramite il client API.</li>
 * <li>Visualizzare la classifica dei punteggi in una lista ordinata.</li>
 * <li>Gestire il caricamento asincrono dei dati per non bloccare
 * l'interfaccia.</li>
 * <li>Fornire una visualizzazione chiara e leggibile all'interno della
 * finestra.</li>
 * </ul>
 *
 * <p>
 * Caratteristiche:
 * <ul>
 * <li>Layout responsive basato sulle dimensioni dello schermo.</li>
 * <li>Stile visivo coerente con il tema dell'applicazione (definito in
 * UI_Config e FlatLaf).</li>
 * <li>Scroll automatico per liste lunghe tramite JScrollPane.</li>
 * </ul>
 *
 * @author Strix89
 */
public class UI_Rank extends UI_Abstract {

    // ============== COMPONENTI UI ==============
    private JList<String> rankingList; // Componente JList per visualizzare le voci della classifica
    private JLabel titleLabel; // Etichetta per il titolo "CLASSIFICA"
    private DefaultListModel<String> rankingModel; // Modello dati che alimenta la JList 'rankingList'

    // ============== COSTRUTTORE ==============

    /**
     * Costruisce una nuova istanza della finestra della classifica.
     * Chiama il costruttore della superclasse UI_Abstract, che a sua volta
     * invocherà il metodo initComponents() definito qui per costruire la UI.
     */
    public UI_Rank() {
        super(); // Chiama il costruttore di UI_Abstract
    }

    // ============== INIZIALIZZAZIONE ==============

    /**
     * Metodo chiamato dalla superclasse per inizializzare i componenti della UI.
     * Orchestra la configurazione della finestra, la creazione dei componenti,
     * l'impostazione del layout e l'avvio del caricamento dei dati.
     */
    @Override
    protected void initComponents() {
        configureFrame(); // Imposta proprietà base del JFrame
        createComponents(); // Crea le istanze dei componenti Swing
        setupLayout(); // Organizza i componenti nella finestra usando layout manager
        loadRankingData(); // Avvia il processo di recupero dati dal backend
        pack(); // Dimensiona la finestra per adattarsi ai componenti contenuti
    }

    // ============== CONFIGURAZIONE FINESTRA ==============

    /**
     * Configura le proprietà principali del JFrame:
     * - Imposta le dimensioni preferite calcolate in base allo schermo.
     * - Definisce l'operazione di chiusura (dispose chiude solo questa finestra).
     * - Imposta il colore di sfondo del content pane.
     * - Imposta il layout manager principale (BorderLayout) per il content pane.
     */
    private void configureFrame() {
        setPreferredSize(calculateWindowSize()); // Imposta dimensione desiderata
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Chiude solo questa finestra al click sulla 'X'
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR); // Colore sfondo da configurazione
        getContentPane().setLayout(new BorderLayout()); // Layout principale
    }

    /**
     * Calcola le dimensioni della finestra come frazione delle dimensioni dello
     * schermo.
     * Utilizza rapporti definiti in UI_Config per mantenere la proporzionalità.
     * 
     * @return Un oggetto Dimension con larghezza e altezza calcolate.
     */
    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Ottiene dimensioni schermo
        return new Dimension(
                (int) (screenSize.width * UI_Config.WINDOW_WIDTH_RATIO), // Calcola larghezza
                (int) (screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO) // Calcola altezza
        );
    }

    // ============== CREAZIONE COMPONENTI ==============

    /**
     * Metodo contenitore che chiama i metodi specifici per creare
     * i singoli componenti principali dell'interfaccia.
     */
    private void createComponents() {
        createTitleLabel(); // Crea l'etichetta del titolo
        createRankingList(); // Crea la lista per la classifica
    }

    /**
     * Crea e configura l'etichetta JLabel per il titolo "CLASSIFICA".
     * Imposta font, colore, dimensione (derivata dall'altezza della finestra)
     * e allineamento del testo.
     */
    private void createTitleLabel() {
        titleLabel = new JLabel("CLASSIFICA");
        // Imposta il font usando quello definito in UI_Config, in grassetto,
        // con dimensione calcolata proporzionalmente all'altezza della finestra.
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
                getPreferredSize().height * UI_Config.TITLE_FONT_RATIO));
        titleLabel.setForeground(UI_Config.TEXT_COLOR); // Colore testo da configurazione
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER); // Allinea testo al centro
    }

    /**
     * Crea e configura la JList che visualizzerà la classifica.
     * Inizializza il DefaultListModel, crea la JList associata, imposta
     * font, colori di sfondo e selezione, bordo e colore del testo.
     * Configura anche un renderer personalizzato per l'aspetto delle celle.
     */
    private void createRankingList() {
        rankingModel = new DefaultListModel<>(); // Modello dati vuoto
        rankingList = new JList<>(rankingModel); // Crea JList collegata al modello
        rankingList.setFont(UI_Config.getNormalFont().deriveFont(18f)); // Font normale, dimensione fissa
        rankingList.setBackground(UI_Config.BUTTON_BASE_COLOR); // Sfondo della lista
        rankingList.setSelectionBackground(UI_Config.BUTTON_HOVER_COLOR); // Sfondo cella selezionata
        rankingList.setBorder(createListBorder()); // Bordo personalizzato
        rankingList.setForeground(UI_Config.TEXT_COLOR); // Colore testo elementi
        configureListRenderer(); // Imposta renderer custom per le celle
        setupKeyListener();
    }

    /**
     * Configura il listener per i tasti della JList.
     * Gestisce la pressione della barra spaziatrice per scaricare i log.
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
     * Crea un dialog di progresso per il download.
     * 
     * @param username L'utente di cui si sta scaricando il log
     * @return Il dialog configurato
     */
    private JDialog createProgressDialog(String username) {
        JDialog dialog = new JDialog(this, "Download in corso...", true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Utilizza colori da UI_Config
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(UI_Config.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel label = new JLabel("Scaricando il log di: " + username);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setForeground(UI_Config.TEXT_COLOR); // Testo bianco da UI_Config
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
     * Mostra il risultato del download all'utente.
     * 
     * @param username L'utente di cui si è tentato il download
     * @param success Se il download è riuscito
     * @param errorMessage Il messaggio di errore (se presente)
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
            
            // Aggiungi suggerimenti in base al tipo di errore
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
     * Scarica il file di log dell'utente selezionato nella classifica.
     * Verifica che ci sia una selezione valida, estrae lo username,
     * e avvia il download in background.
     */
    private void downloadSelectedUserLog() {
        int selectedIndex = rankingList.getSelectedIndex();

        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this,
                    "Seleziona prima un utente dalla classifica!",
                    "Nessuna Selezione",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String selectedEntry = rankingModel.getElementAt(selectedIndex);

        // Verifica che non sia un messaggio di errore o stato
        if (selectedEntry.contains("Impossibile caricare") ||
                selectedEntry.contains("Nessun punteggio") ||
                selectedEntry.contains("Attendere prego")) {
            JOptionPane.showMessageDialog(this,
                    "Impossibile scaricare il log per questa voce.",
                    "Selezione Non Valida",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Estrae lo username dalla stringa formattata
        String username = extractUsernameFromEntry(selectedEntry);

        if (username == null || username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Impossibile identificare l'utente selezionato.",
                    "Errore Parsing",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Avvia il download in background
        downloadLogInBackground(username);
    }

    /**
     * Estrae lo username dalla stringa formattata della classifica.
     * Formato atteso: "1 username Punti: 1000 (2024-01-01 12:00:00)"
     * 
     * @param entry La stringa formattata della voce di classifica
     * @return Lo username estratto o null se non trovato
     */
    private String extractUsernameFromEntry(String entry) {
        if (entry == null || entry.trim().isEmpty()) {
            return null;
        }

        try {
            // Rimuove spazi extra e divide la stringa
            String cleanEntry = entry.trim();
            
            // Cerca la posizione della parola "Punti:"
            int puntiIndex = cleanEntry.indexOf("Punti:");
            if (puntiIndex == -1) {
                return null; // Formato non valido
            }
            
            // Estrae la parte prima di "Punti:"
            String beforePunti = cleanEntry.substring(0, puntiIndex).trim();
            
            // Divide per spazi e prende dal secondo elemento in poi (saltando il numero di posizione)
            String[] parts = beforePunti.split("\\s+");
            if (parts.length < 2) {
                return null; // Non c'è abbastanza informazione
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
     * Avvia il download del file di log in un thread separato
     * per non bloccare l'interfaccia utente.
     * 
     * @param username Lo username di cui scaricare il log
     */
    private void downloadLogInBackground(String username) {
        // Mostra dialog di progresso
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

        // Mostra il dialog DOPO aver configurato il timer
        SwingUtilities.invokeLater(() -> progressDialog.setVisible(true));

        new Thread(() -> {
            PoggioClientJersey client = null;
            boolean success = false;
            String errorMessage = null;
            boolean wasTimeout = false;

            try {
                client = new PoggioClientJersey();
                
                // Controlla se il timer è ancora attivo (non è scaduto)
                if (!timeoutTimer.isRunning()) {
                    wasTimeout = true;
                    errorMessage = "Operazione interrotta per timeout";
                } else {
                    ApiClientResult result = client.downloadLogFile(username);

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
                    e.printStackTrace(); // Per debug
                }
            } finally {
                // Ferma il timer se è ancora attivo
                if (timeoutTimer.isRunning()) {
                    timeoutTimer.stop();
                }
                
                if (client != null) {
                    try {
                        client.close();
                    } catch (Exception e) {
                        // Ignora errori di chiusura
                        System.err.println("Errore chiusura client: " + e.getMessage());
                    }
                }
            }

            // Aggiorna UI sul thread EDT
            final boolean finalSuccess = success;
            final String finalErrorMessage = errorMessage;
            final boolean finalWasTimeout = wasTimeout;

            SwingUtilities.invokeLater(() -> {
                // Chiudi il dialog se ancora visibile
                if (progressDialog.isDisplayable()) {
                    progressDialog.dispose();
                }
                
                // Mostra risultato solo se non c'è stato timeout 
                // (il timeout mostra già il suo messaggio tramite il timer)
                if (!finalWasTimeout) {
                    showDownloadResult(username, finalSuccess, finalErrorMessage);
                }
            });

        }, "DownloadThread-" + username).start(); // Nome thread per debug
    }

    /**
     * Avvia il caricamento asincrono dei dati della classifica dal backend.
     * Utilizza un nuovo Thread per eseguire l'operazione di rete (chiamata API)
     * per evitare di bloccare l'Event Dispatch Thread (EDT) di Swing.
     * Mostra un messaggio temporaneo "Attendere prego..." nella lista.
     * Al termine dell'operazione (successo o errore), aggiorna la JList
     * sull'EDT usando SwingUtilities.invokeLater().
     */
    private void loadRankingData() {
        // 1. Preparazione UI (sull'EDT): mostra messaggio di attesa
        rankingModel.clear();
        rankingModel.addElement("Attendere prego...");
        // Qui si potrebbero disabilitare altri componenti se necessario

        // 2. Creazione ed avvio del Thread background per l'operazione di rete
        new Thread(() -> {
            PoggioClientJersey client = null;
            List<RankingEntryDTO> rankingData = Collections.emptyList(); // Lista vuota di default
            boolean errorOccurred = false; // Flag per tracciare errori gravi

            try {
                client = new PoggioClientJersey(); // Istanzia il client API
                List<RankingEntryDTO> result = client.getRanking(); // Esegue la chiamata API (bloccante)

                // Controlla se il risultato è valido (getRanking ritorna null in caso di errore
                // gestito internamente)
                if (result != null) {
                    rankingData = result; // Assegna i dati ottenuti
                } else {
                    // Errore gestito dal client (es. risposta non 200 OK), ma è comunque un
                    // fallimento
                    errorOccurred = true;
                }
            } catch (Exception e) {
                // Cattura eccezioni *impreviste* durante creazione/chiamata client nel thread
                // background
                new GUIErrorHandler().handleFatalError("ERRORE background thread recupero classifica: ", e);
                errorOccurred = true; // Segnala l'errore
            } finally {
                // Blocco finally per assicurare la chiusura del client Jersey
                // Indipendentemente da successo o eccezioni nel try-catch.
                if (client != null) {
                    client.close(); // Rilascia le risorse del client (es. connessioni)
                }
            }

            // Prepara le variabili per l'uso nella lambda di invokeLater
            // Devono essere final o effettivamente final.
            final List<RankingEntryDTO> finalRanking = rankingData;
            final boolean finalErrorOccurred = errorOccurred;

            // 3. Pianifica l'aggiornamento della UI sull'EDT
            // Usa invokeLater per garantire che le modifiche ai componenti Swing
            // avvengano nel thread corretto.
            SwingUtilities.invokeLater(() -> {
                updateRankingList(finalRanking, finalErrorOccurred);
            });

        }).start(); // Avvia l'esecuzione del thread background
    }

    /**
     * Aggiorna il contenuto della JList (rankingList) con i dati della classifica
     * ricevuti dal thread background. Questo metodo DEVE essere eseguito sull'EDT.
     * Gestisce i diversi scenari: errore durante il caricamento, classifica vuota,
     * classifica popolata. Formatta ciascuna voce della classifica prima di
     * aggiungerla.
     *
     * @param ranking       La lista di RankingEntryDTO ottenuta (può essere vuota).
     * @param errorOccurred true se si è verificato un errore grave durante il
     *                      recupero dati.
     */
    private void updateRankingList(List<RankingEntryDTO> ranking, boolean errorOccurred) {
        rankingModel.clear(); // Rimuove il messaggio "Attendere prego..." o i dati precedenti

        if (errorOccurred) {
            // Caso: Errore durante il recupero dati
            rankingModel.addElement("Impossibile caricare i dati.");
            // Notifica l'utente tramite il gestore di errori (mostra popup/log)
            new GUIErrorHandler().handleRecoverableError("Errore di comunicazione con il server per la classifica.");

        } else if (ranking.isEmpty()) {
            // Caso: Recupero riuscito, ma non ci sono punteggi
            rankingModel.addElement("Nessun punteggio registrato.");

        } else {
            // Caso: Recupero riuscito, ci sono punteggi da visualizzare
            int position = 1; // Contatore per la posizione in classifica
            for (RankingEntryDTO entry : ranking) {
                // Gestione sicura di valori potenzialmente null dal DTO
                String dateStr = (entry.getData() != null) ? entry.getData().toString() : "N/D";
                String timeStr = (entry.getOra() != null) ? entry.getOra().toString() : "N/D";
                String scoreStr = (entry.getPunteggio() != null) ? entry.getPunteggio().toString() : "N/A";

                // Formattazione della stringa per l'elemento della lista:
                // %-4d: numero intero, allineato a sinistra, larghezza 4
                // %-25s: stringa (username), allineata a sinistra, larghezza 25
                // Punti: %-8s: stringa (punteggio), allineata a sinistra, larghezza 8
                // (%s %s): stringhe (data e ora)
                String displayString = String.format("%-4d %-25s Punti: %-8s (%s %s)",
                        position++, entry.getUsername(), scoreStr, dateStr, timeStr);
                rankingModel.addElement(displayString); // Aggiunge la stringa formattata al modello
            }
        }
    }

    /**
     * Configura un renderer personalizzato per le celle della JList 'rankingList'.
     * Utilizza un DefaultListCellRenderer anonimo per modificare l'aspetto
     * di ciascuna cella: centra il testo orizzontalmente e aggiunge padding
     * verticale.
     */
    private void configureListRenderer() {
        rankingList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                // Chiama il metodo della superclasse per ottenere il componente JLabel di base
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                // Personalizzazione: centra il testo
                label.setHorizontalAlignment(SwingConstants.CENTER);
                // Personalizzazione: aggiunge spazio sopra e sotto il testo (padding)
                label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // top, left, bottom, right
                return label; // Restituisce il JLabel configurato
            }
        });
    }

    // ============== LAYOUT ==============

    /**
     * Organizza i componenti creati all'interno della finestra usando layout
     * manager.
     * Utilizza un pannello principale (mainPanel) con BorderLayout per contenere
     * il titolo (NORTH) e la lista scorrevole (CENTER).
     * Aggiunge padding attorno al pannello principale.
     */
    private void setupLayout() {
        // Pannello principale che conterrà titolo e lista
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false); // Rende il pannello trasparente per mostrare lo sfondo del frame
        // Aggiunge un bordo vuoto (padding) attorno al pannello principale
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
                UI_Config.BUTTON_INSETS.top,
                UI_Config.BUTTON_INSETS.left,
                UI_Config.BUTTON_INSETS.bottom,
                UI_Config.BUTTON_INSETS.right));

        // Aggiunge il titolo in alto (NORTH)
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Crea uno JScrollPane per permettere lo scorrimento della lista se è lunga
        JScrollPane scrollPane = new JScrollPane(rankingList);
        scrollPane.setBorder(null); // Rimuove il bordo predefinito dello JScrollPane
        // Imposta lo sfondo del viewport (l'area visibile dello scrollpane)
        scrollPane.getViewport().setBackground(UI_Config.BACKGROUND_COLOR);
        // Aggiunge lo JScrollPane con la lista al centro (CENTER)
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Aggiunge il pannello principale al content pane del JFrame (al centro)
        add(mainPanel, BorderLayout.CENTER);
    }

    // ============== STILE COMPONENTI ==============

    /**
     * Crea un bordo composito per la JList 'rankingList'.
     * Combina un bordo esterno (linea colorata) con un bordo interno vuoto
     * (padding).
     * 
     * @return Un oggetto Border configurato.
     */
    private Border createListBorder() {
        // Crea un bordo composto da:
        return BorderFactory.createCompoundBorder(
                // 1. Bordo esterno: una linea con colore e spessore definiti
                BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
                // 2. Bordo interno: uno spazio vuoto (padding)
                BorderFactory.createEmptyBorder(
                        UI_Config.BUTTON_INSETS.top,
                        UI_Config.BUTTON_INSETS.left,
                        UI_Config.BUTTON_INSETS.bottom,
                        UI_Config.BUTTON_INSETS.right));
    }

    // ============== METODI OVERRIDE ==============

    /**
     * Fornisce il titolo da visualizzare nella barra del titolo della finestra.
     * Questo metodo sovrascrive un metodo astratto (o concreto) della superclasse
     * UI_Abstract.
     * 
     * @return Il titolo della finestra.
     */
    @Override
    protected String getWindowTitle() {
        return "Classifica - PoggioAdventure";
    }

    // ============== MAIN PER TEST ==============

    /**
     * Metodo main per avviare questa finestra (UI_Rank) in modo indipendente,
     * utile per testare l'interfaccia grafica isolatamente.
     * Configura il Look and Feel FlatLaf e crea/visualizza l'istanza di UI_Rank
     * sull'Event Dispatch Thread (EDT) di Swing.
     * 
     * @param args Argomenti della riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        try {
            // Imposta il Look and Feel FlatLaf (tema chiaro)
            FlatLightLaf.setup();
            // Schedula la creazione e visualizzazione della GUI sull'EDT
            EventQueue.invokeLater(() -> {
                new UI_Rank().setVisible(true);
            });
        } catch (Exception ex) {
            // Gestisce errori critici durante l'inizializzazione (es. L&F non trovato)
            new GUIErrorHandler().handleFatalError("Errore apertura classifica:", ex);
            // Termina l'applicazione in caso di errore critico all'avvio
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
        }
    }
}