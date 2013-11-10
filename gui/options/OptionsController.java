package cpusim.gui.options;

/**
 * Controller for the Options Dialog.
 *
 * @author Stephen Morse
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) changed saveLoadingTab and saveHighlightingOptions methods so that they work
 * with the Validate class to validate their data before saving said data
 * 2.) removed one try catch and allOkay variable and replaced it with a try catch for a validation exception
 */

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import cpusim.FileChannel;
import cpusim.IOChannel;
import cpusim.Mediator;
import cpusim.Microinstruction;
import cpusim.assembler.PunctChar;
import cpusim.assembler.PunctChar.Use;
import cpusim.gui.help.HelpController;
import cpusim.gui.util.EditingStrCell;
import cpusim.microinstruction.IO;
import cpusim.module.RAM;
import cpusim.module.Register;
import cpusim.util.CPUSimConstants;
import cpusim.util.Convert;
import cpusim.util.RegisterRAMPair;

public class OptionsController implements Initializable {

    @FXML private BorderPane mainBorderPane;

    @FXML private Button helpButton;
    @FXML private Button OKButton;
    @FXML private Button cancelButton;
    @FXML private Button newButton;
    @FXML private Button deleteButton;
    @FXML private Button duplicateButton;
    @FXML private Button applyButton;

    @FXML private TabPane tabPane;
    @FXML private Tab IOOptionsTab;
    @FXML private Tab highlightingTab;
    @FXML private Tab loadingTab;
    @FXML private Tab punctuationTab;
    
    @FXML private AnchorPane ioOptionsAnchorPane;
    @FXML private TableView<IOOptionsData> IOOptionsTable;
    @FXML private TableColumn<IOOptionsData, IO> nameColumn;
    @FXML private TableColumn<IOOptionsData, IOChannel> connectionColumn;
    private ObservableList<IOChannel> allChannels;

    @FXML private TableView<RegisterRAMPair> highlightingTable;
    @FXML private TableColumn<RegisterRAMPair, Register> registerColumn;
    @FXML private TableColumn<RegisterRAMPair, RAM> RAMColumn;
    @FXML private TableColumn<RegisterRAMPair, Boolean> dynamicColumn;

    @FXML private ChoiceBox<RAM> codeStore;
    @FXML private TextField startingAddress;
    
    @FXML private TableView<PunctChar> leftPunctuationTable;
    @FXML private TableColumn<PunctChar, String> leftASCIIColumn;
    @FXML private TableColumn<PunctChar, Use> leftTypeColumn;
    @FXML private TableView<PunctChar> rightPunctuationTable;
    @FXML private TableColumn<PunctChar, String> rightASCIIColumn;
    @FXML private TableColumn<PunctChar, Use> rightTypeColumn;

    private RegisterRAMPair highlightingSelectedSet;
    private IOOptionsData IOOptionsSelectedSet;
    private Mediator mediator;
    private ObservableList<Register> registers;
    private ObservableList<RAM> RAMs;
    
    /**
     * Constructor with mediator passed
     * from the desktop controller.
     *
     * @param med The mediator.
     */
    public OptionsController(Mediator med) {
        mediator = med;
        registers = mediator.getMachine().getAllRegisters();
        RAMs = mediator.getMachine().getAllRAMs();
    }

