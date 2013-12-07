package cpusim.gui.desktop;

import cpusim.Mediator;
import cpusim.gui.TextAreaEnhancer;
import cpusim.gui.TextEnhancement;
import cpusim.gui.help.Logs.Log;

import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Collection;

/*Things possibly accounted for when highlighting
 * font size
 * font color
 * background color
 * x scroll
 * y scroll
 * change in machine
 * debug mode
 * line numbers
 * resized text area
 * resized GUI window
 * change in comment symbol
 * combination of comment and word
 * scrollbars get highlihted by comment
 * highlights show over line numbers/debug boxes etc.
 * scrolling under Lucida Console isn't right.
 * backspacing doesn't adjust scroll
 * maximizing doesn't updates scroll

*/

/* Known Problems (?):
 * really big file slow it down (who knows if this is true)
 */

/* This class is implemented by calibrating hard-values for default conditions and then
  * scaling by the appropriate factors for change. A lot of numerical parameters therefore
  * have the form "Po + a * P / Po" if Po is an initial integer value or the form
  * "Po + (P-Po) * a" if Po is a float. In the former case, P/Po will return 0 if
  * Po > P and otherwise 1 provided that the condition P >> Po fails. In both cases,
  * it is clear that when P == Po the base condition is preserved. Although it will
  * be specified in the external documentation preconditions on a per method basis,
  * we sometimes have to assume !(P >> Po) which might be limiting in terms
  * of extensibility but requisite under the current Java FX limitations. Scalars
  * written above as "a" are sometimes empirically calculated, which leaves
   * room for improvement.
 */

 /* Various color optimizations are done throughout the program. Some of them
  * might appear to be pointless... well, they might be, but they're all done
  * for a reason. That reason usually can be simplified to not wanting colors
  * to overrun each other creating unsightly and oddly colored boxes, and not
  * wanting colors that are chosen for you to make anything invisible. We are
  * restrained by not being able to change any of the choices already available.
  * Thus, we restrict things that were previously unavailable to the user. One
  * of the these is scrollbar color, which might seem pointless. We pick a
  * color that has red/green bands equal to the background color and a blue
  * band that is guaranteed not to be the same as the background color. This
  * ensures that it is always visible, but never overwritten by comment
  * highlights. More justification is given in-text. We also pick highlight
  * colors and keyword colors for the user and optimize visibility by contrast.
  * The current model CANNOT work if these decisions are given to the user.
  * It is possible to make highlight colors seem similar to regular text
  * color - mainly as the text color approaches the background color on
  * primary bands. It isn't surprising that when you make your text harder to
  * see, you make your highlights harder to see too, and this is not considered
  * a bug.
  */

 /* The implementation of this class lends some explanation to its name. Although
  * ostensibly we are not "highlighting" text, that is in fact what we are doing,
  * so we prefer tht name over a name light "font color changer" or the like.
  */

/**
 * Author: Matt
 * This class manages text highlighting for keywords and comments. It is initialized
 * about a Java FX TextArea and assumes that it can use information from CPUSim
 * standard classes as required by the constructor. If this precondition fails,
 * then the class may not operate correctly. If additional powers are given to
 * the user well beyond the scope of powers intended by the Controller, then the
 * Controller may not operate correctly. Once highlighting is enabled for a
 * TextArea, it cannot be disabled. If the references to critical external
 * sources as specified in the constructor are modified in an unexpected way -
 * e.g, all data from the host is set to null - then the Controller may throw
 * exceptions.
 * Date: 11/24/13
 */
public class TextHighlightController implements TextEnhancement{

    /** List of highlights **/
    private RectangleBank highlights;
    /** TextArea to modify **/
    private TextArea text;
    /**Source to Grab List From**/
    Mediator host;
    /** Color of keyword text **/ // stored for optimization
    BlendMode keywordColor;
    /** Color of comment text **/ //Stored for optimization
    BlendMode commentColor;
    /** Color of label text **/ //Stored for optimization
    BlendMode labelColor;
    /**Whether the enhancement is enabled**/
    boolean enableFlag;

    /**
     * Constructs a new TextHighlightController
     * @param textArea The text region to enable highlighting on
     * @param source The object from which keywords are grabbed
     */
    public TextHighlightController( TextArea textArea, Mediator source ){
        text = textArea;
        host = source;
        highlights = new RectangleBank();
        enableFlag = true;
    }

    /** Enables highlihting on each refresh **/
    public void configure(){
        enableFlag = true;
    }

