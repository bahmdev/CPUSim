/**
 * File: ConcreteChannel
 * Last update: August 2013
 */
package cpusim;

/**
 * This class represents an IOChannel that uses the State pattern to
 * delegate the actual behavior to another IOChannel. It just provides
 * an extra level of indirection to allow for separation of the GUI from
 * the model. This class is part of the model. The state channels
 * involve the GUI as needed.  It is up to the GUI to add these state
 * channels.
 */
public class ConcreteChannel implements IOChannel {
    private String name;
    private IOChannel state;
    
    /**
     * The constructor to create a ConcreteChannel.
     * 
     * @param n - The name of the channel.
     * @param s - The actual channel this concrete channel
     * uses to execute any instructions.
     */
    public ConcreteChannel(String n, IOChannel s) {
        this.state = s;
        this.name = n;
    }
    
    /**
     * Creates a concrete channel with null name.
     * 
     * @param ioc - The actual channel this concrete channel
     * uses to execute any instructions.
     */
    public ConcreteChannel(IOChannel ioc) {
    	this(null, ioc);
    }
    
    /**
     * Creates a concrete channel with null IOChannel.
     * 
     * @param n - The name of the channel.
     */
    public ConcreteChannel(String n) {
        this(n, null);
    }
    
    /**
     * Sets the IOChannel.
     * 
     * @param c - The actual channel this concrete channel
     * uses to execute any instructions.
     */
    public void setState(IOChannel c) { 
    	this.state = c; 
    }
    
    /**
     * Uses the state to read a long from the channel.
     * 
     * @param numBits - the number of bits the long should be 
     * able to fit into.
     */
    public long readLong(int numBits) {
        return state.readLong(numBits);
    }
    
    /**
     * Uses the state to read an ASCII character from the channel.
     */
    public char readAscii() {
        return state.readAscii();
    }
    
    /**
     * Uses the state to read a Unicode character from the channel.
     */
    public char readUnicode() {
        return state.readUnicode();
    }
    
    /**
     * Uses the state to output a Long value to the user.
     * 
     * @param value - the value to output to the user.
     */
    public void writeLong(long value) {
        state.writeLong(value);
    }
    
    /**
     * Uses the state to output an ASCII value to the user.
     * 
     * @param longValue - the long value of the character to
     * output to the user.
     */
    public void writeAscii(long longValue) {
        state.writeAscii(longValue);
    }
    
    /**
     * Uses the state to output a Unicode value to the user.
     * 
     * @param longValue - the long value of the Unicode character
     * to output to the user.
     */
    public void writeUnicode(long longValue) {
        state.writeUnicode(longValue);
    }
    
    /**
     * Resets the state.
     */
    public void reset() {
        state.reset();
    }
    
    /**
     * Gives the current state (IOChannel).
     * 
     * @return - the current state (IOChannel).
     */
    public IOChannel getChannel() {
    	return state;
    }
    
    /**
     * Gives a string representation of the Concrete channel.
     */
    public String toString() { 
    	if (state == null) {
    		return name; 
    	}
    	else {
    		return state.toString();
    	}
    }
}
