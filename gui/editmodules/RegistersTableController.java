/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Changed the return value of checkValidity from a boolean to void (the functionality
 * enabled by that boolean value is now controlled by throwing ValidationException)
 * 2.) Changed the edit commit method on the name column so that it calls Validate.nameableObjects()
 * which throws a ValidationException in lieu of returning a boolean value
 * 3.) Moved rangesAreInBounds method to the Validate class and changed the return value to void
 * from boolean
 */
package cpusim.gui.editmodules;

import cpusim.Mediator;
import cpusim.Module;
import cpusim.gui.util.EditingIntCell;
import cpusim.gui.util.EditingStrCell;
import cpusim.gui.util.EditingLongCell;
import cpusim.module.Register;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * The controller for editing the Branch command in the EditMicroDialog.
 */
public class RegistersTableController
        extends ModuleController implements Initializable {
    @FXML
    TableView<Register> table;
    @FXML
    TableColumn<Register,String> name;
    @FXML TableColumn<Register,Integer> width;
    @FXML TableColumn<Register,Long> initialValue;

    private ObservableList currentModules;
    private Register prototype;
    private ConditionBitTableController bitController;

    /**
     * Constructor
     * @param mediator holds the machine and information needed
     */
    public RegistersTableController(Mediator mediator){
        super(mediator);
        this.currentModules = machine.getModule("registers");
        this.prototype = new Register("???", 16, 0);
        clones = (Module[]) createClones();

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
                "registersTable.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        for (int i = 0; i < clones.length; i++){
            table.getItems().add((Register)clones[i]);
        }
    }

    /**
     * initializes the dialog window after its root element has been processed.
     * makes all the cells editable and the use can edit the cell directly and
     * hit enter to save the changes.
     *
     * @param url the location used to resolve relative paths for the root
     *            object, or null if the location is not known.
     * @param rb  the resources used to localize the root object, or null if the root
     *            object was not localized.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        name.prefWidthProperty().bind(table.prefWidthProperty().divide(100/40.0));
        width.prefWidthProperty().bind(table.prefWidthProperty().divide(100/20.0));
        initialValue.prefWidthProperty().bind(table.prefWidthProperty().divide(100/40.0));

        Callback<TableColumn<Register,String>,TableCell<Register,String>> cellStrFactory =
                new Callback<TableColumn<Register, String>, TableCell<Register, String>>() {
                    @Override
                    public TableCell<Register, String> call(
                            TableColumn<Register, String> setStringTableColumn) {
                        return new EditingStrCell<Register>();
                    }
                };
        Callback<TableColumn<Register,Integer>,TableCell<Register,Integer>> cellIntFactory =
                new Callback<TableColumn<Register, Integer>, TableCell<Register, Integer>>() {
                    @Override
                    public TableCell<Register, Integer> call(
                            TableColumn<Register, Integer> setIntegerTableColumn) {
                        return new EditingIntCell<Register>();
                    }
                };
        Callback<TableColumn<Register,Long>,TableCell<Register,Long>> cellLongFactory =
                new Callback<TableColumn<Register, Long>, TableCell<Register, Long>>() {
                    @Override
                    public TableCell<Register, Long> call(
                            TableColumn<Register, Long> setLongTableColumn) {
                        return new EditingLongCell<Register>();
                    }
                };


        name.setCellValueFactory(new PropertyValueFactory<Register, String>("name"));
        width.setCellValueFactory(new PropertyValueFactory<Register, Integer>("width"));
        initialValue.setCellValueFactory(new PropertyValueFactory<Register, Long>("initialValue"));

        //Add for Editable Cell of each field, in String or in Integer
        name.setCellFactory(cellStrFactory);
        name.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Register, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Register, String> text) {
                        String newName = text.getNewValue();
                        String oldName = text.getOldValue();
                        ( text.getRowValue()).setName(newName);
                        try{
                            Validate.namedObjectsAreUniqueAndNonempty(table.getItems().toArray());
                        } catch (ValidationException ex){
                            (text.getRowValue()).setName(oldName);
                            updateTable();
                        }
                    }
                }
        );

        width.setCellFactory(cellIntFactory);
        width.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Register, Integer>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Register, Integer> text) {
                        ((Register)text.getRowValue()).setWidth(
                                text.getNewValue());
                    }
                }
        );

        initialValue.setCellFactory(cellLongFactory);
        initialValue.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<Register, Long>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<Register, Long> text) {
                        ((Register)text.getRowValue()).setInitialValue(
                                text.getNewValue());
                    }
                }
        );
    }

    /**
     * gets the tableview object
     * @return the tableview object
     */
    public TableView getTable() {
        return table;
    }

    /**
     * assigns the given bitController to the instance variable by that name
     * @param bitController the bitController used for this controller
     */
    public void setBitController(ConditionBitTableController bitController)
    {
        this.bitController = bitController;
    }

    /**
     * getter for prototype of the right subclass
     * @return the prototype of the subclass
     */
    public Module getPrototype()
    {
        return prototype;
    }

    /**
     * returns the clone register associated with the given original register.
     * @param original the original register to be cloned
     * @return null if there is no such clone register.
     */
    public Register getCloneOf(Register original)
    {
        Set e = assocList.keySet();
        Iterator it = e.iterator();
        while(it.hasNext())
        {
            Register clone = (Register) it.next();
            if (assocList.get(clone) == original)
                return clone;
        }
        return null;
    }

    /**
     * getter for the class object for the controller's objects
     * @return the class object
     */
    public Class getModuleClass()
    {
        return Register.class;
    }

    /**
     * getter for the current hardware module
     * @return the current hardware module
     */
    public ObservableList getCurrentModules()
    {
        return currentModules;
    }

    /**
     * returns a string of the types of the controller
     * @return a string of the types of the controller
     */
    public String toString()
    {
        return "Register";
    }

    /**
     * gets properties
     * @return an array of String representations of the
     * various properties of this type of microinstruction
     */
