package cpusim.gui.help;

import javafx.scene.control.TextField;
import java.util.ArrayList;
import java.util.List;
import cpusim.gui.GeneralSuggestionManager;

/**
 * Author: Matthew Levine
 * Date: 11/16/13
 */
public class HelpMenuTabCompletion extends GeneralSuggestionManager{


    public HelpMenuTabCompletion(TextField textField){
        initUtilityFields(textField);
    }

    public List getData(){
        //data 
        ArrayList<String> list = new ArrayList<String>();
        list.add("page1");
        list.add("page2");
        list.add("page3");
        return list;

    }

}
