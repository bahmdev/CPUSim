package cpusim.gui.desktop;

import cpusim.Mediator;
import cpusim.gui.desktop.editorpane.EditorPaneController;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

/**
 * Author: Matt
 * Date: 11/19/13
 * This class is designed to...
 */
public class PopupManager {

    TextArea text;
    Stage textWin;

    /** Constructs a new popup manager and initializes the duplicate text frame
     *
     * @param textArea
     * @param stage
     * @param controller
     * @param mediator
     */
    public  PopupManager(TextArea textArea, Stage stage,
                         EditorPaneController controller, Mediator mediator ){
        //duplicate text as not to disturb concurrent modification
        text = new TextArea(textArea.getText());
        textWin = buildWindow(text,controller,mediator,stage);
        allowPopupsControlEscape(stage);
    }

    /**
     * Builds the window and returns it
     * @param text The text to display in the window
     * @param controller The controller to grab the textarea from
     * @param mediator the mediator containing keyword information
     * @param stage the original stage location of the text
     * @return The newly constructred stage
     */
    private Stage buildWindow(TextArea text,EditorPaneController controller,
                              Mediator mediator, Stage stage ){
        //build the stage, add the grid and text
        Stage textWin = new Stage();
        GridPane gpane = new GridPane();
        gpane.getChildren().add(text);
        textWin.setScene(new Scene(gpane));

        //fix dimensions
        fixGridDimensionsToStage(gpane, textWin, stage);

        //Add cool text enhancements
        //new TabCompletionManager(text,
        //        mediator).enableTextProcessing();


        //show
        return textWin;
    }

    /**
     * Fixes grid dimensions to an assumed parent stage
     * @param gridpane The grid pane to change size of
     * @param textWin the stage to get size of
     * @param stage The original stage from which to grab information
     */
    private void fixGridDimensionsToStage(GridPane gridpane, Stage textWin, Stage stage){
        //this is a long block of configuration code, but not too much way around it
        textWin.setHeight(stage.getHeight()/1.3);
        double minWidth = stage.getWidth()/3 < 466 ? 466 : stage.getWidth()/3;
        textWin.setWidth(minWidth);
        ColumnConstraints columnPercent = new ColumnConstraints();
        columnPercent.setPercentWidth(100);
        RowConstraints rowPercent = new RowConstraints();
        rowPercent.setPercentHeight(100);
        gridpane.getColumnConstraints().add(columnPercent);
        gridpane.getRowConstraints().add(rowPercent);
        textWin.setOpacity(1);
    }

    private void configureBindings(){
        text.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent car) {
                if (car.isAltDown() ){
                    if (car.getCode().toString().equals("MINUS")  && textWin.getOpacity() > .2 )
                        textWin.setOpacity(textWin.getOpacity()-.1);
                    else if (car.getCode().toString().equals("EQUALS") && textWin.getOpacity() <= .9)
                        textWin.setOpacity(textWin.getOpacity()+.1);
                }
            }
        });
    }

    /** Allows tab creations by cntrl-esc
     * @param stage The stage to add a binding to
     */
    private void allowPopupsControlEscape(Stage stage){
        stage.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent car) {
                if (car.getCode().toString().equals("K") &&
                        car.isControlDown() && car.isShiftDown()) {
                    show();
                }
            }
        });
    }

    public void show(){
        textWin.show();
        textWin.setTitle("Duplicate Code Window");
        configureBindings();
    }

    /** Sets the title of the window
     * @param title the title of the window
     */
    public void title( SimpleStringProperty title ){
        textWin.setTitle(title.get());
    }
}

