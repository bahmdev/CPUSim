/*
 * Base.java
 */
package cpusim.gui.util;

/**
 * This class stores a base ("Bin", "Dec", or "Hex")
 * for displaying values of registers or RAM cells.
 * @author Ben Borchard
 */
public class Base {
	public static final String BINARY = "Bin";
	public static final String DECIMAL = "Dec";
	public static final String HEX = "Hex";
	
    private String base;
    
    public Base(String base) {
        this.base = base;
    }
    
    public String getBase() {
        return base;
    }
    
    public void setBase(String base) {
        this.base = base;
    }

    public String toString() {
    	return base;
    }
}
