/**
 * File: ConsoleChannel
 * Last update: August 2013
 */
package cpusim;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import cpusim.util.Convert;
import cpusim.util.FXUtilities;

/**
 * This class implements IOChannel using a console that appears as a
 * panel along the bottom edge of the main CPU Sim desktop.
 */
public class ConsoleChannel implements IOChannel {
    private String name; 
    private TextArea ioConsole;
    
    private static enum Type {Long, ASCII, Unicode}
    private Type readingType;
    private int numBits;
    
    private boolean inputStarted;
	private boolean needToDeleteEnter;
	private boolean done;
	private int startCaret;
	private long valueFromUser;
	private Mediator mediator;
	
	private String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private boolean inputCancelled;
	
	/**
	 * Constructor for new Console Channel. There is only
	 * one Console channel that is used, however, look in
	 * CPUSimConstants file to find the ConsoleChannel.
	 * 
	 * @param name - The name given to the console channel.
	 */
    public ConsoleChannel(String name) {
        this.name = name;
        this.ioConsole = null;
        readingType = Type.Long;
        inputCancelled = false;
    }
    
    /**
     * Sets the mediator and sets up ioConsole.
     * 
     * @param med - The Mediator.
     */
    public void setMediator(Mediator med) {
    	this.mediator = med;
    	this.ioConsole = med.getDesktopController().getIOConsole();
    	
    	ioConsole.setEditable(false);
        ioConsole.setOnKeyPressed(
        		new EventHandler<KeyEvent>() {
        			@Override
        			public void handle(KeyEvent event) {
        				if (ioConsole.isEditable()) {
        					String content = ioConsole.getText();

        					if (!inputStarted) {
        						startCaret = content.length();
        					}

        					if (ioConsole.getCaretPosition() < startCaret) {
        						ioConsole.positionCaret(content.length());
        					}

        					if (event.getCode().equals(KeyCode.BACK_SPACE)) {
        						if (ioConsole.getCaretPosition() == startCaret) {
        							ioConsole.insertText(startCaret, " ");
        						}
        					}
        					else if (event.getCode().equals(KeyCode.ENTER)) {
        						inputStarted = false;
        						String enteredText = (ioConsole.getText(startCaret, ioConsole.getText().length()));
        						if (readingType == Type.Long){
        							enteredText = enteredText.trim();
        						}
        						
        						ioConsole.appendText(LINE_SEPARATOR);
        						
        						// Output directions if the user asks for "help"
        						if(enteredText.toLowerCase().equals("help")) {
        							switch(readingType) {
        							case Long:
        								ioConsole.appendText( "Type in a decimal, binary, or hexadecimal " +
        										"integer. " +
        										"For binary, use a prefix of \"0b\" or \"-0b\"." +
        										"For hexadecimal, use " +
        										"\"0x\" or \"-0x\"." + LINE_SEPARATOR +
        										"To halt execution, use the Stop menu item from the Execute menu."
        										+LINE_SEPARATOR); 
        								break;
        							case ASCII:
        								ioConsole.appendText("Type in a character with no surrounding " +
        										"quotes and then press Enter/Return." + LINE_SEPARATOR +
        										"To halt execution, use the Stop menu item from the Execute menu."
        										+LINE_SEPARATOR);
        								break;
        							case Unicode:
        								ioConsole.appendText("Type in a character with no surrounding " +
        										"quotes and then press Enter." + LINE_SEPARATOR +
        										"To halt execution, use the Stop menu item from the Execute menu."
        										+LINE_SEPARATOR);
        								break;
        							default:
        								break;
        							}
        						}
        						
        						// Get inputted value
        						switch(readingType) {
        						case Long:
        							try {
        								valueFromUser = Convert.fromAnyBaseStringToLong(enteredText);
        							} catch(NumberFormatException nfe) {
        								ioConsole.appendText("Error. You must enter an integer." + 
        										LINE_SEPARATOR + getPrompt());
        								needToDeleteEnter = true;
            							return;
        							}
        							break;
        						case ASCII:
        						case Unicode:
        							if (enteredText.length() > 0) {
        								valueFromUser = (long) enteredText.charAt(0);
        							} else {
        								valueFromUser = -1;
        							}
        							break;
        						}
        						
        						
        						// Check for validity
        						boolean validInput = false;
        						String errorMessage = "";
        						
        						switch(readingType) {
        						case Long:
        							validInput = Convert.fitsInBits(valueFromUser, numBits);
        							errorMessage = "Not enough bits to store " +
        									"the given int. Number of bits = "+numBits+".";
        							break;
        						case ASCII:
        							validInput = (0 <= valueFromUser && valueFromUser <= 255);
        							errorMessage = "Character entered is not a valid ASCII character.";
        							break;
        						case Unicode:
        							validInput = (0 <= valueFromUser && valueFromUser <= 65535);
        							errorMessage = "Character entered is not a valid Unicode character.";
        							break;
        						default:
        							break;
        						}
        						
        						// Correct for errors
        						if (!validInput) {
        							ioConsole.appendText(errorMessage);
        							ioConsole.appendText(LINE_SEPARATOR+getPrompt());
    								needToDeleteEnter = true;
        							return;
        						}
        						
        						// reset
        						ioConsole.setEditable(false);
        						done = true;
        						return;
        					}

        					inputStarted = true;
        				}
        			}
        		});

        ioConsole.setOnKeyReleased(
        		new EventHandler<KeyEvent>() {
        			@Override
        			public void handle(KeyEvent event) {
        				if (needToDeleteEnter) {
        					if (event.getCode().equals(KeyCode.ENTER)) {
        						String content = ioConsole.getText();
        						ioConsole.setText(ioConsole.getText(0, content.length()-1));
        						ioConsole.positionCaret(content.length());
        						needToDeleteEnter = false;
        					}
        				}
        			}
        		});
    }
    
