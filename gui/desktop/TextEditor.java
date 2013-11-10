/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpusim.gui.desktop;

import com.sun.javafx.scene.web.skin.HTMLEditorSkin;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

/**
 *
 * @author Ben Borchard
 */
public class TextEditor extends HTMLEditor {
    
    private static ArrayList<EventHandler> handlers;
    
    public TextEditor(){
        this("");
    }
    
    public TextEditor(String text){
        
        super();
        
        handlers = new ArrayList<>();
        
        setHtmlText(text);
        
        ObservableList children = getChildren();
        
        ObservableList toRemove = FXCollections.observableArrayList();
        
        ObservableList children2 = ((HTMLEditorSkin)children.get(0)).getChildren();
        
        ObservableList children3 = ((GridPane)children2.get(0)).getChildren();
        
        ObservableList children4 = ((ToolBar)children3.get(0)).getItems();
        
        handlers.add(((Button)children4.get(0)).getOnAction());
        handlers.add(((Button)children4.get(1)).getOnAction());
        handlers.add(((Button)children4.get(2)).getOnAction());
                
        for (Object tool : children3){
            toRemove.add(tool);
        }
        
        for (Object tool : toRemove){
            if (tool instanceof ToolBar){
                children3.remove(tool);
            }
        }
    }
    
    public static ArrayList<EventHandler> getHandlers(){
        return handlers;
    }

}
