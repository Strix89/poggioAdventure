package com.mycompany.poggioadventure.ui.gui.views;

import com.mycompany.poggioadventure.core.utils.ApiClientResult;
// Import PoggioAdventure Core/Utils
import com.mycompany.poggioadventure.core.utils.PoggioClientJersey; // Client per comunicare con l'API backend
import com.mycompany.poggioadventure.core.utils.Utils; // Utility generiche (es. per uscire)

// Import PoggioAdventure UI (Abstract, Enums, Handlers)
import com.mycompany.poggioadventure.ui.UI_Abstract; // Classe base astratta per le finestre UI
import com.mycompany.poggioadventure.ui.ErrorHandler; // Interfaccia per gestione errori
import com.mycompany.poggioadventure.ui.gui.GUIErrorHandler; // Implementazione GUI per ErrorHandler

// Import Swing (GUI)
import javax.swing.*; // Componenti base Swing (JFrame, JButton, JTextField, etc.)
import javax.swing.border.Border; // Interfaccia/Factory per i bordi dei componenti

// Import AWT (Grafica e Eventi)
import java.awt.*; // Classi base AWT (Dimension, Toolkit, LayoutManager, EventQueue, etc.)
import java.awt.event.MouseAdapter; // Adapter per eventi mouse (semplifica implementazione listener)
import java.awt.event.MouseEvent; // Evento del mouse
import java.awt.event.WindowAdapter; // Adapter per eventi finestra
import java.awt.event.WindowEvent; // Evento della finestra

/**
 * Interfaccia grafica (JFrame) per la schermata di creazione di una "Nuova Partita"
 * nell'applicazione PoggioAdventure.
 * Permette all'utente di inserire il proprio nome e avviare una nuova sessione di gioco.
 *
 * <p>Responsabilità principali:
 * <ul>
 * <li>Mostrare un campo di testo per l'inserimento del nome giocatore.</li>
 * <li>Fornire un pulsante per confermare e avviare la partita.</li>
 * <li>Validare l'input (nome non vuoto).</li>
 * <li>Interagire con il backend (tramite {@link PoggioClientJersey}) per verificare se il nome utente esiste già.</li>
 * <li>Gestire la transizione alla finestra di gioco principale ({@link UI_Game}) se l'utente non esiste.</li>
 * <li>Gestire la chiusura della finestra corrente e della finestra "parent" (se fornita).</li>
 * <li>Mostrare messaggi di errore all'utente in caso di problemi (validazione, utente esistente, errori server).</li>
 * </ul>
 *
 * <p>Pattern utilizzati (come da Javadoc originale):
 * <ul>
 * <li>Template Method (grazie all'ereditarietà da {@link UI_Abstract} che definisce la struttura `initComponents`).</li>
 * <li>Observer (implicito nell'uso di ActionListener e MouseListener per gli eventi dei componenti Swing).</li>
 * </ul>
 *
 * @author Strix89 // Autore originale
 */
public class UI_NewGame extends UI_Abstract {

    // ============== COMPONENTI UI ==============
    // Dichiarazione dei componenti grafici usati in questa finestra

    /** Campo di testo dove l'utente inserisce il nome desiderato per la nuova partita. */
    private JTextField nameField;
    /** Pulsante che l'utente preme per avviare la nuova partita dopo aver inserito il nome. */
    private JButton startButton;
    /** Etichetta che mostra il titolo principale della finestra (es. "INSERISCI IL TUO NOME"). */
    private JLabel titleLabel;
    /** Riferimento opzionale alla finestra che ha aperto questa (es. il menu principale).
     * Viene usato per disabilitarla/nasconderla mentre questa finestra è attiva e per chiuderla
     * quando la partita viene avviata. */
    private JFrame parentFrame;

    // ============== COSTRUTTORI ==============

    /**
     * Costruttore principale che accetta una finestra "parent".
     * Quando questa finestra `UI_NewGame` viene creata, la finestra parent viene disabilitata
     * per prevenire interazioni mentre si sceglie il nome. Verrà riabilitata o chiusa
     * a seconda delle azioni successive.
     *
     * @param parent La finestra {@link JFrame} che ha aperto questa schermata (può essere null).
     */
    public UI_NewGame(JFrame parent) {
        super(); // Chiama il costruttore di UI_Abstract (che chiama initComponents)
        this.parentFrame = parent;
        if (parent != null) {
            // Disabilita la finestra parent per dare focus a questa finestra modale-like
            parent.setEnabled(false);
        }
    }

