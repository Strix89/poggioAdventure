/**
 * Classe che rappresenta un'interfaccia grafica (GUI) per visualizzare l'inventario di un gioco.
 * Estende JFrame e utilizza componenti Swing per la renderizzazione.
 * 
 * Caratteristiche principali:
 * - Mostra una lista scrollabile di oggetti (a sinistra).
 * - Visualizza dettagli come immagine e descrizione quando un oggetto viene selezionato (a destra).
 * - Supporta personalizzazione del font e dello stile grafico (tema FlatLaf).
 * - Pulsante "ESCI" per terminare l'applicazione (da valutare se adeguato al contesto).
 * 
 * Struttura:
 * - Utilizza un layout BorderLayout diviso in due sezioni principali:
 *   1. Pannello sinistro: Lista degli oggetti dell'inventario con scroll.
 *   2. Pannello destro: 
 *      - Area per l'immagine dell'oggetto selezionato.
 *      - Area di testo per la descrizione dettagliata.
 *      - Pulsante "ESCI".
 * 
 * Funzionalità chiave:
 * - Metodo addObjectsToScroller(): Popola dinamicamente la lista degli oggetti.
 * - Gestione degli eventi: Click sugli oggetti per visualizzarne i dettagli.
 * - Validazione del font tramite setFontName().
 * 
 * Note importanti:
 * - Il costruttore UI_Inventory(String name) imposta solo il nome della finestra ma NON inizializza i componenti.
 *   Potrebbe essere un bug poiché initComponents() non viene chiamato.
 * - Il pulsante "ESCI" utilizza System.exit(0), che termina l'intera applicazione. Potrebbe non essere appropriato 
 *   se questa finestra è parte di un'applicazione più complessa.
 * - Il font personalizzato (nameFont) non è applicato a tutti i componenti (es. i componenti usano "Segoe UI" in initComponents()).
 * - Utilizza FlatLightLaf per il tema grafico, ma questo viene impostato solo nel main() (non nel costruttore).
 * 
 * Esempio d'uso:
 * UI_Inventory inventory = new UI_Inventory();
 * inventory.addObjectsToScroller(listaOggetti);
 * inventory.setVisible(true);
 * 
 * @author Strix89
 * @version 1.0
 */
package com.mycompany.poggioadventure.ui;

