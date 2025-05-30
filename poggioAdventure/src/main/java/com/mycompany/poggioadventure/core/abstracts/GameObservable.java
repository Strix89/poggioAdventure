package com.mycompany.poggioadventure.core.abstracts;

import com.mycompany.poggioadventure.core.utils.GameContext;
import com.mycompany.poggioadventure.observers.GameObserver;
import com.mycompany.poggioadventure.parser.ParserOutput;

/**
 *
 * @author pierpaolo
 */
public interface GameObservable {
    
    /**
     *
     * @param o
     */
    public void attach(GameObserver o);
    
    /**
     *
     * @param o
     */
    public void detach(GameObserver o);
    
    /**
     *
     * @param output */
    public void notifyObservers(ParserOutput parserOutput, GameContext gameContext);
    
}