    /**
     * Costruttore alternativo senza una finestra parent.
     * Utile principalmente per testare questa finestra isolatamente o se viene
     * lanciata come prima finestra dell'applicazione.
     */
    public UI_NewGame() {
        super(); // Chiama il costruttore di UI_Abstract (che chiama initComponents)
        // parentFrame rimane null
    }

    // ============== INIZIALIZZAZIONE ==============

    /**
     * Metodo chiamato dalla superclasse {@link UI_Abstract} per inizializzare
     * l'interfaccia utente di questa specifica finestra.
     * Orchestra la creazione e configurazione di tutti gli elementi:
     * configurazione base del frame, creazione dei componenti, definizione del layout,
     * impostazione dei listener per gli eventi e infine dimensionamento della finestra.
     */
    @Override
    protected void initComponents() {
        configureFrame();       // Imposta proprietà base del JFrame
        createComponents();     // Crea istanze dei componenti (label, field, button)
        setupLayout();          // Organizza i componenti nella finestra
        setupEventListeners();  // Aggiunge listener per pulsante e campo testo
        pack();                 // Dimensiona la finestra per adattarsi ai componenti
    }

    // ============== CONFIGURAZIONE FINESTRA ==============

    /**
     * Configura le proprietà fondamentali del {@link JFrame} per questa schermata.
     * Imposta le dimensioni preferite, il comportamento alla chiusura (chiude solo
     * questa finestra), il colore di sfondo, il layout manager principale e
     * aggiunge un listener per gestire la riattivazione della finestra parent
     * quando questa viene chiusa.
     */
    private void configureFrame() {
        setPreferredSize(calculateWindowSize()); // Imposta le dimensioni calcolate
        // Imposta l'operazione di default quando si preme 'X': chiude solo questa finestra
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // Imposta il colore di sfondo usando un valore da UI_Config
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        // Usa BorderLayout come layout principale per il content pane
        getContentPane().setLayout(new BorderLayout());

        // Aggiunge un listener per eventi della finestra
        addWindowListener(new WindowAdapter() {
            /**
             * Metodo chiamato quando la finestra UI_NewGame viene chiusa (dopo DISPOSE_ON_CLOSE).
             * Se esisteva una finestra parent e questa è ancora visualizzabile,
             * la riabilita, permettendo all'utente di interagire di nuovo con essa.
             */
            @Override
            public void windowClosed(WindowEvent e) {
                if (parentFrame != null && parentFrame.isDisplayable()) {
                    parentFrame.setEnabled(true); // Riabilita la finestra chiamante
                }
            }
        });
    }

