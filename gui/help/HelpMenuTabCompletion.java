package cpusim.gui.help;

import javafx.scene.control.TextField;
import java.util.ArrayList;
import java.util.List;
import cpusim.gui.help.HelpController;
import cpusim.gui.GeneralSuggestionManager;

/**
 * Author: Matthew Levine & Bilal Ahmad
 * Date: 11/16/13
 */
public class HelpMenuTabCompletion extends GeneralSuggestionManager{


    public HelpMenuTabCompletion(TextField textField){
        initUtilityFields(textField);
    }

    public List getData(){
        //data 
        ArrayList<String> list = new ArrayList<String>();

        for (int i=1; i<HelpController.nameURLPairs.length; i++){
            list.add(HelpController.nameURLPairs[i][0].toLowerCase());
        }

        return list;

    }

}
