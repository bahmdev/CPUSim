package cpusim.gui;

import cpusim.Mediator;
import cpusim.gui.desktop.TabCompletionManager;
import cpusim.gui.desktop.TextHighlightController;
import javafx.animation.AnimationTimer;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;

import java.util.*;

/**
 * @author Mathew Levine & Bilal Ahmad
 * @modified 12/06/2013
 * This class manages text enhancements on a TextArea.
 */
public class TextAreaEnhancer {

    /** Enumerated list of avalaible enhancements **/
    public static enum Enhancement {HIGHLIGHTING,TAB_COMPLETION};

    /** Interace allows for enhancement construction **/
    private interface EnhancementAction{ TextEnhancement
                                         addEnhancement(TextArea ta, Mediator host); }

    /** Map directing enhancement enums to the appropriate constructor **/
    private static HashMap<Enhancement,EnhancementAction> enhancementMap =
                                    new HashMap<Enhancement, EnhancementAction>(){{
        //Highlight enum return highlight manage
        put(Enhancement.HIGHLIGHTING, new EnhancementAction() {
            public TextEnhancement addEnhancement(TextArea ta, Mediator host) {

                return new TextHighlightController(ta, host);
            }
        });
        //tab completion enum return tab completion manager
        put(Enhancement.TAB_COMPLETION, new EnhancementAction() {
            public TextEnhancement addEnhancement(TextArea ta, Mediator host) {

                return new TabCompletionManager(ta, host);
            }
        });
    }};

    /** Give a description and a getter **/
    private ArrayList<TextEnhancement> textEnhancements;

    /** The active textarea. Give a getter **/
    private ArrayList<TextArea> observedTexts;

    /** The TextArea used only when forcing an update **/
    private TextArea forcedTextArea;

    /** Constructs a new TextAreaEnhancer to manage enhancements on text fields **/
    public TextAreaEnhancer(){
        textEnhancements = new ArrayList<>();
        observedTexts = new ArrayList<>();
        forcedTextArea = null;
        /*it is impossible to know all of the times when we need to
         * update our enhancements. It might seem that listening to changes
         * in text would be enough, but what about changes in scroll, changes
         * in font/bg color, debug mode, line numbers? There's too many things
         * to add separate listeners, and doing so would certainly entail
         * missing subtle cases.
        */
        new AnimationTimer(){
            @Override
            public void handle(long now){
                updateOnStep();
            }
        }.start();

    }

    /** Adds enhancements to the given textarea with information from the given host.
     * Does auto-configure the enhancement. Does not add an enhancement to a text
     * that already has an enhancement of that type.
     * @param textarea The TextArea to be enhanced
     * @param host The host from which to grab information about enhancing
     * @param enhancements The Type(s) of the enhancements to add
     */
    public void enhance( TextArea textarea, Mediator host, Enhancement... enhancements){
        observedTexts.add(textarea);
        for ( Enhancement enhancement : enhancements ){
            //check for duplicates
            if (!checkUniqueness(enhancement,textarea)) continue;

            //add the enhancement to the internal list
            textEnhancements.add(enhancementMap.get(
                    enhancement).addEnhancement(textarea, host));
            //enable by default
            textEnhancements.get(textEnhancements.size()-1).configure();
        }
        initFixBackspaceGlitch(textarea); //fix a Java FX Glitch
    }

    /** Returns true if the given textArea has no enhancements of the given type,
     * else returns false
     * @return True if textarea and type are a unique pair, else false
     */
    private boolean checkUniqueness( Enhancement enhancement, TextArea textArea ){
        for (TextEnhancement enh : textEnhancements ){
            if ( enh.getEnhancementType().equals(enhancement) &&
                    enh.isTextArea(textArea) ){
                return false;
            }
        }
        return true;
    }

    /** Returns the focused text area of it's managed area. Returns null
     * if none of it's managed areas are active.
     * @return TextArea the active TextArea
     */
    public TextArea getActiveTextArea(){
        for ( TextArea text : observedTexts ){
            if (text.isFocused() ) return text;
        }
        return null;
    }

