/**
 * Enhanced Tab
 * @author: Matthew Levine, Bilal Ahmad
 * Last Modified: 11/15/13
 *
 * The Enhanced Tab class is an extension of the FX Tab class that allows users to type text. The enhancements
 * include text highlighting and tab completion. It abides by the Liskov Substitution principle, i.e, it can be used
 * anywhere that the Tab can be used; it is designed for the DesktopController's use.
 **/

package cpusim.gui;

import cpusim.MachineInstruction;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.*;

import java.util.*;

public abstract class GeneralSuggestionManager {
    /** The TextArea to modfiy **/
    private TextInputControl text;
    /** The map of keywords **/
    private List<String> keywordCompleteList;
    /** Utility flag for speeding up searches **/
    private boolean wordHasChanged;
    /** Utility list for speeding up searches. Is ArrayList for ease of copying **/
    private ArrayList<String> keywordSubList;
    /** Map for measuring frequency of word use **/
    private HashMap<String, Integer> wordUsageMap;
    /**Pop up for displaying suggestions to the user **/
    ContextMenu popup;
    /**Menu items to display suggestions to the user **/
    private MenuItem[] textSuggestions;

    interface KeyAction{ void runKeyAction(KeyEvent car); }

    private Map<String,KeyAction> keyToMethodMap;

    private int caretPositionX;

    /** Instantiates utility fields that are not accessible externally.
     * @param textarea  The textarea to consider input from
     */
    public void initUtilityFields(TextInputControl textarea){
        int numberOfTextSuggestions = 4;

        //instantiate fundamental fields
        text = textarea;
        wordHasChanged = true;
        wordUsageMap = new HashMap<String,Integer>();
        buildMap();

        //build the popup menu
        popup = new ContextMenu();
        addMenuItems(numberOfTextSuggestions);

        keywordCompleteList = new ArrayList<String>();
        keywordSubList = new ArrayList<String>( keywordCompleteList );
    }

    /** Builds an internal map of functions associated with each keypress. **/
    private void buildMap(){
        //this makes the loop in the key listener immensely neater and more manageable
        keyToMethodMap = new HashMap<String,KeyAction>();
        keyToMethodMap.put( "TAB",
                new KeyAction() { public void runKeyAction(KeyEvent car) {completeTheWord(car);}});
        keyToMethodMap.put( "SPACE",
                new KeyAction() { public void runKeyAction(KeyEvent car) {actOnSpaceKey(car);}});
        keyToMethodMap.put( "BACK_SPACE",
                new KeyAction() { public void runKeyAction(KeyEvent car){hidePopup();}});
        keyToMethodMap.put("ENTER",
                new KeyAction() {
                    public void runKeyAction(KeyEvent car) {
                        actOnEnterKey(car);
                    }
                });
        keyToMethodMap.put( "DOWN",
                new KeyAction() { public void runKeyAction(KeyEvent car){actOnUpDown(car);}});
        keyToMethodMap.put( "UP",
                new KeyAction() { public void runKeyAction(KeyEvent car){actOnUpDown(car);}});
    }

