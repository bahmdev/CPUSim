/**
 * File: DialogChannel
 * Last update: August 2013
 */
package cpusim;

import javafx.scene.control.Dialogs;
import cpusim.util.Convert;
import cpusim.util.FXUtilities;
import javafx.stage.Stage;

/**
 * This class implements IOChannel using Dialog Boxes.
 */
public class DialogChannel implements IOChannel {
    private Stage stage;
    private String name; 
    private Dialogs.DialogResponse result;  
    private String input;
    
    /**
     * Creates a new dialog channel. There is only
	 * one DialogChannel that is used, however, look in
	 * CPUSimConstants file to find the DialogChannel.
     * 
     * @param name - The name given to the console channel.
     */
    public DialogChannel(String name) {
        this.name = name;
       this.stage = null;
        this.result = Dialogs.DialogResponse.NO;
    }
    
    /**
     * Sets the Stage.
     * 
     * @param stage the Stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * returns the next integer from input as a long that fits in the given
     * number of bits.  If it doesn't fit, a NumberFormatException is thrown.
     *
     * @param numBits the number of bits into which the long must fit
     * @return the long value that was input
     * @throws ExecutionException if it cannot read a long.
     */
    public long readLong(final int numBits) {
    	long result = 0;
    	boolean done = false;
    	while (!done) {
    		try {
    			FXUtilities.runAndWait(new Runnable() {
    				public void run() {
    					input = Dialogs.showInputDialog(stage,
    							"Type in a decimal, binary, or hexadecimal integer\n" +
    									"or type in a character surrounded by single quotes.\n" +
    									"For binary, use a prefix of \"0b\" or \"-0b\".\n" +
    									"For hexadecimal, use \"0x\" or \"-0x\".", 
    									"Input Dialog", 
    							"Dialog Channel");
    				}
    			});
    		} catch (Exception e) {
    			throw new ExecutionException("An Exception was thrown" +
    					" when we attempted to read a value from the console.");
    		}
    		
    		if (input == null) {
    			// The user chose "Cancel" from the input dialog.
    			throw new ExecutionException("Input cancelled.");
            }
            long inputLong;
            try {
                inputLong = Convert.fromAnyBaseStringToLong(input);
                Convert.checkFitsInBits(inputLong, numBits);
                result = inputLong;
                done = true;
            } catch (NumberFormatException nfe) {
            	try {
            		FXUtilities.runAndWait(new Runnable() {
            			public void run() {
            				Dialogs.showErrorDialog(stage,
                        			"Please try again. ", 
                        			"Error", 
                        			"Dialog Channel");
            			}
            		});
        		} catch (Exception e) {
        			throw new ExecutionException("An Exception was thrown" +
        					" when we attempted to read from the console.");
        		}
            }
        }
    	// If we get to this point, input was valid.
        return result;
    }

    /**
     * returns the next ASCII char from input.
     *
     * @return the ASCII character read
     * @throws ExecutionException if it cannot read an ASCII char.
     */
    public char readAscii() {
        while (true) {
        	try {
    			FXUtilities.runAndWait(new Runnable() {
    				public void run() {
    					input = Dialogs.showInputDialog(stage,
    		        			"Type in an ASCII character for input", 
    		    				"Input Dialog", 
    		    				"Dialog Channel");
    				}
    			});
    		} catch (Exception e) {
    			throw new ExecutionException("An Exception was thrown" +
    					" when we attempted to read a value from the console.");
    		}

        	if (input == null || input.equals("")) {
                // The user chose "Cancel" from the input dialog.
                throw new ExecutionException("Input cancelled.");
            }
            char c = input.charAt(0);
            // Make sure input is valid.
            if (((int) c) > 255) {
            	try {
            		FXUtilities.runAndWait(new Runnable() {
            			public void run() {
            				Dialogs.showErrorDialog(stage,
            						"That character is not an ASCII value.\n" +
            								"Please try again.", 
            								"Error", 
            						"Dialog Channel");
            			}
            		});
        		} catch (Exception e) {
        			throw new ExecutionException("An Exception was thrown" +
        					" when we attempted to write to the console.");
        		}
                continue;
            }
            return c;
        }
    }

