package cpusim.gui.desktop;

import java.util.HashMap;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Path;
import javafx.stage.Stage;
import javafx.stage.Window;
import cpusim.gui.desktop.editorpane.EditorPaneController;

public class RowHeightController {
	public static String forResettingRowHeights = "\na\nb\nA\nB\nc\nC\nd\nD\nE" +
			"\ne\nF\nf\nG\ng\nX\nx\nZ\nz\n1\n2\n3\n4\n5\n6\n7\n8\n9";
	
	private HashMap<Integer, Double> courierNewMap;
	private HashMap<Integer, Double> lucidaConsoleMap;
	
	private int i, j;
	public final String[] fonts = {"\"Courier New\"", "\"Lucida Console\""};
	public final int[] sizes = {10, 12, 14, 16, 18};
	
	private boolean done;

	public RowHeightController() {
		courierNewMap = new HashMap<Integer, Double>();
		lucidaConsoleMap = new HashMap<Integer, Double>();
		done = false;
	}

	public void put(Integer key, Double value, boolean courier) {
		HashMap<Integer, Double> h = courier ? courierNewMap : lucidaConsoleMap;
		h.put(key, value);
	}

	public Double get( Integer key, boolean courier) {
		HashMap<Integer, Double> h = courier ? courierNewMap : lucidaConsoleMap;
		return h.get(key);
	}
	
	public void calculateAndSaveAll(TextArea ta) {
		i = 0;
		j = 0;
		ta.setWrapText(false);
		calculateAndSaveRowHeight(ta);
	}

	private void calculateAndSaveRowHeight(final TextArea textArea) {
		if (i >= fonts.length || j >= sizes.length) {
			return;
		}
		
		int fontSize = sizes[j];
		String font = fonts[i];
		
		// Make and set Style
		String style = "-fx-font-size: "+fontSize+";" +
				"-fx-font-family:" + font+";";
		textArea.setStyle(style);

		// Set Text
		String s = "";
		for (int i = 0; i < 120; i++) {
			s += forResettingRowHeights;
		}
		textArea.setText(s);
		textArea.positionCaret(textArea.getText().length());

		// Calculate
		final Integer integer = Integer.valueOf(fontSize);
		final boolean courier = font.equals("\"Courier New\"");
		Platform.runLater(new Runnable() {
			public void run() {
				Path caret = findCaret(textArea);
				Point2D caretLoc = findScreenLocation(caret);
				Point2D textAreaLoc = findScreenLocation(textArea);
				int numLines = EditorPaneController.numberOfLinesInString(textArea.getText());
				double newSize = (caretLoc.getY()-textAreaLoc.getY())/(numLines-1);
				put(integer, Double.valueOf(newSize), courier);
				// Return if done, else increment.
				if (j < sizes.length-1) {
					j++;
				} else if (j == sizes.length-1) {
					j = 0;
					if (i < fonts.length-1) {
						i++;
					} else if (i == fonts.length-1) {
						((Stage) textArea.getScene().getWindow()).close();
						done = true;
						return;
					}
				}
				calculateAndSaveRowHeight(textArea);
			}
		});
	}
	
	public boolean getIsDoneCalculatingValues() {
		return done;
	}

	/**
	 * by James_D  on May 6, 2013 1:05 AM
	 * From Oracle Forum: https://forums.oracle.com/thread/2534556
	 * 
	 * @param node
	 * @return
	 */
	private Point2D findScreenLocation(Node node) {
		double x = 0;
		double y = 0;
		for (Node n = node; n != null; n=n.getParent()) {
			Bounds parentBounds = n.getBoundsInParent();
			x += parentBounds.getMinX();
			y += parentBounds.getMinY();
		}
		Scene scene = node.getScene();
		x += scene.getX();
		y += scene.getY();
		Window window = scene.getWindow();
		x += window.getX();
		y += window.getY();
		Point2D screenLoc = new Point2D(x, y);
		return screenLoc;
	}

	/**
	 * by James_D  on May 6, 2013 1:05 AM
	 * From Oracle Forum: https://forums.oracle.com/thread/2534556
	 * 
	 * @param parent
	 * @return
	 */
	private Path findCaret(Parent parent) {
		// Warning: this is an ENORMOUS HACK
		for (Node n : parent.getChildrenUnmodifiable()) {
			if (n instanceof Path) {
				return (Path) n;
			} else if (n instanceof Parent) {
				Path p = findCaret((Parent) n);
				if (p != null) {
					return p;
				}
			}
		}
		return null;
	}
	
}

