package cpusim.gui.desktop;

import cpusim.Mediator;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

/**
 * Author: Matt
 * This class manages text highlighting for keywords.
 * Date: 11/15/13
 */
public class TextHighlightController {

    /** List of highlights **/
    private ArrayList<Rectangle> highlights;
    /** TextArea to modify **/
    private TextArea text;
    /**Source to Grab List From**/
    Mediator host;

    /** Lists of exceptional keys that need to be handled separately **/
    private final ArrayList<String> replacementKeys = new ArrayList<String>(){{add("BACK_SPACE");
        add("DELETE");add(" ");}};
    private final ArrayList<String> ignoreKeys = new ArrayList<String>(){{add("SHIFT");
        add("HOME"); add("END"); add("PAGE_UP"); add("PAGE_DOWN"); add("LEFT");
        add("RIGHT"); add("UP"); add("DOWN"); }};

    /**
     * Constructs a new TextHighlightController
     * @param textArea The text region to enable highlighting on
     * @param source The object from which keywords are grabbed
     */
    public TextHighlightController( TextArea textArea, Mediator source ){
        text = textArea;
        host = source;
        highlights = new ArrayList<Rectangle>();

        //This is a courtesy, it does almost nothing (at construction, the thing is
        // nearly guaranteed to have the default font).
        //If you start messing with fonts, this system will (probably) fail
        if ( !text.getStyle().equals("") ){
            throw new RuntimeException("Text Highlighter Assumes Default Settings");
        }
    }

    /** Returns true if the given string is a keyword.
     * @param string the String to validate
     * @return The boolean indicating keywordiness
     */
    public boolean isKeyword( String string ){
        //cast to strings, possibly redundant so could improve in that case
        ArrayList<String> stringKeyWords = new ArrayList<String>();
        for ( Object key : host.getMachine().getInstructions() ){
            stringKeyWords.add(key.toString());
        }
        return stringKeyWords.contains(string);

    }

    /**
     * Enables highlighting for the text region.
     */
    public void enableHighlighting(){
        text.addEventFilter(KeyEvent.ANY, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent car) {
                if ( isKeyIgnorable(car) )  return;
                clearSelection(car); //we overwrite this feature, so we need to add it back
                updateHighlights();

                //highlighting doesn't currently work with scrolling
                if ( text.getScrollLeft() > 0){
                    removeAllHighlights();
                }
            }
        });
        text.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                enableMouseClicks(mouseEvent);
            }
        });

    }

    /** Enables refreshing of highlights on mouse click
     * @param me The mouse event to signal refreshing
     */
    public void enableMouseClicks( MouseEvent me ){
        updateHighlights();
    }

    /** Returns all text in the textarea
     * @return A string list of all lines of text in the textarea
     */
    private String[] getAllText( ){
        int savePos = text.getCaretPosition();
        text.end();
        text.selectHome();
        String selection = text.getSelectedText();
        String[] lines = selection.split("\n");
        text.positionCaret(savePos);
        return lines;
    }

    /**
     * Updates all highlights on the textarea.
     */
    private void updateHighlights(){
        removeAllHighlights();
        String[] text = getAllText();
        int row = -1; //-1 to make math slightly nicer
        int literalCol = 0;
        int caretCol;

        for ( String line : text ){
            row += 1;
            String[] wordArray = line.split(" ");

            for ( String word : wordArray ){
                if ( word.contains( "\t" ) ){
                    detab();
                    return;
                }
                //if it's a keyword, get the screen coords and highlight; could make the
                // map text coords->screen coords vary by font, but hardcoded for now
                if ( isKeyword(word) ){
                    caretCol = line.indexOf(word,literalCol);
                    addHighlight((int)(0.9*((caretCol+1)*8.0+22.5)),
                            (int)(row*13.5),word.length());
                }
                literalCol += word.length();
            }
            literalCol = 0;
        }
    }

    /**
     *  Adds a highlight to the textarea.
     *  @param xloc the horizontal position of the highlight
     *  @pararm yloc the veritical position of the highlight
     *  @param width the width of the highlight
     */
    public void addHighlight( int xloc, int yloc, int width ){
        Rectangle highlight = new Rectangle(0,0,width*8+6,15);
        highlight.setTranslateX(xloc+5);
        highlight.setTranslateY(-310 + yloc);
        highlight.setFill(Paint.valueOf("blue"));
        highlight.setBlendMode(BlendMode.BLUE);
        ((GridPane) text.getParent()).add(highlight, 0, 0);
        highlights.add(highlight);
    }

    /**
     * Removes a higlight stored at index location as given
     * @param index The index location of the highlight to remove
     */
    public void removeHighlight( int index ){
        if ( index < highlights.size() ){
            Rectangle rect = highlights.remove(index);
            ((GridPane) text.getParent()).getChildren().remove(rect);
        }
    }

    /**
     * Removes all highlights
     */
    public void removeAllHighlights( ){
        for ( int i = 0; i < highlights.size(); i++ ){
            removeHighlight(i);
        }
    }

    /** Removes the current selection if the keyword is appropriate
     *  @param  car The keyevent to signal deletion
     **/
    public void clearSelection(KeyEvent car){
        if ( car.getCode().isLetterKey() || car.getCode().isDigitKey() ||
                replacementKeys.contains(car.getCode().toString())){
            if ( !text.getSelectedText().equals(""))
                text.replaceSelection("");
        }
    }

    /** Returns whether a key should ignore highlighting refresh or not
     *  @param car The key to test for ignorability
     **/
    public boolean isKeyIgnorable( KeyEvent car){
        return ignoreKeys.contains(car.getCode().toString());
    }

    /** Replaces all tabs in the code with equivalent number of spaces, redraws text. **/
    public void detab(){
        //This method isn't particularly efficient, but we don't expect it
        //to be called frequently either
        removeAllHighlights(); //dump the highlights
        //get variables for resetting text
        String[] totalText = getAllText();
        String newTotalText = "";
        String newLine;
        String tabMax = "                 ";//maximum size of a tab
        int sizeOfTab;
        for ( String line : totalText ){
            newLine = "";
            for ( String word : line.split(" ") ){
                //get how big our tab should be by subtracting it's length
                //without the tab from it's length with the tab
                sizeOfTab = word.length() - word.replace("\t","").length();
                //replace all tabs with a substring of the maxtab of appropriate size
                newLine += word.replace("\t", tabMax.substring(sizeOfTab))+ " ";
            }
            newTotalText += newLine+"\n";
        }
        text.setText(newTotalText); //update the text
        updateHighlights(); //add highlights back in
    }


}