    /** Configures all known enhancements
     *  @param flag If true, configures, if false, deconfigures
     **/
    public void configureAll(boolean flag){
        //This method isn't used right now, but seems like useful
        //functionality to include for completeness
        for (TextEnhancement enhancement : textEnhancements ){
            if (flag) enhancement.configure();
            else enhancement.deconfigure();
        }
    }

    /** Configures or deconfigures all known enhancements of the given type
     * @param type The type of the elements to be configured
     * @param flag If true, configures, if false, deconfigures
     */
    public void configureAll(Enhancement type, boolean flag ){
        for (TextEnhancement enhancement : textEnhancements ){
            if ( enhancement.getEnhancementType().equals(type) ){
                if (flag)  enhancement.configure();
                else enhancement.deconfigure();
            }
        }
    }

    /** Updates the active textarea per iteration **/
    public void updateOnStep(){
        //get the active textarea; quit if is none
        TextArea text = forcedTextArea == null ? getActiveTextArea() : forcedTextArea;
        if (text == null) return;

        //get the relevant enhancements
        ArrayList<TextEnhancement> relevantEnhancements = new ArrayList<>();;
        for ( TextEnhancement tEnh : textEnhancements ){
            if ( tEnh.isTextArea(text) ) relevantEnhancements.add(tEnh);
        }

        //preprocess
        for ( TextEnhancement enh : relevantEnhancements )
            enh.preProcess();

        //Enhance the text by looping over it.
        loopWords(text,relevantEnhancements);

        //postprocess
        for ( TextEnhancement enh : relevantEnhancements )
            enh.postProcess();
    }

    /** Forces the given TextArea to update it's enhancements even if it
     * is not focused.
     */
    public void forceUpdate( TextArea textArea ){
        forcedTextArea = textArea;
        updateOnStep();
        forcedTextArea = null;
    }

    /** Loops over the words in the given text and applies the appropriate
     * enhancements
     * @param text The textarea to enhance
     * @param textEnhancementList the list of enhancements to apply to the text
     */
    private void loopWords( TextArea text, Collection<TextEnhancement> textEnhancementList){
        String[] allText = text.getText().split("\n");
        int row, literalCol, caretPos,caretGuess, runningCaretGuess;
        row = literalCol = 0;
        //if we hit a comment or the like stop checking; messies up code but is efficient
        boolean stopFlag = false;
        //these track where the caret is in relation to our loop
        caretPos = text.getCaretPosition();
        caretGuess = runningCaretGuess = 0;
        boolean currentWord; //the word the caret is on matches the loop word

        for ( String line : allText ){
            String[] wordArray = line.split(" ");

            for ( String word : wordArray ){
                if (!word.equals("") && !stopFlag){
                    //if the text isn't formatted correctly (tabs not spaces) correct it
                    if ( word.contains( "\t" ) ){
                        detab(text);
                        return;
                    }
                    currentWord = caretGuess+word.length()-caretPos == 0;
                    //modify the text for each enhancement if within screen view
                    for ( TextEnhancement textEnhancement : textEnhancementList ){
                        if ( textEnhancement.isEnabled() ){
                            //as stated elsewhere, this violates the OO principle of not
                            //modifying and returning, but is overwhelmingly convenient
                            stopFlag = textEnhancement.mutateWord(word,line,literalCol,
                                    row, currentWord) != stopFlag || stopFlag;
                            if ( stopFlag ) warpAll();
                        }
                    }
                }
                literalCol += word.length()+1;
                caretGuess += word.length()+1; //tracks caret in one line

            }
            //on a new line, set variables accordingly
            stopFlag = false;
            runningCaretGuess += line.length() + 1; //tracks overall caret
            caretGuess = runningCaretGuess;
            literalCol = 0;
            row += 1;

        }

    }

