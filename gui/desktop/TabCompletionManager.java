package cpusim.gui.desktop;

import cpusim.Mediator;
import cpusim.gui.GeneralSuggestionManager;
import javafx.scene.control.TextArea;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Matthew Levine
 * Date: 11/16/13
 */
public class TabCompletionManager extends GeneralSuggestionManager {

    private Mediator source;

    public TabCompletionManager( TextArea textarea, Mediator mediator ){
        initUtilityFields(textarea);
        source = mediator;
    }

    public List getData(){
        return source.getMachine().getInstructions();
    }

}
