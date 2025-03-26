package di.uniba.map.b.adventure.impl;

import di.uniba.map.b.adventure.ErrorHandler;
import java.util.logging.Logger;

/**
 *
 * @author Strix89
 */
public class CLIErrorHandler implements ErrorHandler {
    private static final Logger LOGGER = Logger.getLogger(CLIErrorHandler.class.getName());
    @Override
    public void handleFatalError(String message, Throwable ex) {
        LOGGER.log(java.util.logging.Level.SEVERE, message, ex);
    }

    @Override
    public void handleRecoverableError(String message) {
        LOGGER.warning(message);
    }
}