    /**
     * Calcola le dimensioni desiderate per la finestra "Nuova Partita".
     * Le dimensioni sono calcolate come percentuale delle dimensioni standard
     * definite in {@link UI_Config}, ma ridotte ulteriormente (80% larghezza, 60% altezza)
     * per renderla più piccola, ad esempio, della finestra del menu principale.
     *
     * @return Un oggetto {@link Dimension} con larghezza e altezza calcolate.
     */
    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // Ottiene dimensioni schermo
        // Calcola dimensioni come frazione di quelle standard definite in UI_Config, con riduzione
        return new Dimension(
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO * 0.8), // Larghezza ridotta
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO * 0.6) // Altezza ridotta
        );
    }

    // ============== CREAZIONE COMPONENTI ==============

    /**
     * Crea e configura le istanze dei componenti Swing principali
     * (etichetta titolo, campo di testo nome, pulsante start).
     * Applica stili (font, colori, bordi) definiti in {@link UI_Config}.
     */
    private void createComponents() {
        // --- Creazione Etichetta Titolo ---
        titleLabel = new JLabel("INSERISCI IL TUO NOME");
        // Imposta font (grassetto, dimensione proporzionale ridotta) e colore
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.TITLE_FONT_RATIO * 0.7f));
        titleLabel.setForeground(UI_Config.TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER); // Allinea testo al centro

        // --- Creazione Campo di Testo Nome ---
        nameField = new JTextField();
        // Imposta font (normale, dimensione proporzionale) e colori
        nameField.setFont(UI_Config.getNormalFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO)); // Usa ratio bottone per dimensione
        nameField.setForeground(UI_Config.TEXT_COLOR);
        nameField.setBackground(UI_Config.BUTTON_BASE_COLOR);
        nameField.setCaretColor(UI_Config.TEXT_COLOR); // Colore del cursore testo
        nameField.setBorder(createTextFieldBorder()); // Applica bordo personalizzato
        nameField.setHorizontalAlignment(SwingConstants.CENTER); // Allinea testo inserito al centro

        // --- Creazione Pulsante Start ---
        startButton = new JButton("START");
        // Imposta font (grassetto, dimensione proporzionale) e colori
        startButton.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO));
        startButton.setForeground(UI_Config.TEXT_COLOR);
        startButton.setBackground(UI_Config.BUTTON_BASE_COLOR);
        startButton.setBorder(createButtonBorder()); // Applica bordo personalizzato
        startButton.setFocusPainted(false); // Non disegna il bordo tratteggiato del focus
        // Cambia il cursore a "mano" quando si passa sopra il pulsante
        startButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Aggiunge l'effetto di cambio colore al passaggio del mouse
        addButtonHoverEffect(startButton);
    }

    // ============== LAYOUT ==============

    /**
     * Configura il layout dei componenti all'interno della finestra.
     * Utilizza un pannello principale con {@link GridBagLayout} per posizionare
     * il titolo, il campo nome e il pulsante start in modo flessibile e centrato.
     * Applica margini e padding tramite {@link GridBagConstraints} e bordi.
     */
    private void setupLayout() {
        // Pannello principale che userà GridBagLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false); // Rende il pannello trasparente per mostrare lo sfondo del frame
        // Aggiunge un bordo vuoto attorno al pannello per dare spazio dai bordi della finestra
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // top, left, bottom, right

        // Oggetto per definire i vincoli di posizionamento nel GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // Spazio esterno (padding) attorno a ogni componente
        gbc.weightx = 1; // Permette ai componenti di espandersi orizzontalmente se necessario
        gbc.fill = GridBagConstraints.HORIZONTAL; // Fa sì che i componenti riempiano lo spazio orizzontale

        // --- Posizionamento Titolo ---
        gbc.gridy = 0; // Riga 0
        gbc.anchor = GridBagConstraints.CENTER; // Ancoraggio al centro della cella
        mainPanel.add(titleLabel, gbc);

        // --- Posizionamento Campo Nome ---
        gbc.gridy = 1; // Riga 1
        gbc.ipady = 20; // Padding interno verticale (aumenta altezza componente)
        // Imposta dimensioni preferite per il campo nome (basate sulla finestra)
        nameField.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * 0.7),  // 70% larghezza finestra
            (int)(getPreferredSize().height * 0.1) // 10% altezza finestra
        ));
        // Imposta una dimensione fissa per il font del campo nome (ignora quella precedente?)
        nameField.setFont(UI_Config.getBoldFont().deriveFont(22f)); // Dimensione 22, grassetto
        mainPanel.add(nameField, gbc);

        // --- Posizionamento Pulsante Start ---
        gbc.gridy = 2; // Riga 2
        gbc.ipady = 0; // Reset padding interno verticale
        gbc.fill = GridBagConstraints.NONE; // Non riempie più lo spazio orizzontale
        // Imposta dimensioni preferite per il pulsante (leggermente più grandi delle standard)
        startButton.setPreferredSize(new Dimension(
            (int)(getPreferredSize().width * (UI_Config.BUTTON_WIDTH_RATIO + 0.1)),
            (int)(getPreferredSize().height * (UI_Config.BUTTON_HEIGHT_RATIO + 0.05))
        ));
        // Imposta una dimensione fissa per il font del pulsante (ignora quella precedente?)
        startButton.setFont(UI_Config.getBoldFont().deriveFont(22f)); // Dimensione 22, grassetto
        mainPanel.add(startButton, gbc);

        // Aggiunge il pannello principale al centro del BorderLayout del frame
        add(mainPanel, BorderLayout.CENTER);
    }

    // ============== STILE COMPONENTI ==============

    /**
     * Crea un bordo personalizzato per il campo di testo {@code nameField}.
     * Combina un bordo esterno a linea (con colore e spessore definiti)
     * e un bordo interno vuoto per creare padding dentro il campo.
     * @return Un oggetto {@link Border} configurato.
     */
    private Border createTextFieldBorder() {
        return BorderFactory.createCompoundBorder( // Combina due bordi
            // Bordo esterno: linea
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2), // Colore da config, spessore 2
            // Bordo interno: spazio vuoto (padding)
            BorderFactory.createEmptyBorder(10, 15, 10, 15) // top, left, bottom, right
        );
    }

    /**
     * Crea un bordo personalizzato per i pulsanti (in questo caso, {@code startButton}).
     * Simile a {@code createTextFieldBorder}, combina una linea esterna e padding interno.
     * @return Un oggetto {@link Border} configurato.
     */
    private Border createButtonBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(5, 25, 5, 25) // Padding diverso dal text field
        );
    }

    /**
     * Aggiunge un effetto visivo di "hover" a un pulsante {@link JButton}.
     * Cambia il colore di sfondo del pulsante quando il mouse entra nell'area
     * del pulsante e lo ripristina quando il mouse esce.
     * @param button Il pulsante a cui aggiungere l'effetto hover.
     */
    private void addButtonHoverEffect(JButton button) {
        // Aggiunge un listener per gli eventi del mouse
        button.addMouseListener(new MouseAdapter() {
            // Chiamato quando il cursore entra nell'area del pulsante
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(UI_Config.BUTTON_HOVER_COLOR); // Imposta colore hover
            }

            // Chiamato quando il cursore esce dall'area del pulsante
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(UI_Config.BUTTON_BASE_COLOR); // Ripristina colore base
            }
        });
    }

    // ============== GESTIONE EVENTI ==============

    /**
     * Configura i listener per gli eventi principali dell'interfaccia:
     * - Click sul pulsante "Start".
     * - Pressione del tasto Invio nel campo nome {@code nameField}.
     * Entrambi gli eventi triggerano l'azione di avvio partita ({@code handleStartGame}).
     */
    private void setupEventListeners() {
        // Aggiunge un ActionListener al pulsante: eseguito al click
        startButton.addActionListener(e -> handleStartGame());
        // Aggiunge un ActionListener al campo testo: eseguito alla pressione di Invio
        nameField.addActionListener(e -> handleStartGame());
    }

    // ============== LOGICA APPLICATIVA ==============

    /**
     * Gestisce la logica principale quando l'utente tenta di avviare una nuova partita
     * (premendo il pulsante Start o Invio nel campo nome).
     * <p>Passi:
     * <ol>
     * <li>Recupera e valida il nome inserito (non deve essere vuoto).</li>
     * <li>Chiude questa finestra (`dispose()`) e la finestra parent (`parentFrame.dispose()`), se esiste.</li>
     * <li>Chiama il client API (`PoggioClientJersey`) per verificare se l'utente esiste già sul server (*ATTENZIONE: chiamata di rete sull'EDT!*).</li>
     * <li>Se l'utente NON esiste ({@code USER_NOT_FOUND}): lancia la finestra di gioco {@link UI_Game} usando {@code EventQueue.invokeLater}.</li>
     * <li>Se l'utente ESISTE GIA' ({@code SUCCESS_OK}) o si verifica un errore di connessione/sconosciuto: mostra un messaggio di errore tramite {@link GUIErrorHandler}.</li>
     * </ol>
     */
    private void handleStartGame() {
        String playerName = nameField.getText().trim(); // Ottiene il nome e rimuove spazi bianchi
        ErrorHandler errorHan = new GUIErrorHandler(); // Istanza per mostrare errori GUI

        // 1. Validazione Input
        if (playerName.isEmpty()) {
            errorHan.handleRecoverableError("Il nome del giocatore non può essere vuoto!");
            return; // Interrompe l'azione se il nome non è valido
        }

        // 2. Chiusura Finestre Attuali/Parent
        if(parentFrame != null) parentFrame.dispose(); // Chiude la finestra parent (es. menu)
        dispose(); // Chiude questa finestra (UI_NewGame)

        // 3. Verifica Esistenza Utente su Server (*Eseguita sull'EDT - Rischio Blocco UI*)
        PoggioClientJersey gameClient = null; // Dichiarato fuori per chiusura nel finally implicito (ma non c'è finally qui)
        try {
             gameClient = new PoggioClientJersey();
            // Esegue la chiamata sincrona al backend
            ApiClientResult result = gameClient.checkUserExists(playerName);
             gameClient.close(); // Chiude il client

            // 4. Gestione Risultato Verifica
            switch(result){
                case USER_NOT_FOUND -> // Utente non trovato -> Procedi con l'avvio del gioco
                    // Usa invokeLater per assicurare che la creazione della nuova UI avvenga sull'EDT
                    EventQueue.invokeLater(() -> {
                        try {
                            // Crea e rende visibile la finestra principale del gioco
                            new UI_Game(playerName).setVisible(true);
                        } catch (Exception e) {
                            // Gestisce errori critici durante l'inizializzazione di UI_Game
                            new GUIErrorHandler().handleFatalError("Errore critico durante l'avvio della partita: ", e);
                             Utils.exitApplication(Utils.EXIT_CODE_CRITICAL); // Esce in caso di errore fatale
                        }
                    });
                // Fine caso USER_NOT_FOUND
                case SUCCESS_OK -> {// Utente già esistente sul server
                    errorHan.handleRecoverableError("Errore: L'utente '" + playerName + "' esiste già. Carica la partita o scegli un nome diverso.");
                    new UI_Init().setVisible(true);
                // L'utente vedrà questo messaggio, ma le finestre precedenti sono già chiuse.
                // Potrebbe essere necessario un modo per tornare al menu principale qui.
                }
                case CONNECTION_ERROR -> // Errore di connessione al server
                    errorHan.handleRecoverableError("Errore di connessione: Impossibile comunicare con il server per verificare l'utente.");

                default -> // Altri errori sconosciuti dal server
                    errorHan.handleRecoverableError("Errore sconosciuto durante la verifica dell'utente (" + result + ").");
            }
        } catch (Exception e) {
             // Cattura eccezioni durante creazione/uso/chiusura del client Jersey
             errorHan.handleRecoverableError("Errore imprevisto durante la comunicazione con il server: " + e.getMessage());
            if (gameClient != null) {
                try { gameClient.close(); } catch (Exception ce) { /* ignora errore chiusura */ }
            }
        }
    }

    // ============== METODI OVERRIDE ==============

    /**
     * Restituisce il testo da visualizzare nella barra del titolo della finestra.
     * Sovrascrive il metodo (probabilmente astratto) di {@link UI_Abstract}.
     * @return Il titolo della finestra "Nuova Partita".
     */
    @Override
    protected String getWindowTitle() {
        return "Nuova Partita - PoggioAdventure";
    }

    // ============== MAIN PER TEST ==============

    /**
     * Metodo main per avviare questa finestra ({@code UI_NewGame}) in modo indipendente,
     * utile per testare l'interfaccia grafica isolatamente.
     * Assicura che la creazione della finestra avvenga sull'Event Dispatch Thread (EDT) di Swing.
     * @param args Argomenti della riga di comando (non utilizzati).
     */
    public static void main(String[] args) {
        try {
            // Schedula la creazione e visualizzazione della GUI sull'EDT
            EventQueue.invokeLater(() -> {
                new UI_NewGame().setVisible(true);
            });
        } catch (Exception ex) {
            // Gestisce errori critici durante l'inizializzazione Swing/L&F o altro
            new GUIErrorHandler().handleFatalError("Errore durante l'inizializzazione della UI:", ex);
            // Termina l'applicazione in caso di errore critico all'avvio
            Utils.exitApplication(Utils.EXIT_CODE_CRITICAL);
        }
    }
}