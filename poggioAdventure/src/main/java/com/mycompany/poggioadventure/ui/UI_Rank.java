package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Classe che rappresenta l'interfaccia utente per visualizzare la classifica del gioco.
 * Mostra una lista di giocatori con i loro punteggi e livelli.
 * Estende la classe astratta UI_Abstract per ereditare la struttura di base dell'interfaccia grafica.
 */
public class UI_Rank extends UI_Abstract {

    // Componenti dell'interfaccia utente
    private JList<String> rankingList;  // Lista per visualizzare la classifica
    private JLabel titleLabel;         // Etichetta per il titolo della finestra

    // Modello per la lista della classifica (dati fittizi)
    private DefaultListModel<String> rankingModel;

    /**
     * Costruttore della classe. Chiama il costruttore della superclasse UI_Abstract
     * per inizializzare l'interfaccia grafica.
     */
    public UI_Rank() {
        super();
    }

    /**
     * Implementazione del metodo astratto initComponents() della superclasse UI_Abstract.
     * Inizializza tutti i componenti dell'interfaccia utente, inclusi la lista della classifica
     * e il titolo.
     */
    @Override
    protected void initComponents() {
        configureFrame();  // Configura la finestra principale
        createComponents();  // Crea i componenti dell'interfaccia
        setupLayout();  // Configura il layout della finestra
        pack();  // Ridimensiona la finestra per adattarsi ai componenti
    }

    /**
     * Configura la finestra principale con le impostazioni di base:
     * - Dimensioni preferite
     * - Comportamento alla chiusura
     * - Colore di sfondo
     * - Layout principale
     */
    private void configureFrame() {
        setPreferredSize(calculateWindowSize());  // Imposta le dimensioni della finestra
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Chiude solo questa finestra
        getContentPane().setBackground(UI_Config.BACKGROUND_COLOR);  // Colore di sfondo
        getContentPane().setLayout(new BorderLayout());  // Layout principale
    }

    /**
     * Calcola le dimensioni della finestra in base alle dimensioni dello schermo
     * e ai rapporti definiti in UI_Config.
     *
     * @return Dimension Oggetto Dimension che rappresenta le dimensioni della finestra
     */
    private Dimension calculateWindowSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();  // Ottiene le dimensioni dello schermo
        return new Dimension(
            (int)(screenSize.width * UI_Config.WINDOW_WIDTH_RATIO),  // Larghezza in base al rapporto
            (int)(screenSize.height * UI_Config.WINDOW_HEIGHT_RATIO)  // Altezza in base al rapporto
        );
    }

    /**
     * Crea i componenti dell'interfaccia utente, inclusi il titolo e la lista della classifica.
     */
    private void createComponents() {
        // Titolo
        titleLabel = new JLabel("CLASSIFICA");
        titleLabel.setFont(UI_Config.getBoldFont().deriveFont(
            getPreferredSize().height * UI_Config.TITLE_FONT_RATIO));  // Scala il font
        titleLabel.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);  // Allinea il testo al centro

        // Inizializza il modello della lista con dati fittizi
        rankingModel = new DefaultListModel<>();
        rankingModel.addElement("1. Giocatore1 - Punti: 9500 - Lvl 10");
        rankingModel.addElement("2. Giocatore2 - Punti: 8700 - Lvl 9");
        rankingModel.addElement("3. Giocatore3 - Punti: 8200 - Lvl 8");
        rankingModel.addElement("4. Giocatore4 - Punti: 7800 - Lvl 8");
        rankingModel.addElement("5. Giocatore5 - Punti: 7200 - Lvl 7");

        // Crea la JList e associa il modello
        rankingList = new JList<>(rankingModel);
        rankingList.setFont(UI_Config.getNormalFont().deriveFont(
            getPreferredSize().height * UI_Config.BUTTON_FONT_RATIO));  // Scala il font
        rankingList.setBackground(UI_Config.BUTTON_BASE_COLOR);  // Colore di sfondo
        rankingList.setSelectionBackground(UI_Config.BUTTON_HOVER_COLOR);  // Colore di selezione
        rankingList.setBorder(createListBorder());  // Bordo personalizzato
        rankingList.setForeground(UI_Config.TEXT_COLOR);  // Colore del testo

        // Personalizzazione del renderer per allineare il testo e aggiungere spazio
        rankingList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                         boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(SwingConstants.CENTER);  // Allinea il testo al centro
                label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));  // Aggiunge spazio sopra e sotto
                return label;
            }
        });
    }

    /**
     * Configura il layout della finestra, posizionando i componenti nei pannelli appropriati.
     */
    private void setupLayout() {
        JPanel mainPanel = new JPanel(new BorderLayout());  // Pannello principale
        mainPanel.setOpaque(false);  // Rende il pannello trasparente
        mainPanel.setBorder(BorderFactory.createEmptyBorder(
            UI_Config.BUTTON_INSETS.top,
            UI_Config.BUTTON_INSETS.left,
            UI_Config.BUTTON_INSETS.bottom,
            UI_Config.BUTTON_INSETS.right
        ));

        // Aggiunge il titolo al pannello
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // ScrollPane per la lista
        JScrollPane scrollPane = new JScrollPane(rankingList);
        scrollPane.setBorder(null);  // Rimuove il bordo predefinito
        scrollPane.getViewport().setBackground(UI_Config.BACKGROUND_COLOR);  // Colore di sfondo
        mainPanel.add(scrollPane, BorderLayout.CENTER);  // Aggiunge la lista al pannello

        add(mainPanel, BorderLayout.CENTER);  // Aggiunge il pannello principale alla finestra
    }

    /**
     * Crea un bordo personalizzato per la lista della classifica.
     *
     * @return Border Bordo configurato
     */
    private Border createListBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI_Config.BORDER_COLOR, 2),  // Bordo esterno
            BorderFactory.createEmptyBorder(
                UI_Config.BUTTON_INSETS.top,
                UI_Config.BUTTON_INSETS.left,
                UI_Config.BUTTON_INSETS.bottom,
                UI_Config.BUTTON_INSETS.right
            )  // Padding interno
        );
    }

    /**
     * Metodo main di esempio per testare l'interfaccia.
     * Da rimuovere in produzione o utilizzare solo per scopi dimostrativi.
     */
    public static void main(String[] args) {
        try {
            FlatLightLaf.setup();  // Configura il tema FlatLaf light
            EventQueue.invokeLater(() -> {
                UI_Rank ranking = new UI_Rank();  // Crea la finestra della classifica
                ranking.setVisible(true);  // Rende la finestra visibile
            });
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                "Errore nell'apertura della classifica: " + ex.getMessage(),
                "Errore",
                JOptionPane.ERROR_MESSAGE);  // Mostra un messaggio di errore
            System.exit(1);  // Termina l'applicazione
        }
    }

    /**
     * Implementazione del metodo astratto getWindowTitle() della superclasse UI_Abstract.
     * Restituisce il titolo della finestra.
     *
     * @return Stringa contenente il titolo della finestra
     */
    @Override
    protected String getWindowTitle() {
        return "Classifica - PoggioAdventure";
    }
}