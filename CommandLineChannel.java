/**
 * File: CommandLineChannel
 * Last update: July 31, 2013
 */
package cpusim;

import java.util.Scanner;

/**
 * This class implements IOChannel using the terminal/command line.  It is
 * used when CPU Sim is run in non-GUI mode.
 */
public class CommandLineChannel implements IOChannel {

    Scanner scanner = new Scanner(System.in);

    /**
     * Constructor for CommandLineChannel. There is only
     * one CommandLineChannel channel that is ever used.
     */
    public CommandLineChannel() {
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
        long valueFromUser = 0;

        System.out.println("Enter an integer: ");
        while (scanner.hasNext()) {
            if(scanner.hasNextLong()) {
                valueFromUser = scanner.nextLong();
                break;
            }
            else {
                scanner.next(); //consume the illegal input
                System.out.println("Illegal input.  Enter an integer: ");
            }
        }

        return valueFromUser;
    }

    /**
     * returns the next ASCII char from input.
     *
     * @return the ASCII character read
     */
    public char readAscii() {
        String valueFromUser = "";
        System.out.println("Enter one character: ");
        if (scanner.hasNext())
            valueFromUser = scanner.next(); //consume the illegal input
        return valueFromUser.charAt(0);
    }

    /**
     * returns the next Unicode char from input.
     *
     * @return the Unicode character read
     */
    public char readUnicode() {

        return readAscii();
    }

    /**
     * writes the given long value to the output
     *
     * @param longValue the long value to be output
     */
    public void writeLong(final long longValue) {
        System.out.println("Output: "+longValue);
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
        System.out.println("Output: "+ (char)longValue);
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
        System.out.println("Output: "+(char)longValue);
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
        return "Command Line Channel";
    }

}
