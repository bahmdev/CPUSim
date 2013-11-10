package cpusim.gui.desktop.editorpane;
import javafx.beans.property.SimpleBooleanProperty;

public class DebugCellData {
	private SimpleBooleanProperty isBreakPoint;
	
	public DebugCellData() {
		isBreakPoint = new SimpleBooleanProperty(false);
	}
	
	public SimpleBooleanProperty isBreakPointProperty() {
		return isBreakPoint;
	}
	
	public void setIsBreakPoint(boolean newIsBreakPoint) {
		this.isBreakPoint.set(newIsBreakPoint);
	}
	
	public boolean getIsBreakPoint() {
		return isBreakPoint.get();
	}
	
	public String toString() {
		return "";
	}
}