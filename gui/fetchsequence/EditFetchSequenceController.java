/*
 * Ben Borchard
 * 
 * Last Modified 6/4/13s
 */

/*
 * Michael Goldenberg, Jinghui Yu, and Ben Borchard modified this file on 10/27/13
 * with the following changes:
 * 
 * 1.) Removed the isAbleToClose method and instead call Validate.containsDecodeMicro
 * to check if there is a decode microinstruction in the fetch sequence before closing
 */
package cpusim.gui.fetchsequence;

import cpusim.Machine;
import cpusim.Mediator;
import cpusim.Microinstruction;
import cpusim.gui.help.HelpController;
import cpusim.gui.util.DragTreeCell;
import cpusim.util.Validate;
import cpusim.util.ValidationException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Ben Borchard
 */
public class EditFetchSequenceController implements Initializable {

    @FXML AnchorPane implementationFormatPane;
    
    @FXML TreeView microInstrTreeView;

    Mediator mediator;
    
    ObservableList<Microinstruction> micros;
                
    Microinstruction draggingMicro;
    
    public EditFetchSequenceController(Mediator mediator) {
        this.mediator = mediator;
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        micros = FXCollections.observableArrayList();
        for (Microinstruction micro : mediator.getMachine().getFetchSequence().getMicros()){
            micros.add(micro);
        }
        
        setUpMicroTableView();
        
        updateMicros();
        

        implementationFormatPane.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                event.acceptTransferModes(TransferMode.COPY);
                double localY = implementationFormatPane.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
                int index = getMicroinstrIndex(localY);
                insertMicroinstr(index);
            }
        });
        implementationFormatPane.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                /* data dropped */
                /* if there is a string data on dragboard, read it and use it */
                Dragboard db = event.getDragboard();
                String microName = db.getString().split(",")[0];
                String className = db.getString().split(",")[1];
                Microinstruction micro = null;
                for (String string : Machine.MICRO_CLASSES){
                    for (Microinstruction instr : mediator.getMachine().getMicros(string)){
                        if (instr.getName().equals(microName) && instr.getMicroClass().equals(className)){
                            micro = instr;
                        }
                    }
                }
                double localY = implementationFormatPane.sceneToLocal(event.getSceneX(), event.getSceneY()).getY();
                int index = getMicroinstrIndex(localY);
                micros.add(index, micro);
             }
        });
        implementationFormatPane.setOnDragExited(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                updateMicros();
             }
        });
        
    }
    
    @FXML
    protected void handleCancel(ActionEvent ae){
        ((Stage)implementationFormatPane.getScene().getWindow()).close();
    }
    
    @FXML
    protected void handleOkay(ActionEvent ae){
        try{
            Validate.containsDecodeMicro(micros);
            mediator.getMachine().getFetchSequence().setMicros(micros);
            mediator.setMachineDirty(true);
            ((Stage)implementationFormatPane.getScene().getWindow()).close();
        }
        catch(ValidationException ex){
            Dialogs.showErrorDialog((Stage)microInstrTreeView.getScene().getWindow(), ex.getMessage()
                    , "Fetch Sequence Error");
        }
    }

    @FXML
    protected void handleHelp(ActionEvent ae){
    	String startString = "Fetch Sequence Dialog";
    	if (mediator.getDesktopController().getHelpController() == null) {
			HelpController helpController = HelpController.openHelpDialog(
					mediator.getDesktopController(), startString);
			mediator.getDesktopController().setHelpController(helpController);
		}
		else {
			HelpController hc = mediator.getDesktopController().getHelpController();
			hc.getStage().toFront();
			hc.selectTreeItem(startString);
		}
    }
    
    public void updateMicros() {

        implementationFormatPane.getChildren().clear();
        int i = 0;
        for (final Microinstruction micro : micros){
            final Label microLabel = new Label(micro.getName());
            microLabel.setPrefWidth(implementationFormatPane.getPrefWidth());
            microLabel.setPrefHeight(20);
            microLabel.setLayoutY(i);
            microLabel.setTooltip(new Tooltip(micro.getMicroClass()));

            microLabel.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    /* drag was detected, start a drag-and-drop gesture*/
                    /* allow any transfer mode */
                    Dragboard db = microLabel.startDragAndDrop(TransferMode.ANY);

                    micros.remove(micro);
                    updateMicros();

                    ClipboardContent content = new ClipboardContent();
                    content.putString(microLabel.getText()+","+microLabel.getTooltip().getText());
                    db.setContent(content);

                    event.consume();
                }
            });

            microLabel.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    if (mouseEvent.getButton().equals(MouseButton.PRIMARY)
                            && mouseEvent.getClickCount() == 2 ){
                        ObservableList<TreeItem> list = microInstrTreeView.getRoot().getChildren();

                        for (TreeItem<String> t : list){
                            if (t.getValue().equals(micro.getMicroClass())){
                                t.setExpanded(true);
                                microInstrTreeView.scrollTo(list.indexOf(t));
                                ObservableList<TreeItem<String>> nodes = t.getChildren();

                                for (TreeItem<String> tt : nodes){
                                    if (tt.getValue().equals(micro.getName())){
                                        microInstrTreeView.getSelectionModel().select(
                                                list.indexOf(t)+nodes.indexOf(tt)+2);
                                    }

                                }
                            }
                            else {
                                t.setExpanded(false);
                            }
                        }
                    }
                }
            });


            i += 20;
            implementationFormatPane.getChildren().add(microLabel);
        }
    }

    public void setUpMicroTableView() {

        TreeItem<String> rootNode = new TreeItem<>("MicroInstructions");

        microInstrTreeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> param) {
                return new DragTreeCell(mediator,
                        (Stage)implementationFormatPane.getScene().getWindow(),
                        microInstrTreeView, getClasses() );
            }
        });

        rootNode.setExpanded(true);

        for(String microClass : Machine.MICRO_CLASSES){
            TreeItem<String> classNode = new TreeItem<>(microClass);
            for (final Microinstruction micro : mediator.getMachine().getMicros(microClass)){
                final TreeItem<String> microNode = new TreeItem<>(micro.getName());
                classNode.getChildren().add(microNode);
            }
            rootNode.getChildren().add(classNode);
        }

        microInstrTreeView.setRoot(rootNode);
    }
    
    private int getMicroinstrIndex(double localY) {
        List<Double> cutOffLocs = new ArrayList<>();
        cutOffLocs.add(0.0);
        for (Node instr : implementationFormatPane.getChildren()){
            Label label = (Label) instr;
            cutOffLocs.add(label.getLayoutY()+.5*label.getPrefHeight());
        }
        cutOffLocs.add(implementationFormatPane.getPrefHeight());
        int index = 0;
        for (int i=0; i<cutOffLocs.size()-1; i++){
            if (localY >= cutOffLocs.get(i) && localY < cutOffLocs.get(i+1)){
                index = i;
            }
        }
        return index;
    }
    
    private void insertMicroinstr(int index) {
        int i = 0;
//        currentInstr.getMicros().clear();
//        currentInstr.getMicros().add(index, new Branch("",0,new ControlUnit("", mediator.getMachine())));
        updateMicros();
        for(Node instr : implementationFormatPane.getChildren()){
            Label label = (Label)instr;
            if (i >= index){
                label.setPrefHeight(label.getPrefHeight()+20);
            }
            i++;
        }
    }

    private EditFetchSequenceController getClasses(){
        return this;
    }

}