    /** (Temporarily) misconfigures Enhancements so that they will not throw exception
     *  but will not visibly modify the text. The misconfiguration must
     *  be undone by a regular configuration or by normal execution of
     *  "mutateWord." The misconfiguration must hold on updates executed by the
     *  TextAreaEnhancer and by local Enhancement updates (such as locally defined
     *  event threads). The misconfiguration should not modify any underlying
     *  text field that the Enhancement has reference to; it should be a local change.
     */
    public void warpAll(){
        /* This might seem a little weird; it's original purpose is to deal with a subtle
         * but resilient bug resulting from the fact that when we quit our loop for
         * comments, the tab completion keeps trying to complete tabs but doesn't have
         * the appropriate position. An alternative would be to remove all other listeners
         * besides the one in loopWords from all Enhancement-related classes. That seems
         * pretty elegant to me, but not obvious to implement, and not necessarily
         * advantageous in other regards.
         *
         * We also note that the stopFlag in loopWords is designed to stop unnecessary
         * actions from taking place in parts of the text - like comments - where we
         * don't want to waste time modifying. This undermines that, and is meant to
         * catch the cases where that safeguard fails. In places where that safeguard
         * does not fail, this is redundant.
         *
         * In addition to the listed requirements, it is probably advisable that in
         * implementation the warp does not introduce a new field to solve this problem.
         * That actually undermines the convenience of this method. I can't claim that
         * in the external documentation, but if you're reading this, it's a good thing
         * to note.
         */
        for ( TextEnhancement textEnhancement : textEnhancements )
            textEnhancement.warp();
    }

    /** Replaces all tabs in the code with equivalent number of spaces, redraws text. If a tab is not
     * strictly defined in the original text editor, then the formatting may not reproduce consistency
     * in tab size.
     * @param text The text to detab
     **/
    public void detab( TextArea text ){
        //This method isn't particularly efficient, but we don't expect it
        //to be called frequently either
        //get variables for resetting text
        String[] totalText = text.getText().split("\n");
        String newTotalText, newLine, tabMax;
        newTotalText =  "";
        tabMax  = "                 ";//maximum size of a tab
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
    }

    /** Binds to the backspace key the fix to a glitch that occurs in Java FX 2.2
     * where scrollvalues are not updated properly
     * @param text The textarea on which to initialize glitch-fixing
     */
    private void initFixBackspaceGlitch(TextArea text){
        text.addEventFilter(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent car) {
                if ( car.getCode().toString().equals("BACK_SPACE") )
                    fixBackspaceGlitch(getActiveTextArea());
            }
        });

    }

    /**Backspaces are broken in Java FX 2.2... great!
     * Anyone relying on the scrollvalue of a TextArea or similar inputcontrol
     * will not be able to guarantee correctness in Java FX 2.2; this fixes
     * that bug
     * @param textArea The textArea in which to update scrollbars
     */
    private void fixBackspaceGlitch( TextArea textArea ){
        //this is an internal cog thrown in to fix a bug in Java FX - take it out as soon
        //as the bug is resolved
        if (textArea.getScrollTop() == 0 && textArea.getScrollLeft() == 0 ) return;

        //get the boundaries of the TextArea
        Bounds content = textArea.lookup(".content").boundsInLocalProperty().get();
        double dHeight = content.getHeight() - textArea.getHeight();
        double dWidth = content.getWidth() - textArea.getWidth();

        //get the scroll-bar parameters
        double scrollX,scrollY;
        scrollX = scrollY = 0;
        ScrollPane scrollPane = (ScrollPane) textArea.lookup(".scroll-pane");
        Collection scrollBars = scrollPane.lookupAll(".scroll-bar");
        for ( Object bar : scrollBars ){
            if ( bar instanceof ScrollBar){
                if ( ((ScrollBar) bar).getOrientation().equals(Orientation.HORIZONTAL) ){
                    scrollX = ((ScrollBar)bar).getValue();
                }
                else {
                    scrollY = ((ScrollBar)bar).getValue();
                }
            }
        }

        //update scrollbars
        if ( textArea.getScrollLeft() != 0 )
            textArea.setScrollLeft(dWidth*scrollX+4);
        else if ( textArea.getScrollTop() != 0 )
            textArea.setScrollTop(dHeight*scrollY+4);

    }

}
