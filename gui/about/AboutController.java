package cpusim.gui.about;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Dialogs;
import javafx.scene.control.Dialogs.DialogOptions;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ZoomEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class AboutController implements Initializable {
	
	@FXML private ImageView imageView;
	@FXML private WebView webView;
	@FXML private Button okButton;
	
	private int counter;
	private String[] progression = {"OK", "Hey, that's not OK", "Don't Zoom there.", "Seriously, Stop",
			"I would stop if I were you.", "You're going to make me angry.", 
			"You wouldn't like me when I'm angry.", "RAAAHHHH!!", "Now I'm \"Hulk Button!!!\"",
			"Hulk"};
	private SimpleStringProperty bodyColor;
	
	private String style = "-fx-background-radius: 30; " +
			"-fx-font: 13px \"System\"; " +
			"-fx-font-weight: bold;" +
			"-fx-text-fill: black;" +
			"-fx-padding: 0 0 0 0;" +
			"-fx-shadow-highlight-color: f0f0f0;" +
			"-fx-body-color:";
	private String defaultBodyColor = "linear-gradient(from 0% 0% to 0% 80%, #ACCDF7, #5599EF, #ACCDF7);";
	private String HULK_GREEN = "#159702";
	
	public AboutController() {
		counter = 0;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Load in image
		URL url = getClass().getResource("/cpusim/gui/about/cpusim_logo.jpg");
		imageView.setImage(new Image(url.toExternalForm()));
		
		// Set up Text in Web View
		WebEngine webEngine = webView.getEngine();
		url = getClass().getResource("/cpusim/gui/about/aboutCPUSim.html");
		webEngine.load(url.toExternalForm());
		
		bodyColor = new SimpleStringProperty(defaultBodyColor);
		okButton.styleProperty().bind((new SimpleStringProperty(style)).concat(bodyColor));
		
		Platform.runLater(new Runnable() {
			public void run() {
				((Stage) (okButton.getScene().getWindow())).setOnCloseRequest(
						new EventHandler<WindowEvent>() {
							@Override
							public void handle(WindowEvent we) {
								if (!onOkButtonClicked()) {
									we.consume();
								}
							}
				});
			}
		});
		
	}
	
	@FXML
	public boolean onOkButtonClicked() {
		if (!bodyColor.get().equals(HULK_GREEN)) {
			((Stage) (okButton.getScene().getWindow())).close();
			return true;
		} else {
			Dialogs.showErrorDialog(((Stage) (okButton.getScene().getWindow())), 
					"HULK BUTTON WILL NOT LET YOU LEAVE! You must " +
					"appease me first.", "Error: Hulk will not let you leave.", "Hulk Error", DialogOptions.OK);
			return false;
		}
	}
	
	/**
	 * An easter egg! Zoom on the empty pane around
	 * the OK button to see it work.
	 * @param ze
	 */
	public void zoomEaster(ZoomEvent ze) {
		counter++;
		if (counter == 32) {
			String s;
			for (int i = 0; i < progression.length; i++) {
				s = progression[i];
				if (okButton.getText().equals(s)) {
					int dir = (ze.getZoomFactor() < 1.0) ? 1 : -1;
					dir = ((i == 0) && (ze.getZoomFactor() > 1.0)) ? 0 : dir;
					dir = ((i == progression.length-1) && (ze.getZoomFactor() <= 1.0))
							? 0 : dir;
					String newText = progression[(i+dir)%progression.length];
					
					if (newText.equals(progression[0])) {
						okButton.setPrefWidth(160.0);
						bodyColor.set(defaultBodyColor);
					} else if(newText.equals(progression[progression.length-1])) {
						okButton.setPrefWidth(160.0);
						bodyColor.set(HULK_GREEN);
					} else {
						okButton.setPrefWidth(10*newText.length());
					}
					okButton.setText(newText);
					break;
				}
			}
			
			// Reset counter
			counter = 0;
		}
	}
}