package cpusim.gui.desktop.editorpane;
import javafx.beans.property.SimpleStringProperty;

public class LineNumberCellData {
	private SimpleStringProperty lineNumberString;
	
	public LineNumberCellData() {
		this(0);
	}
	
	public LineNumberCellData(int lineNumber) {
		lineNumberString = new SimpleStringProperty(String.valueOf(lineNumber));
	}
	
	public SimpleStringProperty lineNumberStringProperty() {
		return lineNumberString;
	}
	
	public void setLineNumber(int newLineNumber) {
		this.lineNumberString.set(String.valueOf(newLineNumber));
	}
	
	public void setLineNumberString(String newLineNumberString) {
		this.lineNumberString.set(newLineNumberString);
	}
	
	public int getLineNumber() {
		return Integer.parseInt(lineNumberString.get());
	}
	
	public String getLineNumberString() {
		return lineNumberString.get();
	}
	
	public String toString() {
		return lineNumberString.get();
	}
}