    /**
     * Tells the text area to highlight text
     */
    public void enableTextProcessing(){
        //add manual parse to key presses
        text.addEventFilter(KeyEvent.ANY, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent car) {
                setKeywordsFromMachine();
                onKeyPress(car);
                if (keyToMethodMap.containsKey(car.getCode().toString())) {
                    keyToMethodMap.get(car.getCode().toString()).runKeyAction(car);
                } else {
                    hidePopup();
                    if (car.getCode().isLetterKey())
                        showCompletions(car);
                }
            }
        });
        //mouse clicks kill tab completion too
        text.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                hidePopup();
                caretPositionX = text.getCaretPosition();
            }
        });
    }

    /**
     * Adds the given number of menu items to the dropdown display
     * when offering suggestions
     * @param numItems  The number of suggested items to add
     */
    public void addMenuItems( int numItems ){
        textSuggestions = new MenuItem[numItems];
        for (int i =0; i < numItems; i++){
            textSuggestions[i] = new MenuItem("");
            popup.getItems().add(textSuggestions[i]);
        }
    }

    /**
     * Identifies that the TextArea is starting a new word and updates
     * the frequency of the last word written
     * @param car The character wrapping the event triggering the method
     */
    public void actOnSpaceKey( KeyEvent car ){
        hidePopup();
        if ( !car.getEventType().toString().equals( "KEY_RELEASED" ) ) return;

        //get the word you just made
        String curText = getLastWord();

        //count how many times we use particular keywords
        if ( wordUsageMap.containsKey(curText)){
            wordUsageMap.put( curText, wordUsageMap.get(curText)+1 );
        }
        else{
            wordUsageMap.put( curText, 1);
        }
        //flag we're onto a new word
        wordHasChanged = true;

    }

    /** Allows you to select suggestions with arrow keys if suggestions are avaliable
     * @param car The character indicating an arrow key
     */
    private void actOnUpDown(KeyEvent car) {
        if ( textSuggestions.length > 0 && textSuggestions[0].isVisible() )
            return;
        showCompletions(car);
    }


    /**
     * Sets the corrected position of the caret to zero
     * @param car A wrapper for a key press triggering this call. Typically ENTER key
     */
    public void actOnEnterKey( KeyEvent car ){
        if ( !(car.getEventType().toString().equals( "KEY_RELEASED") ) ) return;
        caretPositionX = 0;
    }

    /**
     * Updates the corrected position of the caret relative to the FX caret
     * @param car A generic key event triggering the call
     */
    private void onKeyPress( KeyEvent car ){
        caretPositionX = text.getCaretPosition();
    }


    /**
     * Calculates possible suggestions based on the current word relative
     * to the FX caret and displays those suggestions
     * @param car The key press triggering this call
     */
    public void showCompletions( KeyEvent car ) {
        if (!car.getEventType().toString().equals( "KEY_RELEASED" ) )
            return;
        //get the current word
        String curText = getLastWord().toLowerCase();

        int numItems = textSuggestions.length;
        //check if this is the word we checked last time
        if ( wordHasChanged ){
            keywordSubList = new ArrayList<String>( keywordCompleteList );
            wordHasChanged = false;
        }

        String[] favoriteWords = new String[numItems];
        int[] favoriteWordUses = new int[ numItems ];
        ArrayList<String> usedWords = new ArrayList<String>();
        ArrayList<String> removedWords = new ArrayList<String>();

        getPopularWords(numItems, curText, usedWords,
                removedWords, favoriteWords, favoriteWordUses);

        keywordSubList.removeAll( removedWords );

        //show the popup menus
        for ( int i = 0; i < numItems; i++ ){
            if ( favoriteWords[i] != ""  && favoriteWords[i] != null ){
                textSuggestions[i].setVisible(true);
                makeSuggestionWindow(favoriteWords[i],
                        caretPositionX, 0, i); //make dropdown for tab completion
            }
            else{
                textSuggestions[i].setVisible( false );
            }

        }

    }

    /** Delegator methods for modifying two lists in a particular way
     * removes the words from "removedWords" that don't start with
     * curText and adding the most popular words to "favoriteWords"
     * takes in usedWords from it's caller method as not to
     * instantiate any variables in the body of the method (doing so
     * had such an effect as to slow down the text editor... not entirely
     * sure why)
     */
    private void getPopularWords( int numItems, String curText,
                                  ArrayList<String> usedWords, ArrayList<String> removedWords,
                                  String[] favoriteWords, int[] favoriteWordUses ){
        for ( int i = 0; i < numItems; i++ ){
            usedWords.add("");
            for ( String word : keywordSubList ){
                if ( !word.startsWith(curText) ){
                    removedWords.add(word);
                }
                else if (!curText.equals("")){
                    if ( usedWords.contains(word) ){ continue; }
                    if (usedWords.contains(favoriteWords[i]))
                        usedWords.remove(favoriteWords[i]);
                    //check if this word is more favorited than our current guess
                    favoriteWords[i] = favoriteWordUses[i] == 0 ? word :
                            compareTwoWordsByFrequency(word,favoriteWords[i]);
                    //update the frequency of the favorited word appropriately
                    favoriteWordUses[i] = wordUsageMap.containsKey(favoriteWords[i])
                            ? wordUsageMap.get(favoriteWords[i]) : 0;

                    usedWords.add(favoriteWords[i]);
                }
            }
        }
    }

    /**
     * Makes a popup suggesting a given name in a given slot of the popup
     * @param word The word to suggest
     * @param xoffset the y position by which to displace the widget
     * @param yoffset the y position by which to displace the widget
     * @param indexPosition the index position of the label to change
     */
    public void makeSuggestionWindow( String word, double xoffset,double yoffset,
                                      int indexPosition ){
        //inner subclass requires final variables; parameters aren't
        final String w = word;
        final int finalIndexPosition = indexPosition;
        textSuggestions[indexPosition].setText(w);

        textSuggestions[indexPosition].setOnAction(new EventHandler<ActionEvent>(){
            public void handle(ActionEvent e) {
                completeTheWord(null,finalIndexPosition);
            }
        });

        popup.show(text, Side.BOTTOM,0,0);
    }

    /**
     * Grabs the word substring closest to the caret and adds to it
     * the first word suggestion
     * @param car An optional KeyEvent (null is acceptable) if tabbing
     */
    private void completeTheWord( KeyEvent car ){
        completeTheWord( car, 0 );
    }

    /**
     * Grabs the word substring closest to the caret and adds to it
     * the first word suggestion.
     * @param car An optional KeyEvent (null is acceptable) if tabbing
     * @param indexOfSugg The index of the suggestion
     */
    private void completeTheWord(KeyEvent car, int indexOfSugg){
        if ( car != null ) car.consume();
        String word = textSuggestions[indexOfSugg].getText();
        String curPart = getLastWord();
        word = word.replaceFirst(curPart,"");
        int index = caretPositionX;
        text.insertText(index,word);
        hidePopup();
    }

    /** Hides the popup window and erases it's content **/
    public void hidePopup(){
        for ( int i = 0; i < textSuggestions.length; i++){
            textSuggestions[i].setText("");
        }
        wordHasChanged = true;
        popup.hide();
    }


    /**
     * Compares two words based on their frequency of use by the user.
     * Returns s1 if neither string has been used.
     * @param s1 The first word
     * @param s2 The second word
     * @return 1 the string used more frequently; tie goes to s1
     */
    public String compareTwoWordsByFrequency( String s1, String s2){
        if ( wordUsageMap.containsKey(s1) && wordUsageMap.containsKey(s2))
            return wordUsageMap.get( s1 ) >= wordUsageMap.get( s2 ) ? s1 : s2;
        if ( wordUsageMap.containsKey(s1) ) return s1;
        return s2;
    }

    /**
     * Retrieves the word before the caret. Does not change the position of the caret
     * @return The word before the carret.
     */
    public String getLastWord(){
        //grab the last word, make sure caret doesn't move
        int savePosition = caretPositionX;
        text.selectPreviousWord();
        String curText = text.getSelectedText();
        text.positionCaret( savePosition );
        //pressing enter doesn't by default reset caret index
        curText = curText.replace("\n","  ");

        //adjust for problems in the get previous word method; grabs too much
        curText = curText.trim();
        if (curText.contains(" "))
            curText = curText.replace( curText.substring(0, curText.indexOf(" ")),"");
        curText = curText.replace(" ","");

        return curText; //return the word you got
    }

    /** Sets the internal list of keywords to the Machine's instruction names **/
    public void setKeywordsFromMachine(){
        //this is pretty slow - O(n) for a straightforward retrieval
        //we should add a listener to the machine, it's a little tricky
        //though with handling references
        List rawKeywords = getData();
        ArrayList<String> keywordList = new ArrayList<String>(rawKeywords.size());
        for (Object mi : rawKeywords ){
            keywordList.add( mi.toString().toLowerCase() );
        }
        if ( !keywordCompleteList.equals(keywordList)){
            keywordCompleteList = keywordList;
            keywordCompleteList.add("beastwood");  //easter egg
            keywordSubList = keywordList;
            wordHasChanged = true;
        }
    }

    /** Returns the data to be used for selections **/
    public abstract List getData();

}