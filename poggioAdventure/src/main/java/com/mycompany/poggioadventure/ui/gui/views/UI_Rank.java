package com.mycompany.poggioadventure.ui.gui.views;

import com.formdev.flatlaf.FlatLightLaf;
import com.mycompany.poggioadventure.ui.UI_Abstract;
import com.mycompany.poggioadventure.core.utils.Utils;
import com.mycompany.poggioadventure.ui.gui.GUIErrorHandler;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Interfaccia per la visualizzazione della classifica dei giocatori in PoggioAdventure.
 * 
 * <p>Responsabilità principali:
 * <ul>
 *   <li>Visualizzare la classifica dei punteggi</li>
 *   <li>Ordinare i giocatori per punteggio decrescente</li>
 *   <li>Fornire una visualizzazione chiara e leggibile</li>
 * </ul>
 * 
 * <p>Caratteristiche:
 * <ul>
 *   <li>Layout responsive basato sulle dimensioni dello schermo</li>
 *   <li>Stile visivo coerente con il tema dell'applicazione</li>
 *   <li>Scroll automatico per liste lunghe</li>
 * </ul>
 *
 * @author Strix89
 */
public class UI_Rank extends UI_Abstract {

    // ============== COMPONENTI UI ==============
    private JList<String> rankingList;      // Componente lista per la classifica
    private JLabel titleLabel;             // Titolo della finestra
    private DefaultListModel<String> rankingModel;  // Modello dati per la lista

    // ============== COSTRUTTORE ==============
    
    /**
     * Inizializza la finestra della classifica.
     * Configura l'interfaccia utente e carica i dati della classifica.
     */
    public UI_Rank() {
        super();
    }

    // ============== INIZIALIZZAZIONE ==============
    
    /**
     * Configura l'interfaccia utente come richiesto da UI_Abstract.
     * Crea e posiziona tutti i componenti grafici.
     */
    @Override
    protected void initComponents() {
        configureFrame();
        createComponents();
        setupLayout();
        pack();
    }

    // ============== CONFIGURAZIONE FINESTRA ==============
    
    /**
     * Imposta le proprietà base della finestra:
     * - Dimensioni responsive
     * - Comportamento chiusura
     * - Sfondo e layout
     */
    private void configureFrame() {
        setPreferredSize(calculateWindowSize());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);
        getContentPane().setLayout(new BorderLayout());
    }

    /**
     * Calcola dimensioni in base allo schermo utilizzando i rapporti standard.
     */
    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Dimension(
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO),
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO)
        );
    }

    // ============== CREAZIONE COMPONENTI ==============
    
    /**
     * Crea e configura i componenti principali:
     * - Titolo della classifica
     * - Lista dei punteggi
     */
    private void createComponents() {
        createTitleLabel();
        createRankingList();
    }

    /**
     * Crea e configura il titolo della classifica.
     */
    private void createTitleLabel() {
        titleLabel = new JLabel("CLASSIFICA");
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.TITLE_FONT_RATIO));
        titleLabel.setForeground(UI_Config.TEXT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Crea e configura la lista della classifica.
     */
    private void createRankingList() {
        rankingModel = new DefaultListModel<>();
        loadSampleData(); // TODO: Sostituire con dati reali
        
        rankingList = new JList<>(rankingModel);
        rankingList.setFont(UI_Config.getNormalFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO));
        rankingList.setBackground(UI_Config.BUTTON_BASE_COLOR);
        rankingList.setSelectionBackground(UI_Config.BUTTON_HOVER_COLOR);
        rankingList.setBorder(createListBorder());
        rankingList.setForeground(UI_Config.TEXT_COLOR);
        
        configureListRenderer();
    }

    /**
     * Carica dati di esempio (da sostituire con implementazione reale).
     */
    private void loadSampleData() {
        rankingModel.addElement("1. Giocatore1 - Punti: 9500 - Lvl 10");
        rankingModel.addElement("2. Giocatore2 - Punti: 8700 - Lvl 9");
        rankingModel.addElement("3. Giocatore3 - Punti: 8200 - Lvl 8");
        rankingModel.addElement("4. Giocatore4 - Punti: 7800 - Lvl 8");
        rankingModel.addElement("5. Giocatore5 - Punti: 7200 - Lvl 7");
    }

    /**
     * Configura il renderer personalizzato per la lista.
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

    // ============== LAYOUT ==============
    
    /**
     * Configura il layout della finestra:
     * - Titolo in alto
     * - Lista classifica al centro
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            UI_Config.BUTTON_INSETS.top,
            UI_Config.BUTTON_INSETS.left,
            UI_Config.BUTTON_INSETS.bottom,
            UI_Config.BUTTON_INSETS.right
        ));

        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(rankingList);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UI_Config.BACKGROUND_COLOR);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    // ============== STILE COMPONENTI ==============
    
    /**
     * Crea bordo per la lista classifica.
     */
    private Border createListBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),
            BorderFactory.createEmptyBorder(
                UI_Config.BUTTON_INSETS.top,
                UI_Config.BUTTON_INSETS.left,
                UI_Config.BUTTON_INSETS.bottom,
                UI_Config.BUTTON_INSETS.right
            )
        );
    }

    // ============== METODI OVERRIDE ==============
    
    @Override
    protected String getWindowTitle() {
        return "Classifica - PoggioAdventure";
    }

    // ============== MAIN PER TEST ==============
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