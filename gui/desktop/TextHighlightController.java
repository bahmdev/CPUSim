package cpusim.gui.desktop;

import cpusim.Mediator;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

/**
 * Author: Matt
 * Date: 11/15/13
 */
public class TextHighlightController {

    /** List of highlights **/
    private ArrayList<Rectangle> highlights;
    /** TextArea to modify **/
    private TextArea text;
    /**Source to Grab List From**/
    Mediator host;


    /**
     * Constructs a new TextHighlightController
     * @param textArea The text region to enable highlighting on
     * @param source The object from which keywords are grabbed
     */
    public TextHighlightController( TextArea textArea, Mediator source ){
        text = textArea;
        host = source;
        highlights = new ArrayList<Rectangle>();

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
                String key = car.getCode().toString();
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
                enableMouseClicks( mouseEvent );
            }
        });

    }

    public void enableMouseClicks( MouseEvent me ){
        updateHighlights();
    }

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
     * Updates all highlights on the textarae.
     */
    private void updateHighlights(){
        removeAllHighlights();
        String[] text = getAllText();
        int row = -1;
        int count = 0;
        int col;
        for ( String line : text ){
            row += 1;
            String[] wordArray = line.split(" ");
            for ( String word : wordArray ){
                if ( isKeyword(word) ){
                    col = line.indexOf(word,count);
                    System.out.println(count);
                    addHighlight((int)((col+1)*7.5+22.5),(int)(row*13.5),word.length());
                }
                count += word.length();
            }
            count = 0;
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
        highlight.setTranslateX(20+xloc-width*3.5);
        highlight.setTranslateY(-310 + yloc);
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
            boolean x = ((GridPane) text.getParent()).getChildren().remove(rect);
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


}
