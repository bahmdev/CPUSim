/**
 * File: GUIMain
 * Last Update: August 2013
 */
package cpusim;

import java.io.File;
import java.util.List;
import cpusim.gui.desktop.DesktopController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Dialogs;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * A class to start the GUI. This is used every time 
 * the application is used unless it is run in command line mode.
 */
public class GUIMain extends Application {
	
	/**
	 * Starts the GUI.
	 */
	@Override
	public void start(Stage stage)
    {
		List<String> params = getParameters().getRaw();
		String machineFileName = params.get(0);
		String textFileName = params.get(1);
		
		Mediator mediator = new Mediator(stage);
		DesktopController deskController = new DesktopController(mediator, stage);
		Pane mainPane = null;
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"gui/desktop/desktop.fxml"));
		fxmlLoader.setController(deskController);
        try {
            mainPane = (Pane) fxmlLoader.load();
        } catch (Exception e) {
            Dialogs.showErrorDialog(stage,
                    "The FXML resource for the Desktop could not be found.",
                    "Error finding Resource",
                    "Desktop");
            System.exit(1);
        }
        Scene mainScene = new Scene(mainPane);
		stage.setScene(mainScene);

		// Now load the text file
        if (textFileName.equals("")) {
            deskController.addDefaultTab();
        }
        else {
            deskController.open(new File(textFileName));
        }

        // load the machine
        if (machineFileName.equals("")) {
            mediator.newMachine(); // a new machine
        }
        else {
            mediator.openMachine(new File(machineFileName));
        }

        stage.show();
	}
}