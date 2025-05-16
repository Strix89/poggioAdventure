package com.mycompany.poggioadventure.observers;

import com.mycompany.poggioadventure.core.abstracts.GameDescription;
import com.mycompany.poggioadventure.parser.ParserOutput;

/**
 *
 * @author pierpaolo
 */
public interface GameObserver { 

    /**
     *
     * @param description
     * @param parserOutput
     * @return
     */
    public String update(GameDescription description, ParserOutput parserOutput);

}