    /**
     * returns the next Unicode char from input.
     *
     * @return the Unicode character read
     * @throws ExecutionException if it cannot read an Unicode char.
     */
    public char readUnicode() {
    	while (true) {
    		try {
    			FXUtilities.runAndWait(new Runnable() {
    				public void run() {
    					input = Dialogs.showInputDialog(stage,
    							"Type in an Unicode character for input", 
    							"Input Dialog", 
    							"Dialog Channel");
    				}
    			});
    		} catch (Exception e) {
    			throw new ExecutionException("An Exception was thrown" +
    					" when we attempted to read a value from the console.");
    		}
        	
            if (input == null || input.equals("")) {
                // The user chose "Cancel" from the input dialog.
                throw new ExecutionException("Input cancelled.");
            }
            char c = input.charAt(0);
            // Make sure input is valid.
            if ((c) > 65535) {
            	try {
            		FXUtilities.runAndWait(new Runnable() {
            			public void run() {
            				Dialogs.showErrorDialog(stage,
            						"That character is not a Unicode value.\n" +
            								"Please try again.", 
            								"Error", 
            						"Dialog Channel");
            			}
            		});
            	} catch (Exception e) {
        			throw new ExecutionException("An Exception was thrown" +
        					" when we attempted to write to the console.");
        		}
                continue;
            }
            return c;
        }
    }

    /**
     * writes the given long value to the output
     *
     * @param value the long value to be output
     */
    public void writeLong(final long value) {
    	try {
    		FXUtilities.runAndWait(new Runnable() {
    			public void run() {
    				result = Dialogs.showConfirmDialog(stage,
    						"Next integer output value: " + value,
    						"Integer Output", 
    						"Dialog Channel", 
    						Dialogs.DialogOptions.OK_CANCEL);
                }
            });
        } catch (InterruptedException e) {
            // This usually happens if the user picks "Stop".  But the output dialog
            // is modal and so we can't pick "Stop", and instead the user must
            // click the "Abort" button in the dialog, so we need do nothing here.
        	System.out.println("This shouldn't have happened. InterruptedException " +
        			"from DialogChannel's writeLong method.");
        }
		catch (java.util.concurrent.ExecutionException e) {
			// Don't know how to handle this exception, so let's just
            // throw one of our ExecutionExceptions
            throw new ExecutionException("An ExecutionException was thrown" +
                    " when we attempted to write the long value " + value + " to" +
                    " the user in a dialog.");
		}
        if (result == Dialogs.DialogResponse.CANCEL) {
            // The user chose "Abort" from the output dialog
            throw new ExecutionException("Abort requested.");
        }
    }

    /**
     * writes the given long value to the output as an ASCII value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an ASCII char
     */
    public void writeAscii(final long longValue) {
        if (longValue > 255 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as an ASCII value.");
        try {
        	FXUtilities.runAndWait(new Runnable() {
                public void run() {
                	result = Dialogs.showConfirmDialog(stage,
                			"Next ASCII output value: " + (char) longValue,
                			"ASCII output", 
    						"Dialog Channel", 
    						Dialogs.DialogOptions.OK_CANCEL);
                }
            });
        } catch (InterruptedException e) {
            // This usually happens if the user picks "Stop".  But the output dialog
            // is modal and so we can't pick "Stop", and instead the user must
            // click the "Abort" button in the dialog, so we need do nothing here.
        } catch (java.util.concurrent.ExecutionException e) {
            //Don't know how to handle this exception, so let's just
            //throw an ExecutionException
            throw new ExecutionException("An InvocationTargetException was thrown" +
                    " when we attempted to write the ASCII value " +
                    ((char) longValue) + " to the user in a dialog.");
        }
        if (result == Dialogs.DialogResponse.CANCEL) {
            // The user chose "Abort" from the output dialog
            throw new ExecutionException("Abort requested.");
        }
    }

    /**
     * writes the given long value to the output as a Unicode value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an Unicode char
     */
    public void writeUnicode(final long longValue) {
        if (longValue > 65535 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as a Unicode value.");
        try {
        	FXUtilities.runAndWait(new Runnable() {
                public void run() {
                	result = Dialogs.showConfirmDialog(stage,
                			"Next Unicode output value: " + (char) longValue,
                			"Unicode output", 
    						"Dialog Channel", 
    						Dialogs.DialogOptions.OK_CANCEL);
                }
            });
        } catch (InterruptedException e) {
            // This usually happens if the user picks "Stop".  But the output dialog
            // is modal and so we can't pick "Stop", and instead the user must
            // click the "Abort" button in the dialog, so we need do nothing here.
        } catch (java.util.concurrent.ExecutionException e) {
            // Don't know how to handle this exception, so let's just
            // throw an ExecutionException
            throw new ExecutionException("An InvocationTargetException was thrown" +
                    " when we \nattempted to write the Unicode value " +
                    ((char) longValue) + " to the user in a dialog.");
        }
        if (result == Dialogs.DialogResponse.CANCEL) {
            // The user chose "Abort" from the output dialog
            throw new ExecutionException("Abort requested.");
        }
    }

    /**
     * Reset the channel.
     */
    public void reset() {}

    /**
     * Gives a string representation of the object.
     * In this case, its name field.
     */
    public String toString() {
        return name;
    }

}
