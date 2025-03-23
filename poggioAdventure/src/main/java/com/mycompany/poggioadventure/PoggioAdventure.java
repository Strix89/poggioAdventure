package com.mycompany.poggioadventure;
import com.mycompany.poggioadventure.ui.UI_Init;

/**
 *
 * @author Strix89
 */
public class PoggioAdventure {

    public static void main(String[] args) {
         // Avvia la GUI principale
        java.awt.EventQueue.invokeLater(() -> {
            // Crea l'istanza di UI_Init che è la finestra principale
            UI_Init uiInit = new UI_Init();
            uiInit.setVisible(true);  // Rende la finestra visibile
        });
    }
}
