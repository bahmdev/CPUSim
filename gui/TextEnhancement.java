package cpusim.gui;

import javafx.scene.control.TextArea;

/**
 * @author Mathew Levine & Bilal Ahmad
 * @modified 12/06/2013
 *
 * This class is implemented by all textarea enhancements. It offers
 * limited method guarantees; it's primary purpose is polymorphism.
 * Implementers of this interface are required not to have a getter
 * for the static field enhancementType.
 */
public interface TextEnhancement {
    /** Returns the type of the enhancement **/
    abstract TextAreaEnhancer.Enhancement getEnhancementType();

    /** Enables the associated enhancment **/
    abstract void configure();

    /** Disables the associated enhancement **/
    abstract void deconfigure();

    /** Returns whether the area being modified by this enhancement is the given area
     * @return boolean indicating whether the area being modified by this
     * enhancement is the given area
     * @param text The text area to check against
     */
    abstract boolean isTextArea(TextArea text );

    /** Preprocesses the enhancement before entering a loop of the textarea.
     * Implementers are not required to take meaningful action at this step. **/
    abstract void preProcess();

    /** Postprocesses the enhancement before entering a loop of the textarea.
     * Implementers are not required to take meaningful action at this step. **/
    abstract void postProcess();

    //We purposefully violate the OO rule - don't return and modify
    //for overwhelming convenience in this next method
    /**
     * Mutates the word under the given conditions. Returns a flag indicating whether
     * the line should terminate checking.
     * @param word The word to act on
     * @param line The line containing the word
     * @param literalCol The column index of the word
     * @param row The row index of the line
     * @param isCaretWord Flag if the word is the word at the caret
     * @return whether we should stop looping after seeing this word
     */
    abstract boolean mutateWord( String word, String line,  int literalCol, int row,
                                 boolean isCaretWord );

    /** Returns whether the Enhancement is enabled **/
    abstract boolean isEnabled();

    /** Warps the Enhancement as defined by TextAreaEnhancer warpAll
     * specifications
     **/
    abstract void warp();

}
