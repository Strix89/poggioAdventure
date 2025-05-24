package com.mycompany.poggioadventure.core;

import java.util.List;
import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.core.utils.StopWatch;
import com.mycompany.poggioadventure.ui.ErrorHandler;
import com.mycompany.poggioadventure.ui.OutputHandler;

/**
 * Context object che incapsula le informazioni necessarie agli observer
 * 
 * @author pierpaolo & Strix89
 */
public class GameContext {
    private final GameDescription gameDescription;
    private final OutputHandler outputHandler;
    private final ErrorHandler errorHandler;
    private final List<String> templog;
    private final StopWatch stopWatch;
    
    public GameContext(GameDescription gameDescription, OutputHandler outputHandler, 
                      ErrorHandler errorHandler, List<String> templog, StopWatch stopWatch) {
        this.gameDescription = gameDescription;
        this.outputHandler = outputHandler;
        this.errorHandler = errorHandler;
        this.templog = templog;
        this.stopWatch = stopWatch;
    }
    
    // Getters
    public GameDescription getGameDescription() { return gameDescription; }
    public OutputHandler getOutputHandler() { return outputHandler; }
    public ErrorHandler getErrorHandler() { return errorHandler; }
    public List<String> getTemplog() { return templog; }
    public StopWatch getStopWatch() { return stopWatch; }
}