    /** Disables text highlighting **/
    public void deconfigure(){
        removeAllHighlights();
        enableFlag = false;
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

    /**Auxilliary method to simplify math by returning 0 if teh
     * current font is the defualt font else 1
     * @return integer of boolean (font!=default)
     */
    private int isntDefaultFont(){
        //this method was added after some of its uses were already implemented
        //refactoring could be probably be using this in lou of complex arithmetic
        return text.getStyle().toString().split(";")[1].split(
                ":")[1].equals("\"Courier New\"") ? 0 : 1;
    }

    /**
     * Places a higlight around the given area defined by the parameters
     * @param caretCol The column index of the caret to begin highlighting
     * @param row The row index of the string to highlight
     */
    private void commentString(int caretCol, int row){
        //get the dimensions of the comment, account for scrollbars (quite annoying)
        int fontSize = getFontSize();
        double scrlY = text.getScrollTop()/1.008/(getFontCorrection()-.006*isntDefaultFont());
        double scrlX = text.getScrollLeft()*1.11;

        addHighlight((int)(0.9 * ((caretCol * fontSize / 12.0 + 1) * 8.0 + 22.5 )-scrlX),
                (int) (row * 13.5 * fontSize / 12.0 -scrlY),
                (int)text.getWidth()-caretCol);
        //change the color of highlight
        highlights.peek().setBlendMode(commentColor);
    }

    /**
     *  Adds a highlight to the textarea. This function takes into account font size; it is calibrated for the fonts
     *  currently accessible to the user in CPUSim - 10,12,14,16,18. Any font sizes other than these will probably
     *  be formatted correctly, but the function does not guarantee accuracy beyond this range (especially in
     *  extreme cases).
     *  @param xloc the horizontal position of the highlight
     *  @pararm yloc the veritical position of the highlight
     *  @param width the width of the highlight
     */
    public void addHighlight( int xloc, int yloc, int width ){
        int fontSize = getFontSize();
        //this function looks gross. It seems to be easily refactorable,but there's a
        // difference between parts using integer arithmetic and parts that aren't.

        //clip
        if ( xloc < 27 ){
            width = (int)(width - (27.0 - xloc )/fontSize > 0 ?
                    width - (27.0 - xloc )/fontSize : 0);
            xloc = 27;
        }

        //build the highlight
        Rectangle highlight = highlights.request();
        highlight.setWidth(width*(6+fontSize/12+2*fontSize/16)+6+(fontSize-12.0)*2+fontSize/17);
        highlight.setHeight(14+fontSize/13*4.0+fontSize/15*3.0+fontSize/17*4.0 -
                (1-getFontCorrection())*(20+fontSize/16*40.0));

        //configure it and place it
        highlight.setDisable(true); //click through highlight
        highlight.setTranslateX(xloc + 5 + getLinenumberOffset());
        highlight.setTranslateY(-310 + yloc * getFontCorrection() - (text.getHeight() - 644) * .5 + fontSize / 13 * 2.0 +
                fontSize / 16 * 2.0 + fontSize / 17 - (1 - getFontCorrection()) * 20);

        //assume keyword color in highlight; color it
        String color = host.getDesktopController().getTextFontData().background;
        highlight.setFill(Paint.valueOf(color));
        highlight.setBlendMode(keywordColor);

        //add it
        ((GridPane) text.getParent()).add(highlight, 0, 0);
    }

    /** Sets the appropriate colors for highlighting
     */
    public void updateColors(){
        keywordColor = getLargestContrast();
        commentColor = getNotLargestContrast();
        //label not guaranteed to be different color than comment
        if (!keywordColor.equals(BlendMode.BLUE))
            labelColor = BlendMode.BLUE;
        else
            labelColor = BlendMode.GREEN;
    }

    /** Returns the BlendMode indicating the largest contrast
     * in color between the fontcolor and the background color.
     * @return The BlendMode of largest Contrast
     */
    private BlendMode getLargestContrast(){
        String bgColorString = host.getDesktopController().getTextFontData().background;
        String fgColorString = host.getDesktopController().getTextFontData().foreground;
        Color bgColor = Color.web(bgColorString);
        Color fgColor = Color.web(fgColorString);
        double dR = bgColor.getRed() - fgColor.getRed();
        double dG = bgColor.getGreen() - fgColor.getGreen();
        double dB = bgColor.getBlue() - fgColor.getBlue();
        if ( dR > dG && dR > dB )
            return BlendMode.RED;
        if ( dG > dR && dG > dB )
            return BlendMode.BLUE;
        return BlendMode.GREEN;
    }

    /** Returns a BlendMode that does not indicate the largest
     * contrast in color between the fontcolor and background color
     * @return A BlendMode that is not the largest contrast
     */
    private BlendMode getNotLargestContrast(){
        //this may seem odd - why not return the second biggest contrast, say?
        //this method allows us to make an important optimization in several places;
        //namely we can use the fact that comment highlights are never blue-blended.
        //thus, we can simply pick a color with the ame blue as the background color
        //and we are guaranteed that it will never be effected by highlights. One
        //example of this optimization is in the comment size and scrollbar - instead
        //of an incredibly messy calculation of how big the comment highlight should
        //be, we simply say go for a very long time; the scrollbar can then pick some
        //pretty color for the thumb without intense calculation (or call to this
        //method) by assuming that the highlight never blends blue.
        if ( getLargestContrast().equals(BlendMode.RED))
            return BlendMode.GREEN;
        return BlendMode.RED;
    }

    /** Returns the horizontal offset effect of line number **/
    private double getLinenumberOffset(){
        int debugOffset = host.getDesktopController().getInDebugMode() ? 30 : 0;
        return ( (GridPane)text.getParent()).getColumnConstraints().get(1).getMaxWidth()
                + debugOffset - 30; //30 is the default offset
    }

    /** Returns the current size of font used in the textarea **/
    private int getFontSize(){
        return Integer.valueOf( text.getStyle().toString().split(";")[0].split(":")[1] );
    }

    /** Returns a scalar that accounts for changes in font. Is hard-calibrated for fonts currently
     * used in CPUSim; introducing new fonts will log a warning and act badly. This method is for
     * internal use and as the number it returns is particular and semi-arbitrary, should only be
     * used after careful testing.
     */
    private double getFontCorrection(){
        //We can call (1-getFontCorrection()) and scale by getFontCorrection() to
        //guarantee that default is unchanged
        String font = text.getStyle().toString().split(";")[1].split(":")[1];
        if (font.equals("\"Courier New\"")){
            return 1.0; //default
        }
        else if (font.equals("\"Lucida Console\"")){
            return 0.88790035587188612099644128113879; //empirical. Change at own risk
        }
        Log.warning("The textarea font is unrecognized. Bad things will happen.");
        return -Double.MAX_VALUE; //do something bad if preconditions fail
    }

    /**
     * Removes all highlights
     */
    public void removeAllHighlights( ){
        highlights.freeAll();
    }

    /** Returns whether the area being modified by this enhancement is the given area
     * @return boolean indicating whether the area being modified by this enhancement is the given area
     * @param text The text area to check against
     */
    @Override
    public boolean isTextArea( TextArea text ){
        return this.text.equals(text);
    }

    /** Preprocesses the enhancemen before entering a loop of the textarea **/
    @Override
    public void preProcess(){

        removeAllHighlights();
        updateColors();
    }

    /** Postprocesses the enhancement before entering a loop of the textarea;
     * Updates scrollbars to appropriate color.
     **/
    @Override
    public void postProcess(){
        ScrollPane scrollPane = (ScrollPane) text.lookup(".scroll-pane");
        if (scrollPane == null) return;

        Collection scrollBars = scrollPane.lookupAll(".scroll-bar");

        for ( Object bar : scrollBars ){
            if ( bar instanceof ScrollBar){
                //get the original red and green (which might be blended to)
                //and guarantee that the new blue is different than the old blue
                String bgCol = host.getDesktopController().getTextFontData().background;
                String rband = String.valueOf((int)(Color.web(bgCol).getRed()*255));
                String gband = String.valueOf((int)(Color.web(bgCol).getGreen()*255));
                String bband = String.valueOf((int)((1-Color.web(bgCol).getBlue()
                        *Color.web(bgCol).getBlue())*255));

                //stylize bars
                ((ScrollBar)bar).lookup(".thumb").setStyle(
                        "-fx-background-color: rgb("+rband+","+gband+","+bband+");");
                ((ScrollBar)bar).lookup(".track").setStyle("-fx-background-color: "+bgCol+";");
                ((ScrollBar)bar).lookup(".track-background").setStyle("-fx-background-color: "+bgCol+";");
                ((ScrollBar)bar).lookup(".increment-button").setStyle("-fx-background-color: "+bgCol+";");
                ((ScrollBar)bar).lookup(".decrement-button").setStyle("-fx-background-color: "+bgCol+";");
                ((ScrollBar)bar).lookup(".increment-arrow").setStyle("-fx-background-color: "+bgCol+";");
                ((ScrollBar)bar).lookup(".decrement-arrow").setStyle("-fx-background-color: "+bgCol+";");
            }
        }
    }

    /** Mutates the word under the given conditions. Returns a flag indicating whether
     * the line should terminate checking.
     * @param word The word to consider modifying
     * @param line The line that the word is a part of
     * @param literalCol the actual column index of the word (not caret position)
     * @param row The row of the line
     **/
    public boolean mutateWord( String word, String line,  int literalCol, int row,
                               boolean isCaretWord ){

        if (!isEnabled()) return false;
        int caretCol;
        //get the comment symbol
        String comSymb = String.valueOf(host.getMachine().getCommentChar());
        if ( word.contains( comSymb) ){
            caretCol = line.indexOf(word,literalCol);

            commentString(caretCol+word.indexOf(comSymb)+1,row);
            System.out.println(highlights.limboRectangles.size());
            System.out.println(highlights.useddRectangles.size());
            System.out.println("-----");
            //return true;
        }


//        //if it's a keyword, get the screen coords and highlight; could make the
//        // map text coords->screen coords vary by font, but hardcoded for now
//        if ( isKeyword(word.replace(comSymb, "")) ||
//                (word.endsWith(":") && literalCol == 0) ){
//            caretCol = line.indexOf(word,literalCol);
//            int fontSize = getFontSize();
//            double scrlX = text.getScrollLeft()*1.11;
//            double scrlY = text.getScrollTop()/1.008/(getFontCorrection());
//
//            addHighlight((int) (0.9 * ((caretCol * fontSize / 12.0 + 1) * 8.0 + 22.5 - scrlX)),
//                    (int) (row * 13.5 * fontSize / 12.0 - scrlY), word.length());
//            //labels
//            if (word.endsWith(":") && literalCol == 0)
//                highlights.peek().setBlendMode(labelColor);
//        }
//
       return false;
    }

    /** Returns whether the area is enabled **/
    @Override
    public boolean isEnabled(){  return enableFlag;  }

    /** Returns the enhancement type of the manager **/
    @Override
    public TextAreaEnhancer.Enhancement getEnhancementType(){
        return TextAreaEnhancer.Enhancement.HIGHLIGHTING; }

    /** Warps the Enhancement along the specifications of
     * TextAreaEnhancer.
     */
    @Override public void warp(){
        return;//FIXME: for all those fancy specifications I kinda flail here
    }

    /** Storage bank for highlights **/
    private class RectangleBank{
        //This is a private inner-class because it is an implementation detail.
        //This class manages the text highlighting rectangles to optimize
        //the process - we shouldn't need to do object creation every time; when (if)
        //highlighting becomes easier in later Java FX version, this should go away.
        //The upside of this implementation is that we don't need to construct to
        //highlights for one word (or many more, potentially, per update); the downsie
        //is that if a word is deleted, say, then we're still occupying memory with its
        //old highlight on the assumption that we'll need it again - this could add up.

        //Storage of highlights being used somewhere
        private ArrayList<Rectangle> useddRectangles;
        //Storage of highlights not being used somewhere but already constructed
        private ArrayList<Rectangle> limboRectangles;

        /**Constructs a new RectangleBank**/
        public RectangleBank(){
            useddRectangles = new ArrayList<>();
            limboRectangles = new ArrayList<>();
        }

        /** Returns a Rectangle. Position and size of the rectangle are assumed arbitrary
         * @return Rectangle a rectangle from the bank
         **/
        public Rectangle request(){
            Rectangle rec; //the rectangle we're going to return
            //if there's an unused one, then use that (and mark it)
            if ( limboRectangles.size() > 0 ){
                rec = limboRectangles.remove(0);
                useddRectangles.add(rec);
                return rec;
            }
            rec = new Rectangle(0,0,0,0);
            useddRectangles.add(rec);
            return rec;
        }

        /** Removes a rectangle from the set rectangles considered "used" and moves it
         * to the set of rectangles considered "free" to be used later. Does nothing
         * if the given Rectangle is not recognized. Undraws rectangle.
         * @param  rectangle The rectangle to free
         */
        public void free( Rectangle rectangle ){
            if (!useddRectangles.remove(rectangle)) return;
            limboRectangles.add(rectangle);
            ((GridPane) text.getParent()).getChildren().remove(rectangle);
        }

        /** Removes all rectangles from the set rectangles considered "used" and moves
         * them to the set of rectangles considered "free" to be used later.
         */
        public void freeAll(){
            //more efficient than calling free in a loop, as we'd hope
            ((GridPane) text.getParent()).getChildren().removeAll(useddRectangles);
            limboRectangles.addAll(useddRectangles);
            useddRectangles.clear();
        }

        /** Returns the last active Rectangle or null if there are no active rectangles
         * @return  Rectangle the last active Rectangle
         **/
        public Rectangle peek(){
            if (useddRectangles.size() > 0)
                return useddRectangles.get(useddRectangles.size()-1);
            return null;
        }

        /** Returns the number of active Rectangles **/
        public int size(){
            return useddRectangles.size();
        }

    }

}