    /**
     * returns the next integer from input as a long that fits in the given
     * number of bits. If it doesn't fit, a NumberFormatException is thrown.
     *
     * @param numBits the number of bits into which the long must fit
     * @return the long value that was input
     * @throws ExecutionException if it cannot read a long.
     */
    public long readLong(int numBits) {
    	this.numBits = numBits;
    	readingType = Type.Long;
    	try {
    		Platform.runLater(new Runnable() {
    			public void run() {
    				ioConsole.appendText(getPrompt());
    				ioConsole.setEditable(true);
                }
            });
    	} catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to read a value from the console.");
		}
    	
    	while (!done && !inputCancelled) {
        	try {
        		inputCancelled = mediator.getMachine().getRunMode() == Machine.RunModes.ABORT;
        		Thread.sleep(10);
			}
			catch (InterruptedException e) {
				System.out.println("Error while sleeping thread");
			}
        }
    	if (inputCancelled) {
    		Platform.runLater(new Runnable() {
    			public void run() {
    				ioConsole.appendText(LINE_SEPARATOR);
    				ioConsole.setEditable(false);
                }
            });
    	}
    	done = false;
        inputCancelled = false;
        inputStarted = false;
        return valueFromUser;
    }

    /**
     * returns the next ASCII char from input.
     *
     * @return the ASCII character read
     * @throws ExecutionException if it cannot read an ASCII char.
     */
    public char readAscii() {
    	readingType = Type.ASCII;
    	try {
    		Platform.runLater(new Runnable() {
    			public void run() {
    				ioConsole.appendText(getPrompt());
    				ioConsole.setEditable(true);
                }
            });
        } catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to read a value from the console.");
		}
    	while (!done && !inputCancelled) {
        	try {
        		inputCancelled = mediator.getMachine().getRunMode() == Machine.RunModes.ABORT;
        		Thread.sleep(10);
			}
			catch (InterruptedException e) {
				System.out.println("Error while sleeping thread");
			}
        }
    	if (inputCancelled) {
    		Platform.runLater(new Runnable() {
    			public void run() {
    				ioConsole.appendText(LINE_SEPARATOR);
    				ioConsole.setEditable(false);
                }
            });
    	}
    	done = false;
        inputCancelled = false;
        inputStarted = false;
        return (char) valueFromUser;
    }

    /**
     * returns the next Unicode char from input.
     *
     * @return the Unicode character read
     * @throws ExecutionException if it cannot read an Unicode char.
     */
    public char readUnicode() {
    	readingType = Type.Unicode;
    	try {
    		Platform.runLater(new Runnable() {
    			public void run() {
    				ioConsole.appendText(getPrompt());
    				ioConsole.setEditable(true);
                }
            });
        } catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to read a value from the console.");
		}
    	while (!done && !inputCancelled) {
        	try {
        		inputCancelled = mediator.getMachine().getRunMode() == Machine.RunModes.ABORT;
        		Thread.sleep(10);
			}
			catch (InterruptedException e) {
				System.out.println("Error while sleeping thread");
			}
        }
    	if (inputCancelled) {
    		Platform.runLater(new Runnable() {
    			public void run() {
    				ioConsole.appendText(LINE_SEPARATOR);
    				ioConsole.setEditable(false);
                }
            });
    	}
    	done = false;
        inputCancelled = false;
        inputStarted = false;
        return (char) valueFromUser;
    }

    /**
     * writes the given long value to the output
     *
     * @param longValue the long value to be output
     */
    public void writeLong(final long longValue) {
    	try {
    		FXUtilities.runAndWait(new Runnable() {
    			public void run() {
    				ioConsole.appendText("Output: "+longValue+LINE_SEPARATOR);
                }
            });
        } catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to write a value to the console.");
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
    				ioConsole.appendText("Output: "+((char)longValue)+LINE_SEPARATOR);
                }
            });
        } catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to write a value to the console.");
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
                    longValue + " as an Unicode value.");
        try {
    		FXUtilities.runAndWait(new Runnable() {
    			public void run() {
    				ioConsole.appendText("Output: "+((char)longValue)+LINE_SEPARATOR);
                }
            });
        } catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to write a value to the console.");
		}
    }

    /** 
     * Reset the input and output. 
     */
    public void reset() {}

    /**
     * Gives a string representation of the object.
     * In this case, its name field.
     */
    public String toString() {
        return name;
    }
    
    private String getPrompt() {
    	switch(readingType) {
    	case Long:
    		return "Enter an integer: ";
    	case ASCII:
    		return "Enter an ASCII character: ";
    	case Unicode:
    		return "Enter a Unicode character: ";
    	}
    	return "Enter an integer: ";
    }
    
}