import com.formdev.flatlaf.FlatLightLaf;
import di.uniba.map.b.adventure.type.AdvObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class UI_Inventory extends JFrame {
    // Componenti UI
    private JScrollPane objectsScroller;  // Pannello scrollabile per la lista oggetti
    private JTextArea descriptionArea;    // Area di testo per la descrizione
    private JButton escButton;            // Pulsante per uscire dall'applicazione
    private JPanel imageObjects;          // Pannello per visualizzare l'immagine
    
    // Configurazioni
    private String nameWindow = "INVENTARIO";  // Nome default della finestra
    private String nameFont = "Crismon Pro";   // Font personalizzabile (non completamente implementato)

    /**
     * Costruttore principale. Inizializza i componenti dell'interfaccia.
     */
    public UI_Inventory() {
        initComponents(); // Inizializzazione degli elementi grafici
    }
    
    /**
     * Costruttore alternativo che permette di impostare un nome personalizzato per la finestra.
     * Attenzione: Non chiama initComponents(), lasciando la finestra non inizializzata.
     * @param name Nome da assegnare alla finestra.
     */
    public UI_Inventory(String name) {
        nameWindow = name; // Potrebbe richiedere una chiamata a initComponents()
    }
    
    /**
     * Imposta il font globale dell'interfaccia. Esegue controlli di validità.
     * @param fontName Nome del font da utilizzare.
     * @throws IllegalArgumentException Se il font non è valido o non trovato.
     */
    public void setFontName(String fontName) {
        if (fontName == null || fontName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome del font nullo o vuoto");
        }
        Font testFont = new Font(fontName, Font.PLAIN, 12);
        String actualFontName = testFont.getFamily();
        if (!actualFontName.equalsIgnoreCase(fontName)) {
            throw new IllegalArgumentException("Font non valido: " + fontName);
        }
        this.nameFont = fontName;
    }

    /**
     * Getter del nome del Font.
     * @return String nome del font
     */
    public String getFontName() {
        return nameFont;
    }

    /**
     * Inizializza tutti i componenti grafici e configura il layout.
     * Metodo privato chiamato dal costruttore principale.
     */
    private void initComponents() {
        // Configurazione generale della finestra
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(nameWindow);
        setSize(650, 480);
        setResizable(false);
        getContentPane().setBackground(new Color(45, 45, 45)); // Sfondo scuro
        // Layout principale con margini
        setLayout(new BorderLayout(15, 15));
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        // 1. PANNELLO SINISTRA - LISTA OGGETTI
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBackground(new Color(70, 70, 70));
        listPanel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        JLabel listTitle = new JLabel(nameWindow);
        listTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        listTitle.setForeground(Color.WHITE);
        listTitle.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        listPanel.add(listTitle, BorderLayout.NORTH);
        objectsScroller = new JScrollPane();
        objectsScroller.setBorder(null);
        objectsScroller.getViewport().setBackground(new Color(90, 90, 90));
        listPanel.add(objectsScroller, BorderLayout.CENTER);
        add(listPanel, BorderLayout.WEST);
        // 2. PANNELLO DESTRA - IMMAGINE E DESCRIZIONE
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(45, 45, 45));
        GridBagConstraints gbc = new GridBagConstraints();
        // 2a. PANNELLO IMMAGINE
        imageObjects = new JPanel(new BorderLayout());
        imageObjects.setBackground(new Color(30, 30, 30));
        imageObjects.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        imageObjects.setPreferredSize(new Dimension(300, 250));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.6;
        gbc.fill = GridBagConstraints.BOTH;
        rightPanel.add(imageObjects, gbc);
        // 2b. PANNELLO DESCRIZIONE
        JPanel descPanel = new JPanel(new BorderLayout());
        descPanel.setBackground(new Color(30, 30, 30));
        descPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        descriptionArea = new JTextArea();
        descriptionArea.setBackground(new Color(60, 60, 60));
        descriptionArea.setForeground(Color.WHITE);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setBorder(null);
        descPanel.add(descScroll, BorderLayout.CENTER);
        gbc.gridy = 1;
        gbc.weighty = 0.4;
        rightPanel.add(descPanel, gbc);
        // 2c. PULSANTE ESCI
        escButton = new JButton("ESCI");
        escButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        escButton.setBackground(new Color(100, 100, 100));
        escButton.setForeground(Color.WHITE);
        escButton.setFocusPainted(false);
        escButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        escButton.addActionListener(e -> System.exit(0));
        gbc.gridy = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 0, 0);
        rightPanel.add(escButton, gbc);
        add(rightPanel, BorderLayout.CENTER);
    }

    /**
     * Popola la lista degli oggetti nell'area scrollabile.
     * Ogni oggetto è rappresentato da una JLabel cliccabile.
     * @param objects Lista di AdvObject da visualizzare nell'inventario.
     */
    public void addObjectsToScroller(java.util.List<AdvObject> objects) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(90, 90, 90));
        for (AdvObject obj : objects) {
            JLabel label = new JLabel(" • " + obj.getName());
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            label.setForeground(Color.WHITE);
            label.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
            label.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    showObjectDetails(obj);
                }
            });
            panel.add(label);
        }
        objectsScroller.setViewportView(panel);
    }

    /**
     * Mostra i dettagli di un oggetto selezionato (immagine e descrizione).
     * @param obj L'oggetto di cui visualizzare i dettagli.
     */
    private void showObjectDetails(AdvObject obj) {
        imageObjects.removeAll();
        imageObjects.setLayout(new BorderLayout());
        if (obj.getImagePath() != null && obj.getImagePath().exists()) {
            ImageIcon icon = new ImageIcon(obj.getImagePath().getPath());
            Image image = icon.getImage().getScaledInstance(280, 200, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(image));
            imageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageObjects.add(imageLabel, BorderLayout.CENTER);
        } else {
            JLabel noImageLabel = new JLabel("Nessuna immagine disponibile");
            noImageLabel.setForeground(Color.WHITE);
            noImageLabel.setHorizontalAlignment(JLabel.CENTER);
            imageObjects.add(noImageLabel, BorderLayout.CENTER);
        }
        descriptionArea.setText(obj.getDescription());
        descriptionArea.setCaretPosition(0);
        revalidate();
        repaint();
    }

    /**
     * Metodo main di esempio per testare l'interfaccia.
     * Da rimuovere in produzione o utilizzare solo per scopi dimostrativi.
     */
    public static void main(String[] args) {
        FlatLightLaf.setup();
        java.awt.EventQueue.invokeLater(() -> {
            UI_Inventory inventoryUI = new UI_Inventory();
            inventoryUI.setVisible(true);
            // Esempio di dati (modifica con i tuoi percorsi reali)
            java.util.List<AdvObject> objects = new ArrayList<>();
            for(int i = 1; i <= 26; i++) {
                objects.add(new AdvObject(
                    i,
                    "Oggetto " + i,
                    "./resources/img/none.png",
                    "Descrizione dettagliata per l'oggetto " + i + ".\n\n" +
                    "Materiale: Speciale\nPeso: " + (i % 10 + 1) + "kg\nRarità: " + (i % 5 + 1) + "/5"
                ));
            }
            inventoryUI.addObjectsToScroller(objects);
        });
    }
}