package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.InputHandler;
import javax.swing.JTextField;

/**
 *
 * @author Strix89
 */
public class GUIInputHandler implements InputHandler {
    private final JTextField commandInputField;

    public GUIInputHandler(JTextField commandInputField) {
        this.commandInputField = commandInputField;
    }

    @Override
    public String getInput() {
        return commandInputField.getText().trim();
    }
}
