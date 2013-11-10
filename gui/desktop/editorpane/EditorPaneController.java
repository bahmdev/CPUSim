package cpusim.gui.desktop.editorpane;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import cpusim.Mediator;
import cpusim.gui.desktop.DesktopController;
import cpusim.module.RAM;
import cpusim.module.RAMLocation;

public class EditorPaneController implements Initializable {
	@FXML private GridPane gridPane;
	@FXML private ListView<DebugCellData> debugListView;
	@FXML private ListView<LineNumberCellData> lineNumberListView;
	@FXML private TextArea textArea;
	
	private SimpleDoubleProperty cellHeightProperty;
	private SimpleStringProperty cellHeightString;
	private StringExpression cellHeightStyle;
	
	private SimpleDoubleProperty cellFontSizeProperty;
	private SimpleStringProperty cellFontSizeString;
	private StringExpression cellFontSizeStyle;
	
	private int currentNumLines;
	
	private Mediator mediator;
	
	/**
	 * Constructor. Initializes with one line in text editor.
	 */
	public EditorPaneController(Mediator m) {
		mediator = m;
		currentNumLines = 1;
	}
	
	/**
	 * Sets up the text area, debug column and line number
	 * column.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initializeColumnProperties();
		initialzeTextArea();
		initializeDebugColumn();
		initializeLineNumberColumn();
	}
	
	/**
	 * Initializes all of the simple string and double
	 * properties necessary to update the how
	 * height and font size;
	 */
	public void initializeColumnProperties() {
		cellHeightProperty = new SimpleDoubleProperty(14);
		cellHeightString = new SimpleStringProperty("14 px;");
		cellHeightStyle = (new SimpleStringProperty("-fx-cell-size: ")).concat(cellHeightString);
		cellHeightProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				double d = newValue.doubleValue();
				cellHeightString.set(String.valueOf(d)+" px;");
			}
		});
		
		cellFontSizeProperty = new SimpleDoubleProperty(10);
		cellFontSizeString = new SimpleStringProperty(" 10 pt;");
		cellFontSizeStyle = (new SimpleStringProperty("-fx-font-size: ")).concat(cellFontSizeString);
		cellFontSizeProperty.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable,
					Number oldValue, Number newValue) {
				double d = newValue.doubleValue();
				cellFontSizeString.set(String.valueOf(d)+" pt;");
			}
		});
	}

	/**
	 * Initializes the debug column.
	 */
	public void initializeDebugColumn() {

		final Callback<DebugCellData, ObservableValue<Boolean>> getProperty = 
				new Callback<DebugCellData, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(DebugCellData param) {
						return param.isBreakPointProperty();
					}
		};

		Callback<ListView<DebugCellData>, ListCell<DebugCellData>> forListView = 
				new Callback<ListView<DebugCellData>, ListCell<DebugCellData>>() {
			@Override
			public ListCell<DebugCellData> call(ListView<DebugCellData> param) {
				CheckBoxListCell<DebugCellData> cblc =
						new CheckBoxListCell<DebugCellData>(getProperty);
				cblc.setAlignment(Pos.CENTER);
				cblc.styleProperty().bind(cellHeightStyle.concat(cellFontSizeStyle));
				return cblc;
			}
		};
		
		debugListView.setCellFactory(forListView);

		ObservableList<DebugCellData> items = debugListView.getItems();
		for (int i = 1 ; i <= currentNumLines; i++) {
			DebugCellData newData = newDebugCellData();
			items.add(newData);
		}
		
		// To disallow scrolling
		debugListView.setEventDispatcher(
				new MyEventDispatcher(debugListView.getEventDispatcher()));
		
		
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// Get scroll bars
				final ScrollBar debugH = (ScrollBar) debugListView.lookup(".scroll-bar:horizontal");
				final ScrollBar debugV = (ScrollBar) debugListView.lookup(".scroll-bar:vertical");
				final ScrollBar textAreaV = (ScrollBar) textArea.lookup(".scroll-bar:vertical");
				
				// Opacity (-7 to get rid of scrollbars)
				if (debugH != null) {
					debugH.setStyle("-fx-opacity: 0; -fx-padding:-7;");
					debugH.setDisable(true);
				}
				if (debugV != null) {
					debugV.setStyle("-fx-opacity: 0; -fx-padding:-7;");
					debugV.setDisable(true);
				}
				
				debugListView.setStyle("-fx-padding:5 0 5 0;");
				
				// Change appropriately according to text Area
				if (textAreaV != null) {
					textAreaV.valueProperty().addListener(new ChangeListener<Number>() {
						@Override
						public void changed(ObservableValue<? extends Number> arg0,
								Number oldVal, Number newVal) {
							debugV.valueProperty().set(newVal.doubleValue());
						}
					});
				}
			}
		});
		
		int size = mediator.getDesktopController().getInDebugMode() &&
				mediator.getMachine().getCodeStore() != null ? 30 : 0;
		gridPane.getColumnConstraints().get(0).setMinWidth(size);
		gridPane.getColumnConstraints().get(0).setMaxWidth(size);
		gridPane.getColumnConstraints().get(0).setPrefWidth(size);
		
		mediator.getMachine().getCodeStoreProperty().addListener(new ChangeListener<RAM>() {
			@Override
			public void changed(ObservableValue<? extends RAM> observable,
					RAM oldValue, RAM newValue) {
				clearAllBreakPointsInRam(oldValue);
				clearAllBreakPointsInRam(newValue);
				setBreakPointsInRam(newValue);
			}
		});
		
	}
	
	/**
	 * Initializes the line number column.
	 */
	public void initializeLineNumberColumn() {
		
		final StringConverter<LineNumberCellData> stringConverter = new StringConverter<LineNumberCellData>() {
			@Override
			public LineNumberCellData fromString(String s) {
				int i = 1;
				try {
					i = Integer.parseInt(s);
				} catch(NumberFormatException nfe) {}
				return new LineNumberCellData(i);
			}
			
			@Override
			public String toString(LineNumberCellData lncd) {
				return lncd.toString();
			}
		};
		
		Callback<ListView<LineNumberCellData>, ListCell<LineNumberCellData>> forListView = 
				new Callback<ListView<LineNumberCellData>, ListCell<LineNumberCellData>>() {
			@Override
			public ListCell<LineNumberCellData> call(ListView<LineNumberCellData> param) {
				TextFieldListCell<LineNumberCellData> tflc =
						new TextFieldListCell<LineNumberCellData>(stringConverter);
				tflc.setAlignment(Pos.CENTER);
				tflc.setEditable(false);
				tflc.styleProperty().bind(cellHeightStyle.concat(cellFontSizeStyle));
				return tflc;
			}
		};
		
		lineNumberListView.setCellFactory(forListView);
		
		for (int i = 1 ; i <= currentNumLines; i++) {
			lineNumberListView.getItems().add(new LineNumberCellData(i));
		}
		
		lineNumberListView.setEventDispatcher(
				new MyEventDispatcher(lineNumberListView.getEventDispatcher()));
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				// Get scroll bars
				final ScrollBar lineNumberH = (ScrollBar) lineNumberListView.lookup(".scroll-bar:horizontal");
				final ScrollBar lineNumberV = (ScrollBar) lineNumberListView.lookup(".scroll-bar:vertical");
				final ScrollBar textAreaV = (ScrollBar) textArea.lookup(".scroll-bar:vertical");
				
				// Opacity (-7 to get rid of scroll bars)
				if (lineNumberH != null) {
					lineNumberH.setStyle("-fx-opacity: 0; -fx-padding:-7;");
					lineNumberH.setDisable(true);
				}
				if (lineNumberV != null) {
					lineNumberV.setStyle("-fx-opacity: 0; -fx-padding:-7;");
					lineNumberV.setDisable(true);
				}
				
				lineNumberListView.setStyle("-fx-padding:5 0 5 0;");
				
				// Change appropriately according to text Area
				if (textAreaV != null) {
					textAreaV.valueProperty().addListener(new ChangeListener<Number>() {
						@Override
						public void changed(ObservableValue<? extends Number> arg0,
								Number oldVal, Number newVal) {
							lineNumberV.valueProperty().set(newVal.doubleValue());
						}
					});
				}
			}
		});

		int size = mediator.getDesktopController()
				.getOtherSettings().showLineNumbers.get() ? 30 : 0;
		gridPane.getColumnConstraints().get(1).setMinWidth(size);
		gridPane.getColumnConstraints().get(1).setMaxWidth(size);
		gridPane.getColumnConstraints().get(1).setPrefWidth(size);
	}
	
	/**
	 * Initializes the main text area.
	 */
	public void initialzeTextArea() {
		textArea.setWrapText(false);
		
		setRowHeights(Integer.parseInt(
				mediator.getDesktopController().getTextFontData().fontSize));
		
		textArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, 
            		final String oldValue, final String newValue) {
            	int numLines = numberOfLinesInString(newValue);
            	ObservableList<DebugCellData> debugItems = 
            			EditorPaneController.this.debugListView.getItems();
            	ObservableList<LineNumberCellData> lineNumberItems = 
            			EditorPaneController.this.lineNumberListView.getItems();
            	if (currentNumLines > numLines) {
            		while (debugItems.size() != numLines) {
            			debugItems.remove(debugItems.size()-1);
            		}
            		while (lineNumberItems.size() != numLines) {
            			lineNumberItems.remove(lineNumberItems.size()-1);
            		}
            	} else if (currentNumLines < numLines) {
            		while (debugItems.size() != numLines) {
            			debugItems.add(newDebugCellData());
            		}
            		while (lineNumberItems.size() != numLines) {
            			lineNumberItems.add(new LineNumberCellData(lineNumberItems.size()+1));
            		}
            	}
            	currentNumLines = numLines;
            }
        });
	}
	
	/**
	 * Gives the textArea editor in this EditorPane.
	 * 
	 * @return the textArea editor in this EditorPane.
	 */
	public TextArea getTextArea() {
		return textArea;
	}
	
	/**
	 * Should be called after changing the font size
	 * of the text area through css.
	 * 
	 * @param fontPtSize - The font size that the text area was set to.
	 */
	public void setRowHeights(int fontPtSize) {
		// 0.65 is roughly what we need to keep size of 
		// text and checkboxes small enough.
		cellFontSizeProperty.set(fontPtSize*.65);
		DesktopController d = mediator.getDesktopController();
		Double newHeight = null;
		try {
			Integer key = Integer.valueOf(fontPtSize);
			boolean courier = d.getTextFontData().font.equals("\"Courier New\"");
			newHeight = d.getRowHeightController().get(key, courier).doubleValue();
		} catch(Exception e) {}
		
		if (newHeight == null) 
			return;
		
		if (cellHeightProperty.get() != newHeight.doubleValue()) {
			cellHeightProperty.set(newHeight.doubleValue());
			// Refresh
			refresh();
		}
	}
	
	/**
	 * Removes the line number column from the display.
	 */
	public void removeLineNumberColumn() {
		addOrRemoveDebugOrLineNumberColumn(false, false);
	}
	
	/**
	 * Reinstates the line number column into the display.
	 */
	public void reinstateLineNumberColumn() {
		addOrRemoveDebugOrLineNumberColumn(true, false);
	}
	
	/**
	 * Removes the debug column (column of check boxes)
	 * from the display.
	 */
	public void removeDebugColumn() {
		addOrRemoveDebugOrLineNumberColumn(false, true);
	}
	
	/**
	 * Reinstates the debug column (column of check boxes)
	 * into the display.
	 */
	public void reinstateDebugColumn() {
		addOrRemoveDebugOrLineNumberColumn(true, true);
	}
	
	/**
	 * Private method to handle all removing and reinstating
	 * of listviews in the display.
	 * 
	 * @param add Boolean to describe whether we are adding
	 * (reinstating) or removing.
	 * @param debug Boolean to describe which listView we are 
	 * reinstating or removing. True for debugListView, false for
	 * lineNumberListView.
	 */
	private void addOrRemoveDebugOrLineNumberColumn(boolean add, boolean debug) {
		int lv = debug ? 0 : 1;
		int size = add ? 30 : 0;
		gridPane.getColumnConstraints().get(lv).setMinWidth(size);
		gridPane.getColumnConstraints().get(lv).setMaxWidth(size);
		gridPane.getColumnConstraints().get(lv).setPrefWidth(size);
		refresh();
	}
	
	public boolean debugListViewIsShowing() {
		return gridPane.getColumnConstraints().get(0).getPrefWidth() > 0;
	}
	
	public boolean lineNumberListViewIsShowing() {
		return gridPane.getColumnConstraints().get(1).getPrefWidth() > 0;
	}
	
	/**
	 * Private method to refresh the display.
	 */
	private void refresh() {
		Platform.runLater(new Runnable() {
			public void run() {
				HashMap<Tab, SimpleBooleanProperty> tabsDirty = mediator.getDesktopController().getTabsDirty();
				Tab correspondingTab = null;
				for(Tab tab : tabsDirty.keySet()) {
					if (mediator.getDesktopController()
							.getTabEditorContollers().get(tab) == 
									EditorPaneController.this) {
						correspondingTab = tab;
					}
				}
				boolean oldVal = tabsDirty.get(correspondingTab).get();
				int caret = textArea.getCaretPosition();
				int anchor = textArea.getAnchor();
				textArea.appendText("\n");
				String t = textArea.getText();
				textArea.setText(t.substring(0, t.length()-1));
				textArea.selectRange(anchor, caret);
				if (!oldVal) {
					tabsDirty.get(correspondingTab).set(oldVal);
				}
			}
		});
	}
	
	public DebugCellData newDebugCellData() {
		final DebugCellData newData = new DebugCellData();
		newData.isBreakPointProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				RAM r = mediator.getMachine().getCodeStore();
				clearAllBreakPointsInRam(r);
				setBreakPointsInRam(r);
			}
		});
		return newData;
	}
	
	public void setBreakPointsInRam(RAM ram) {
		if (ram == null) {
			return;
		}
		for (RAMLocation rLoc : ram.data()) {
			if (rLoc.getSourceLine() == null) {
				continue;
			} else {
				DebugCellData correspondingData = 
						debugListView.getItems().get(rLoc.getSourceLine().getLine());
				rLoc.setBreak(correspondingData.getIsBreakPoint());
			}
		}
	}
	
	public void clearAllBreakPointsInRam(RAM ram) {
		if (ram == null) {
			return;
		}
		for (RAMLocation rLoc : ram.data()) {
			rLoc.setBreak(false);
		}
	}
	
	/**
	 * Gives an array of each line of a string.
	 * Assumes there is no wrapping.
	 * 
	 * @param s The string to split into lines.
	 * @return An Array of Strings, which contains
	 * the lines as they would appear on a text editor.
	 */
	public static final String[] getLines(String s) {
		int num = numberOfLinesInString(s);
		String[] sa = s.split("\n");
		if (sa.length != num) {
			String[] newSa = new String[num];
			for (int i = 0; i < sa.length; i++) {
				newSa[i] = sa[i];
			}
			newSa[num-1] = "";
			sa = newSa;
		}
		return sa;
	}
	
	/**
	 * Given a string, this returns the number of lines 
	 * it would take a text editor to display the string.
	 * 
	 * @param s - The string in question.
	 * @return The number of lines it would take a text 
	 * editor to display that string.
	 */
	public static final int numberOfLinesInString(String s) {
		int numLines = 1;
		// System.getProperty("line.separator") doesn't work
		// on PCs, TextArea may only use "\n" char.
		final String LINE_SEPARATOR = "\n"; 
		while(!s.equals("")) {
			if(s.startsWith(LINE_SEPARATOR)) {
				numLines++;
			}
			s = s.substring(1);
		}
		return numLines;
	}

	/**
	 * Class made to consume scroll events.
	 * 
	 * All other events get passed to the list view's
	 * original event dispatcher.
	 */
	private class MyEventDispatcher implements EventDispatcher {
		private EventDispatcher originalDispatcher;

		public MyEventDispatcher(EventDispatcher originalDispatcher) {
			this.originalDispatcher = originalDispatcher;
		}

		@Override
		public Event dispatchEvent(Event event, EventDispatchChain tail) {
			if (event instanceof ScrollEvent) {
				event.consume();
				return null;
			} else {
				return originalDispatcher.dispatchEvent(event, tail);	
			}
		}
	}
}