    /**
     * Initializes the options window.
     *
     * @param arg0 Standard URL
     * @param arg1 Standard ResourceBundle
     */
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        setUpHighlightingTab();
        setUpLoadingTab();
        setUpPunctuationTab();
        setUpIOOptionsTab();
    }

    /////////////// Buttons ///////////////

    /**
     * Called whenever the Help ("?") button
     * is clicked.
     */
    public void onHelpButtonClicked() {
    	String startString = "Options Dialog";
    	String appendString = "";
    	if (IOOptionsTab.isSelected())
    		appendString = "#IOConnections";
    	else if (highlightingTab.isSelected())
    		appendString = "#Highlighting";
    	else if (loadingTab.isSelected())
    		appendString = "#Loading";
    	else if (punctuationTab.isSelected())
    		appendString = "#Punctuation";
    	
    	if (mediator.getDesktopController().getHelpController() == null) {
			HelpController helpController = HelpController.openHelpDialog(
					mediator.getDesktopController(), startString, appendString);
			mediator.getDesktopController().setHelpController(helpController);
			return;
		}
		else {
			HelpController hc = mediator.getDesktopController().getHelpController();
			hc.getStage().toFront();
			hc.selectTreeItem(startString, appendString);
		}
    }

    /**
     * Called whenever the Apply button
     * is clicked.
     */
    public void onApplyButtonClicked() {
        if (highlightingTab.isSelected()) {
            saveHighlightingTab();
        }
        else if (IOOptionsTab.isSelected()) {
            saveIOOptionsTab();
        }
        else if (loadingTab.isSelected()) {
            saveLoadingTab();
        }
        else if (punctuationTab.isSelected()) {
            savePunctuationTab();
        }
    }

    /**
     * Called whenever the OK button
     * is clicked.
     */
    public void onOKButtonClicked() {
    	// Save individual tabs
        
            boolean canClose = true;
            
            saveHighlightingTab();
            saveIOOptionsTab();
            canClose &= saveLoadingTab();
            canClose &= savePunctuationTab();

            if (canClose){
                // Machine changed & close window.
        //changed to avoid reference to DesktopController object
        //previously mediator.getDesktopController.changedMachine()
                mediator.setMachineDirty(true);
                Stage stage = (Stage) OKButton.getScene().getWindow();
                //close window.
                stage.close();
            }

    }

    /**
     * Called whenever the Cancel button
     * is clicked.
     */
    public void onCancelButtonClicked() {
    	cancelButton.setDisable(true);
        //close window.
        ((Stage) (helpButton.getScene().getWindow())).close();
    }

    /**
     * Called whenever the New button is clicked.
     * Button is only within the highlighting tab.
     */
    public void onNewButtonClicked() {
    	// Add newSet
        ObservableList<RegisterRAMPair> data = highlightingTable.getItems();
        RegisterRAMPair newSet = new RegisterRAMPair(
        		registers.get(0), RAMs.get(0), false);
        data.add(0, newSet);
        
        // Select first and scroll to top
        highlightingTable.getSelectionModel().clearSelection();
        highlightingTable.getSelectionModel().selectFirst();
        highlightingTable.scrollTo(0);
        
        updateHighlightingClickables();
    }

    /**
     * Called whenever the Delete button is clicked.
     * Button is only within the highlighting tab.
     */
    public void onDeleteButtonClicked() {
        ObservableList<RegisterRAMPair> data = highlightingTable.getItems();
        int index = data.indexOf(highlightingSelectedSet);
        if (index >= 0) {
            data.remove(index);
            highlightingTable.setItems(data);
        }

        // Select Correctly 
        int indexToSelect = index-1 < 0 ? index : index-1;
        if (highlightingTable.getItems().size() > 0) {
        	highlightingTable.getSelectionModel().clearSelection();
        	highlightingTable.getSelectionModel().select(indexToSelect);
        }

        updateHighlightingClickables();
    }

    /**
     * Called whenever the New button is clicked.
     * Button is only within the highlighting tab.
     */
    public void onDuplicateButtonClicked() {
        ObservableList<RegisterRAMPair> data = highlightingTable.getItems();
        int index = data.indexOf(highlightingSelectedSet);
        if (index >= 0) {
        	// Make newSet and add to table
        	RegisterRAMPair newSet = highlightingSelectedSet.clone();
        	data.add(0, newSet);

        	// Select first and scroll to top
        	highlightingTable.getSelectionModel().clearSelection();
        	highlightingTable.getSelectionModel().selectFirst();
            highlightingTable.scrollTo(0);
        }
        updateHighlightingClickables();
    }

    /////////////// Saving Tabs ///////////////

    public void saveHighlightingTab() {
        if (!highlightingTab.isDisabled()) {
        	ObservableList<RegisterRAMPair> data = highlightingTable.getItems();
            Validate.allRegisterRAMPairAreUnique(data);
        	mediator.setRegisterRamPairs(data);
        }
    }

    public void saveIOOptionsTab() {
    	if (!IOOptionsTab.isDisabled()) {
    		ObservableList<Microinstruction> ios = mediator.getMachine().getMicros("io");
            ObservableList<IOOptionsData> data = IOOptionsTable.getItems();
            for (IOOptionsData d : data) {
                IO micro = (IO) ios.get(ios.indexOf(d.getIo()));
                micro.setConnection(d.getChannel());
            }
            mediator.getMachine().setMicros("io", ios);
    	}
    }

    public boolean saveLoadingTab() {
    	if (!loadingTab.isDisabled()) {
	        // save the ram used for storing instructions
	        mediator.getMachine().setCodeStore(codeStore.getValue());
	
	        // Save starting address
	        int ramLength = codeStore.getValue().getLength();
	        String startString = startingAddress.getText();
	        int startInt;
                
                try{
                    Validate.startingAddressIsValid(startString, ramLength);
                    mediator.getMachine().setStartingAddressForLoading(
                            (int)Convert.fromAnyBaseStringToLong(startString));
                    return true;
                }
                catch(ValidationException ex){
                    if (tabPane.getSelectionModel().getSelectedItem() != loadingTab) {
                            tabPane.getSelectionModel().select(loadingTab);
                    }
                    Dialogs.showErrorDialog((Stage)OKButton.getScene().getWindow(), ex.getMessage(),
                            "Starting Address Error");
                    return false;
                }
        }
        return true;
    }

    public boolean savePunctuationTab() {
    	if (!punctuationTab.isDisabled()) {
            ObservableList<PunctChar> punctChars = leftPunctuationTable.getItems();
            punctChars.addAll(rightPunctuationTable.getItems());
            try{
                Validate.punctChars(punctChars);
                PunctChar[] pca = new PunctChar[punctChars.size()];
                for (int i=0; i<punctChars.size(); i++){
                    pca[i] = punctChars.get(i);
                }
                mediator.getMachine().setPunctChars(pca);
                return false;
            }
            catch(ValidationException ex){
                if (tabPane.getSelectionModel().getSelectedItem() != punctuationTab) {
                        tabPane.getSelectionModel().select(punctuationTab);
                }
                Dialogs.showErrorDialog((Stage)OKButton.getScene().getWindow(), ex.getMessage(), "Punctuation"
                        + " Character Error");
                return true;
            }
    	}
        return false;
    }

    /////////////// Button Disable/Enablers ///////////////

    /**
     * Used to control the disabling/enabling of
     * the buttons within the highlighting tab.
     * Calling this method should update
     * the New, Delete, and Duplicate buttons according to the state of the
     * window.
     */
    public void updateHighlightingClickables() {
        if (highlightingTable.getItems().isEmpty()) {
            deleteButton.setDisable(true);
            duplicateButton.setDisable(true);
        }
        else {
            if (highlightingSelectedSet == null) {
                deleteButton.setDisable(true);
                duplicateButton.setDisable(true);
            }
            else {
                if (highlightingTable.getItems().indexOf(highlightingSelectedSet) >= 0) {
                    deleteButton.setDisable(false);
                    duplicateButton.setDisable(false);
                }
                else {
                    deleteButton.setDisable(true);
                    duplicateButton.setDisable(true);
                }
            }
        }
    }

    /**
     * Used to control the disabling/enabling of
     * the three buttons along the bottom of the frame.
     * Calling this method should update the Apply, Cancel,
     * and OK buttons according to the state of the window.
     */
    public void updateGlobalClickables() {
        // update apply
    }

    /////////////// Update Tabs ///////////////

    /**
     * This method is called each time the tab
     * changes.
     */
    public void onSelectionChanged() {
        if (IOOptionsTab.isSelected()) {
            updateGlobalClickables();
        }
        else if (highlightingTab.isSelected()) {
            updateHighlightingClickables();
            updateGlobalClickables();
        }
        else if (loadingTab.isSelected()) {
            updateGlobalClickables();
        }
        else if (punctuationTab.isSelected()) {
            updateGlobalClickables();
        }
    }

    ///////////// Setting Up Tables //////////////

    /**
     * Initializes the Highlighting tab.
     */
    private void setUpHighlightingTab() {

        // Disable tab if there is nothing to show.
        if (registers.size() < 1 || RAMs.size() < 1) {
            highlightingTab.setDisable(true);
            return;
        }

        // Making column widths adjust properly
        highlightingTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // updates selectedSet, disables/enables buttons
        highlightingTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<RegisterRAMPair>() {
                    @Override
                    public void changed(ObservableValue<? extends RegisterRAMPair> selected,
                                        RegisterRAMPair oldSet, RegisterRAMPair newSet) {
                        highlightingSelectedSet = newSet;
                        updateHighlightingClickables();
                    }
                });

        // Accounts for width changes.
        highlightingTable.widthProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                        Double newWidth = (Double) newValue;
                        Double sum = registerColumn.getWidth()
                                + RAMColumn.getWidth()
                                + dynamicColumn.getWidth();
                        Double perc = sum/oldValue.doubleValue()*.94;

                        registerColumn.setPrefWidth(newWidth * perc *
                                registerColumn.getWidth() / sum);
                        RAMColumn.setPrefWidth(newWidth * perc *
                                RAMColumn.getWidth() / sum);
                        dynamicColumn.setPrefWidth(newWidth * perc *
                                dynamicColumn.getWidth() / sum);
                    }
                });
        
        // Callbacks
        Callback<TableColumn<RegisterRAMPair, Register>, TableCell<RegisterRAMPair, Register>>
                cellComboRegisterFactory =
                new Callback<TableColumn<RegisterRAMPair, Register>, TableCell<RegisterRAMPair, Register>>() {
                    @Override
                    public TableCell<RegisterRAMPair, Register> call(
                            TableColumn<RegisterRAMPair, Register> setStringTableColumn) {
                        return new ComboBoxTableCell<>(registers);
                    }
                };

        Callback<TableColumn<RegisterRAMPair, RAM>, TableCell<RegisterRAMPair, RAM>>
                cellComboRAMFactory =
                new Callback<TableColumn<RegisterRAMPair, RAM>, TableCell<RegisterRAMPair, RAM>>() {
                    @Override
                    public TableCell<RegisterRAMPair, RAM> call(
                            TableColumn<RegisterRAMPair, RAM> setStringTableColumn) {
                        return new ComboBoxTableCell<RegisterRAMPair, RAM>(RAMs);
                    }
                };

        Callback<TableColumn<RegisterRAMPair, Boolean>, TableCell<RegisterRAMPair, Boolean>>
                cellCheckBoxFactory =
                new Callback<TableColumn<RegisterRAMPair, Boolean>, TableCell<RegisterRAMPair, Boolean>>() {
                    @Override
                    public TableCell<RegisterRAMPair, Boolean> call(
                            TableColumn<RegisterRAMPair, Boolean> setStringTableColumn) {
                    	CheckBoxTableCell<RegisterRAMPair, Boolean> cbtc = 
                    			new CheckBoxTableCell<RegisterRAMPair, Boolean>();
                    	cbtc.setAlignment(Pos.CENTER);
                    	return cbtc;
                    }
                };

        // SetCellValueFactories
        registerColumn.setCellValueFactory(new PropertyValueFactory<RegisterRAMPair, Register>("register"));
        RAMColumn.setCellValueFactory(new PropertyValueFactory<RegisterRAMPair, RAM>("ram"));
        dynamicColumn.setCellValueFactory(new PropertyValueFactory<RegisterRAMPair, Boolean>("dynamic"));

        // Register Factories and setOnEditCommits
        registerColumn.setCellFactory(cellComboRegisterFactory);
        registerColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<RegisterRAMPair, Register>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<RegisterRAMPair, Register> text) {
                        ((RegisterRAMPair) text.getRowValue()).setRegister(
                                text.getNewValue());
                    }
                });

        RAMColumn.setCellFactory(cellComboRAMFactory);
        RAMColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<RegisterRAMPair, RAM>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<RegisterRAMPair, RAM> text) {
                        text.getRowValue().setRam(
                                text.getNewValue());
                    }
                });

        dynamicColumn.setCellFactory(cellCheckBoxFactory);
        dynamicColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<RegisterRAMPair, Boolean>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<RegisterRAMPair, Boolean> text) {
                        text.getRowValue().setDynamic(
                                text.getNewValue());
                    }
                });

        // Load in Rows
        ObservableList<RegisterRAMPair> data = highlightingTable.getItems();
        ObservableList<RegisterRAMPair> regRamPairs = mediator.getRegisterRAMPairs();
        for (RegisterRAMPair rrp : regRamPairs) {
            data.add(rrp.clone());
        }
        highlightingTable.setItems(data);
    }

    /**
     * Initializes the Loading tab.
     */
    private void setUpLoadingTab() {
        if (RAMs.size() < 1) {
            loadingTab.setDisable(true);
            return;
        }
        else {
            codeStore.setItems(RAMs);
            codeStore.setValue(mediator.getMachine().getCodeStore());
            startingAddress.setText(String.valueOf(
                    mediator.getMachine().getStartingAddressForLoading()));
        }
    }

    /**
     * Initializes the Punctuation tab.
     */
    private void setUpPunctuationTab() {
    	
    	// Accounts for width changes.
    	leftASCIIColumn.prefWidthProperty().bind(leftPunctuationTable.widthProperty().divide(100 / 50.0));
    	leftTypeColumn.prefWidthProperty().bind(leftPunctuationTable.widthProperty().divide(100 / 50.0));
    	rightASCIIColumn.prefWidthProperty().bind(rightPunctuationTable.widthProperty().divide(100 / 50.0));
    	rightTypeColumn.prefWidthProperty().bind(rightPunctuationTable.widthProperty().divide(100 / 50.0));

    	// Making column widths adjust properly
    	leftPunctuationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    	rightPunctuationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Callbacks
        Callback<TableColumn<PunctChar, String>, TableCell<PunctChar, String>> cellStrFactory =
                new Callback<TableColumn<PunctChar, String>, TableCell<PunctChar, String>>() {
                    @Override
                    public TableCell<PunctChar, String> call(
                            TableColumn<PunctChar, String> setStringTableColumn) {
                    	EditingStrCell<PunctChar> esc = new EditingStrCell<PunctChar>();
                    	esc.setAlignment(Pos.CENTER);
                    	esc.setFont(new Font("Courier",18));
                    	return esc;
                    }
        };

        Callback<TableColumn<PunctChar, Use>, TableCell<PunctChar, Use>> cellComboFactory1 =
        		new Callback<TableColumn<PunctChar, Use>, TableCell<PunctChar, Use>>() {
        	@Override
        	public TableCell<PunctChar, Use> call(
        			TableColumn<PunctChar, Use> setStringTableColumn) {
        		return new ComboBoxTableCell<PunctChar, Use>(Use.values());
        	}
        };
        
        Callback<TableColumn<PunctChar, Use>, TableCell<PunctChar, Use>> cellComboFactory2 =
        		new Callback<TableColumn<PunctChar, Use>, TableCell<PunctChar, Use>>() {
        	@Override
        	public TableCell<PunctChar, Use> call(
        			TableColumn<PunctChar, Use> setStringTableColumn) {
        		return new ComboBoxTableCell<PunctChar, Use>(Use.values());
        	}
        };

        // Set cellValue Factory
        leftASCIIColumn.setCellValueFactory(new PropertyValueFactory<PunctChar, String>("Char"));
        leftTypeColumn.setCellValueFactory(new PropertyValueFactory<PunctChar, Use>("Use"));
        rightASCIIColumn.setCellValueFactory(new PropertyValueFactory<PunctChar, String>("Char"));
        rightTypeColumn.setCellValueFactory(new PropertyValueFactory<PunctChar, Use>("Use"));

        // Set cell factory and onEditCommit
        leftASCIIColumn.setCellFactory(cellStrFactory);
        rightASCIIColumn.setCellFactory(cellStrFactory);
        // no on edit necessry

        leftTypeColumn.setCellFactory(cellComboFactory1);
        leftTypeColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<PunctChar, Use>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<PunctChar, Use> text) {
                        ((PunctChar) text.getRowValue()).setUse(
                                text.getNewValue());
                    }
                });
        rightTypeColumn.setCellFactory(cellComboFactory2);
        rightTypeColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<PunctChar, Use>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<PunctChar, Use> text) {
                        ((PunctChar) text.getRowValue()).setUse(
                                text.getNewValue());
                    }
                });

        // Put values into table
        ObservableList<PunctChar> leftData = leftPunctuationTable.getItems();
        ObservableList<PunctChar> rightData = rightPunctuationTable.getItems();
        PunctChar[] originalPunctChars = mediator.getMachine().getPunctChars();
        int leftSize = (originalPunctChars.length)/2;
        int rightSize = originalPunctChars.length-leftSize;
        for (int i = 0; i < leftSize+rightSize; i++) {
            if (i<leftSize) {
            	leftData.add(originalPunctChars[i].copy());
            }
            else {
            	rightData.add(originalPunctChars[i].copy());
            }
        }
        leftPunctuationTable.setItems(leftData);
        rightPunctuationTable.setItems(rightData);

        // for disabling appropriately
        punctuationTab.disableProperty().bind(mediator.getDesktopController().inDebugOrRunningModeProperty());
    }

    /**
     * Initializes the IOOptions tab.
     */
    private void setUpIOOptionsTab() {

        // make initial Channels
        allChannels = FXCollections.observableArrayList(
                CPUSimConstants.CONSOLE_CHANNEL,
                CPUSimConstants.DIALOG_CHANNEL,
                CPUSimConstants.FILE_CHANNEL);
        ObservableList<Microinstruction> ios = mediator.getMachine().getMicros("io");
        for (int i = 0; i < ios.size(); i++) {
            IOChannel channel = ((IO) ios.get(i)).getConnection();
            if (channel instanceof FileChannel) {
                allChannels.add(channel);
            }
        }

        // Making column widths adjust properly
        IOOptionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Accounts for width changes.
        nameColumn.minWidthProperty().bind(ioOptionsAnchorPane.widthProperty().divide(100 / 20.0));
        connectionColumn.minWidthProperty().bind(ioOptionsAnchorPane.widthProperty().divide(100 / 79.5));

        // updates selectedSet
        IOOptionsTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<IOOptionsData>() {
                    @Override
                    public void changed(ObservableValue<? extends IOOptionsData> selected,
                                        IOOptionsData oldSet, IOOptionsData newSet) {
                        IOOptionsSelectedSet = newSet;
                        updateIOTableDisplay();
                    }
                });

        // a cellFactory for the IOChannel column
        Callback<TableColumn<IOOptionsData, IOChannel>, TableCell<IOOptionsData, IOChannel>>
                cellComboIOChannelFactory =
                new Callback<TableColumn<IOOptionsData, IOChannel>,
                        TableCell<IOOptionsData, IOChannel>>() {
                    @Override
                    public TableCell<IOOptionsData, IOChannel> call(
                            TableColumn<IOOptionsData, IOChannel> setStringTableColumn) {
                    	return new IOComboBoxTableCell<IOOptionsData, IOChannel>(
                                allChannels);
                    }
                };

        // Set cellValue Factory
        nameColumn.setCellValueFactory(new PropertyValueFactory<IOOptionsData, IO>("io"));
        connectionColumn.setCellValueFactory(
                new PropertyValueFactory<IOOptionsData, IOChannel>("channel"));

        // Set cell factory and onEditCommit, only for connection column because
        // IO column is not editable.
        connectionColumn.setCellFactory(cellComboIOChannelFactory);
        connectionColumn.setOnEditCommit(
            new EventHandler<TableColumn.CellEditEvent<IOOptionsData, IOChannel>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<IOOptionsData, IOChannel> text) {
                    if (!text.getNewValue().equals(CPUSimConstants.FILE_CHANNEL)) {
                        text.getRowValue().setChannel(text.getNewValue());
                    }
                    else {
                        FileChooser fileChooser = new FileChooser();
                        fileChooser.setTitle("Choose Data File");
                        fileChooser.setInitialDirectory(null);
                        File file = fileChooser.showOpenDialog(
                                helpButton.getScene().getWindow());

                        try {
                            if (file != null) {
                                FileChannel newFileChannel = new FileChannel(file);

                                // add it to the ComboBox if not already there
                                boolean alreadyIn = false;
                                for (IOChannel ioc : allChannels) {
                                    if (newFileChannel.toString().equals(ioc.toString())) {
                                        alreadyIn = true;
                                        newFileChannel = (FileChannel) ioc;
                                    }
                                }
                                if (!alreadyIn) {
                                    allChannels.add(newFileChannel);
                                }

                                IOOptionsSelectedSet.setChannel(newFileChannel);
                            }
                            else {
                                IOOptionsSelectedSet.setChannel(text.getOldValue());
                            }
                        } catch (Exception e) {
                        	Dialogs.showErrorDialog(((Stage) (
                                    helpButton.getScene().getWindow())),
                                    "Something went wrong while trying to " +
                                            "load the file. Please make " +
                                            "sure the file is valid and try again.",
                                    "Error while loading file.", "Error");
                            System.out.println("exception " + e);
                        }
                    }
                }
            });

        // Load in rows
        ObservableList<IOOptionsData> data = IOOptionsTable.getItems();
        for (int i = 0; i < ios.size(); i++) {
            IO channel = ((IO) ios.get(i));
            data.add(new IOOptionsData(channel, channel.getConnection()));
        }
        IOOptionsTable.setItems(data);
        
        // for disabling appropriately
        IOOptionsTab.disableProperty().bind(mediator.getDesktopController().inDebugOrRunningModeProperty());
        if (IOOptionsTab.isDisabled()) {
        	for (Tab tab : tabPane.getTabs()) {
        		if (!tab.isDisabled()) {
        			tabPane.getSelectionModel().select(tab);
        			break;
        		}
        	}
        }
    }


    /**
     * updates the values in the connection column using a hack
     */
    public void updateIOTableDisplay() {
        nameColumn.setVisible(false);
        nameColumn.setVisible(true);
        connectionColumn.setVisible(false);
        connectionColumn.setVisible(true);
    }

    //=========== inner class for IO Options tab ============

    class IOComboBoxTableCell<S, T> extends ComboBoxTableCell<S, T> {

        public IOComboBoxTableCell(ObservableList<T> items) {
            super(items);
//            this.editingProperty().addListener(new ChangeListener<Boolean>() {
//                @Override
//                public void changed(ObservableValue<? extends Boolean> arg0,
//                                    Boolean oldValue, Boolean newValue) {
//                    System.out.println("Listener: cell editing changed");
//                    if (! newValue) { // it stopped being edited
//                        T item = IOComboBoxTableCell.this.getItem();
//                        System.out.println("item no longer selected: " + item);
//                        if(item instanceof FileChannel) {
//                            String filename = ((FileChannel) item).getFile().getName();
//                            IOComboBoxTableCell.this.setText(filename);
//                        }
//                    }
//                }
//            });
        }

        @SuppressWarnings("unchecked")
		@Override
        public void updateItem(T item, boolean empty) {
            if (item != CPUSimConstants.FILE_CHANNEL ||
                    IOOptionsSelectedSet == null) {
                super.updateItem(item, empty);
                if (item instanceof FileChannel) {
                    File file = ((FileChannel) item).getFile();
                    setText(file.getName());
                    setTooltip(new Tooltip(file.toString()));
                }
                else {
                	if (item != null) { 
                		setTooltip(new Tooltip(item.toString()));
                	}
                }
            }
            else {
                //if item is FILE_CHANNEL & a row is selected,
                // just display that selected channel -- this is
                // to fix a bug in ComboBoxTableCell
                // also set a tooltip to the full path name
            	super.updateItem((T) IOOptionsSelectedSet.getChannel(), empty);
                File file = ((FileChannel)
                        IOOptionsSelectedSet.getChannel()).getFile();
                setText(file.getName());
                setTooltip(new Tooltip(file.toString()));
            }
        }
    }
    
    ////////////////////// Other Methods //////////////////////
    
    public void selectTab(int index) {
    	if (0 <= index && index <= 3) {
    		if (!tabPane.getTabs().get(index).isDisabled()) {
    			tabPane.getSelectionModel().select(index);
    		}
    	}
    }
}