//    public String[] getProperties()
//    {
//        return new String[]{"name", "amount"};
//    }

    /**
     * Set the clones to the new array passed as a parameter.
     * Does not check for validity.
     *
     * @param newClones Object array containing new set of clones
     */
    public void setClones(ObservableList newClones)
    {
        Register[] branches = new Register[newClones.size()];
        for (int i = 0; i < newClones.size(); i++) {
            branches[i] = (Register) newClones.get(i);
        }
        clones = branches;
    }

    /**
     * Check validity of array of Objects' properties.
     * @param modules an array of Objects to check.
     * @return boolean denoting whether array has objects with
     * valid properties or not
     */
    public void checkValidity(ObservableList modules)
    {
        // convert the array to an array of Registers
        Register[] registers = new Register[modules.size()];

        for (int i = 0; i < modules.size(); i++) {
            registers[i] = (Register) modules.get(i);
        }

        //build up a HashMap of old registers and new widths
        HashMap table = new HashMap();
        for (int i = 0; i < registers.length; i++) {
            Register oldRegister =
                    (Register) getCurrentFromClone(registers[i]);
            if (oldRegister != null && oldRegister.getWidth() !=
                    registers[i].getWidth())
                table.put(oldRegister,
                        new Integer(registers[i].getWidth()));
        }

        // check that all names are unique and nonempty
        Validate.widthsAreInBound(registers);
        Validate.initialValueAreInbound(registers);

        Validate.registerWidthsAreOkay(bitController,registers);
        Validate.registerWidthsAreOkayForMicros(machine,table);

    }

    /**
     * returns true if new micros of this class can be created.
     */
    public boolean newModulesAreAllowed()
    {
        return true;
    }

    /**
     * get the ID of the corresponding help page
     * @return the ID of the page
     */
    public String getHelpPageID()
    {
        return "Registers";
    }

    /**
     * updates the table by removing all the items and adding all back.
     * for refreshing the display.
     */
    public void updateTable()
    {
        name.setVisible(false);
        name.setVisible(true);
        double w =  table.getWidth();
        table.setPrefWidth(w-1);
        table.setPrefWidth(w);
    }

}
