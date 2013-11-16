package cpusim.gui.help;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.io.IOException;
import java.net.URL;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Stack;
import cpusim.gui.desktop.DesktopController;
import cpusim.gui.help.logs.Log;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.PriorityQueue;


/**
 * @author Bilal Ahmad & Mathew Levine
 * @modified 11/15/2013
 */
public class HelpController implements Initializable {
	
	//////////////////// Fields ////////////////////
	
	@FXML private TreeView<String> treeView;
	@FXML private WebView webView;
	@FXML private Button backButton;
	@FXML private Button forwardButton;
	@FXML private Button closeButton;
	@FXML private TextField searchTF;

	
	public static final String pref = "/cpusim/gui/help/helpHTML";
	public static final String[][] nameURLPairs = {
        {"Search Results", "/cpusim/gui/help/logs/searchResults.html"},

        {"CPU Sim Help",pref+"/CPUSimHelpBlank.html"},

		// General Help Files
		{"General Help", pref+"/GeneralHelpBlank.html"},
		{"Using Help", pref+"/generalHelp/usingHelp.html"},
		{"Introduction", pref+"/generalHelp/introduction.html"},
		
		{"A Tour of CPU Sim", pref+"/generalHelp/tour/tutorial.html"},
		{"Part 1: Running CPU Sim", pref+"/generalHelp/tour/runningMachines.html"},
		{"Part 2: Creating New Machines", pref+"/generalHelp/tour/creatingNewMachines.html"},
		{"Part 3: Modifying Existing Machines", pref+"/generalHelp/tour/modifyingMachines.html"},
		
		
		// Menus
		{"Menus", pref+"/MenusBlank.html"},
		{"File Menu", pref+"/menus/file.html"},
		{"Edit Menu", pref+"/menus/edit.html"},
		{"Modify Menu", pref+"/menus/modify.html"},
		{"Execute Menu", pref+"/menus/execute.html"},
		{"Help Menu", pref+"/menus/help.html"},
		
		// Windows
		{"Windows", pref+"/windowsBlank.html"},
		{"Desktop Window", pref+"/windows/mainDisplay.html"},
		{"Machine Instruction Dialog", pref+"/windows/machineInstructions.html"},
		{"Machine Instruction Fields Dialog", pref+"/windows/fields.html"},
		{"Fetch Sequence Dialog", pref+"/windows/fetchSequence.html"},
		{"Hardware Modules Dialog", pref+"/windows/hardwareModules.html"},
        {"Hardware Modules Register Arrays Dialog", pref+"/windows/registerArrays.html"},
		{"Microinstructions Dialog", pref+"/windows/microinstructions.html"},
		{"EQU Editor", pref+"/windows/globalEQUs.html"},
		{"Preferences Dialog", pref+"/windows/preferences.html"},
		{"Options Dialog", pref+"/windows/OptionsDialog.html"},
		
		// Other Features
		{"Other Features", pref+"/otherFeaturesBlank.html"},
		{"Global EQUs", pref+"/other/globalEqus.html"},
		{"Debug Mode", pref+"/other/debugMode.html"},
		{"Keyboard Shortcuts", pref+"/other/keyboardShortcuts.html"},

		// Machine Specification
		{"Machine Specifications", pref+"/machineSpecificationsBlank.html"},
		{"Names", pref+"/specifications/names.html"},
		{"Fetch Sequence", pref+"/specifications/fetchSequence.html"},
		{"Machine Instructions", pref+"/specifications/machineInstrs.html"},
		{"Fields", pref+"/specifications/fields.html"},
		{"Hardware Modules", pref+"/specifications/hardware.html"},
		{"Microinstructions", pref+"/specifications/microInstrs.html"},
		{"Assembly Language", pref+"/assemblyLanguageBlank.html"},
		
		{"Registers", pref+"/specifications/hardware/register.html"},
		{"Register Arrays", pref+"/specifications/hardware/registerArray.html"},
		{"RAMs", pref+"/specifications/hardware/ram.html"},
		{"Condition Bits", pref+"/specifications/hardware/conditionBit.html"},
		
		{"Arithmetic", pref+"/specifications/micros/arithmetic.html"},
		{"Branch", pref+"/specifications/micros/branch.html"},
		{"Decode", pref+"/specifications/micros/decode.html"},
		{"End", pref+"/specifications/micros/end.html"},
		{"Increment", pref+"/specifications/micros/increment.html"},
		{"IO", pref+"/specifications/micros/io.html"},
		{"Logical", pref+"/specifications/micros/logical.html"},
		{"MemoryAccess", pref+"/specifications/micros/memoryAccess.html"},
		{"Set", pref+"/specifications/micros/set.html"},
		{"SetCondBit", pref+"/specifications/micros/setCondBit.html"},
		{"Shift", pref+"/specifications/micros/shift.html"},
		{"Test", pref+"/specifications/micros/test.html"},
		{"Transfer", pref+"/specifications/micros/transfer.html"},
		
		{"Syntax", pref+"/specifications/assemblyLanguage/syntax.html"},
		{"Regular Instructions", pref+"/specifications/assemblyLanguage/regularInstrs.html"},
		{".data Statements", pref+"/specifications/assemblyLanguage/dataInstrs.html"},
		{".include Statements", pref+"/specifications/assemblyLanguage/includeDirectives.html"},
		{".ascii Statements", pref+"/specifications/assemblyLanguage/asciiInstrs.html"},
		{"Macros", pref+"/specifications/assemblyLanguage/macroCalls.html"},
		{"EQUs", pref+"/specifications/assemblyLanguage/equDeclaration.html"}};

    private static final String searchResultsURL = "/cpusim/gui/help/logs/searchResults.html";

    private Stack<TreeItem<String>> backStack;
	private Stack<TreeItem<String>> forwardStack;
	private boolean selectionFromButton;
	private MultipleSelectionModel<TreeItem<String>> msm;
	private HashMap<String,String> urls;
	private TreeItem<String> previousItem;
	
	private String startingPage;
	private String appendString;
	private boolean useAppendString;
	private DesktopController desktop;


	//////////////////// Constructor and Initializer ////////////////////
	
	/**
	 * Constructor which only takes desktop controller.
	 * 
	 * @param d - The DestkopController for the current 
	 * running application.
	 */
	public HelpController(DesktopController d) {
		this(d, null, "");
	}
	
	/**
	 * Constructor which takes in DesktopController 
	 * and initial starting page ID string.
	 * 
	 * @param d - The DestkopController for the current 
	 * running application.
	 * @param startPage - The ID string for the initial 
	 * page.
	 */
	public HelpController(DesktopController d, String startPage) {
		this(d, startPage, "");
	}
	
	/**
	 * Constructor which takes in DesktopController, 
	 * initial starting page ID string, and anchor string
	 * for scrolling purposes.
	 * 
	 * @param d - The DestkopController for the current 
	 * running application.
	 * @param startPage - The ID string for the initial 
	 * page.
	 * @param as - The anchor string, for how far to scroll
	 * down the HTML page.
	 */
	public HelpController(DesktopController d, String startPage, String as) {
		desktop = d;
		startingPage = startPage;
		appendString = as;
		
		backStack = new Stack<TreeItem<String>>();
		forwardStack = new Stack<TreeItem<String>>();
		urls = new HashMap<String,String>();
		
		// Initialize Map
		for (String[] arr : nameURLPairs) {
			urls.put(arr[1], arr[0]);
			urls.put(arr[0], arr[1]);
		}
		
		useAppendString = false;

	}
	
	/**
	 * Initialize the help dialog.
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initializeTreeView();
		initializeWebView();
		initializeButtons();
		initializeSeachField();
	}

	//////////////////// Button Actions ////////////////////

	/**
	 * Called each time the back button is clicked.
	 */
	public void onBackButtonClicked() {
		selectionFromButton = true;
		TreeItem<String> item;
		try {
			item = backStack.pop();
		} catch(EmptyStackException ese) {
			// Shouldn't ever happen
			backButton.setDisable(true);
			return;
		}
		forwardStack.push(previousItem);
		forwardButton.setDisable(false);
		backButton.setDisable(backStack.isEmpty());
		msm.select(item);
	}

	/**
	 * Called each time the forward button is clicked.
	 */
	public void onForwardButtonClicked() {
		selectionFromButton = true;
		TreeItem<String> item;
		try {
			item = forwardStack.pop();
		} catch(EmptyStackException ese) {
			// Shouldn't ever happen
			forwardButton.setDisable(true);
			return;
		}
		backStack.push(previousItem);
		backButton.setDisable(false);
		forwardButton.setDisable(forwardStack.isEmpty());
		msm.select(item);
	}
	
	/**
	 * Called each time the close button is clicked.
	 */
	public void onCloseButtonClicked() {
		((Stage) (closeButton.getScene().getWindow())).close();
	}
	
	//////////////////// Initializer Helpers ////////////////////

	/**
	 * Initializes the tree view.
	 */
	public void initializeTreeView() {
		
		msm = treeView.selectionModelProperty().getValue();
		TreeItem<String> root = treeView.rootProperty().get();
		if (startingPage == null) {
			previousItem = root.getChildren().get(0).getChildren().get(1);
			msm.select(root.getChildren().get(0).getChildren().get(1));
		}
		else {
			previousItem = getItemFromString(startingPage,root);
			msm.select(previousItem);
		}

		// Seleciton listener
		treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>() {
			@Override
			public void changed(ObservableValue<? extends TreeItem<String>> selected, 
					TreeItem<String> oldSet, TreeItem<String> newSet) {
				// oldSet is null whenever an item that has children is clicked.
				// We have implemented a work-around here, but this appears to be
				// a bug in javaFX. 
				if (newSet != null) {
					if (urls.containsKey(newSet.getValue())) {
						if (!selectionFromButton) {
							backStack.add(previousItem);
							forwardStack.clear();
							backButton.setDisable(false);
							forwardButton.setDisable(true);
						}
						else {
							selectionFromButton = false;
						}
						
						WebEngine webEngine = webView.getEngine();
						URL url = getClass().getResource(urls.get(newSet.getValue()));
				        if (url != null) {
				        	if (useAppendString) { 
				        		webEngine.load(url.toExternalForm()+appendString);
				        		useAppendString = false;
				        	}
				        	else {
				        		webEngine.load(url.toExternalForm());
				        	}
				        }
				        else {
				        	System.out.println("URL for "+newSet.getValue()+
                               " is null in HelpController.InitailizeTreeView().");
				        }
					}
					else {
						WebEngine webEngine = webView.getEngine();
			        	webEngine.load(getClass().getResource(pref+"/CPUSimHelpBlank.html").toExternalForm());
			        }
				}
			}
		});

	}

	/**
	 * Initializes the WebView pane, where
	 * the help HTML pages are rendered.
	 */
	public void initializeWebView() {
		WebEngine webEngine = webView.getEngine();
		
		webEngine.getLoadWorker().stateProperty().addListener(
				new ChangeListener<State>() {
					@Override
					public void changed(ObservableValue<? extends State> arg0,
							State oldState, State newState) {
						if (newState == State.SUCCEEDED) {
							WebEngine webEngine = webView.getEngine();
							String s = webEngine.getLocation();


                            if (s.substring(s.lastIndexOf("/")+1).equals("searchResults.html"))
                                return;

							int i = webEngine.getLocation().indexOf(pref);

                            String afterPref = s.substring(i);
							if (afterPref.contains("#")) {
								afterPref = afterPref.substring(0, afterPref.indexOf("#"));
							}
							String newItemsName = urls.get(afterPref);
                            TreeItem<String> ti = getItemFromString(newItemsName, treeView.getRoot());

                            if (!previousItem.getValue().equals(ti.getValue())) {
								msm.select(ti);
							}
							previousItem = ti;
						}
					}
				});
		
		URL url = getClass().getResource("/cpusim/gui/help/helpHTML/generalHelp/introduction.html");
		if (startingPage != null) {
			url = getClass().getResource(urls.get(startingPage));
		}
		webEngine.load(url.toExternalForm()+appendString);
	}

	/**
	 * Initializes the buttons. 
	 */
	public void initializeButtons() {
		backButton.setDisable(true);
		forwardButton.setDisable(true);
	}


    /**
     *  SearchResult
     *  @author Bilal Ahmad & Matthew Levine
     *  last modified: 11/15/2013
     *
     *  SearchResult is an innerclass in HelpControler.
     *  It stores a URL and two associated fields related
     *  to the number of occurances of a key word in the
     *  page's title and body.
     *
     */
    private class SearchResult implements Comparable<SearchResult> {
        private int keyWordHits;
        private int titleHits;
        private String url;


        /**
         * Instantiates new SearchResult
         * @param url the url or uri to the html page
         * @param keyWordHits the number of hits in the body of the page
         * @param titleHits the number of hits in the title
         */
        public SearchResult(String url, int keyWordHits, int titleHits){
            this.url = url;
            this.keyWordHits = keyWordHits;
            this.titleHits = titleHits;

        }

        /**
         * Returns number of keywords in body
         * @return keywords in body
         */
        public int getKeyWordHits(){ return keyWordHits; }

        /**
         * Returns number of keywords in title
         * @return keywrords in title
         */
        public int getTitleHits(){ return titleHits; }

        /**
         * Returns the URL as a string
         * @return the URL
         */
        public String getUrl(){ return url;}

        /**
         * Compares Search Results based on number of
         * keywords in the titles, otherwise
         * defaults to the number of keywords in the body.
         * @param other other search result
         * @return result
         */
        public int compareTo(SearchResult other){
            if (titleHits!=0 || other.getTitleHits()!=0)
                return other.getTitleHits()-titleHits;
            return other.getKeyWordHits()-keyWordHits;
        }

        /**
         * Returns URL and number of keyword hits
         * @return a string representation
         */
        public String toString(){
            return "Location: "+url+"\nNumber of hits: "+keyWordHits+
                              "\nNumber of hits in title: "+titleHits+"\n";
        }

    }

	/**
	 * Initializes the search field.
	 */
	public void initializeSeachField(){
        (new HelpMenuTabCompletion(searchTF)).enableTextProcessing();

		searchTF.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
                //when enter is pressed and text field is not empty
				if (event.getCode().equals(KeyCode.ENTER) && !searchTF.getText().equals("")) {
                    //gets fields and formats them
                    String searchQuery = searchTF.getText().toLowerCase().replace(" ", "|");
                    //regex pattern to look for
                    String regex = "(^|\\s)"+searchQuery+"\\b";
                    Pattern keywordPatern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

                    //prioritized queue of the search results
                    PriorityQueue<SearchResult> results = new PriorityQueue<SearchResult>();

                    //go through all files in help menu
                    for (int i=0; i<nameURLPairs.length; i++){
                        String url = getClass().getResource(nameURLPairs[i][1]).toExternalForm().substring(5);
                        if (url.substring(url.lastIndexOf("/")+1).equals("searchResults.html"))
                            continue;

                        //read lines
                        List<String> lines = null;

                        try{
                            lines = Files.readAllLines(Paths.get(url), Charset.defaultCharset());
                        } catch (IOException e){
                            continue;
                        }

                        //read lines
                        StringBuilder builder = new StringBuilder();

                        for(String line : lines){
                            builder.append(line);
                        }
                        //page
                        String page = builder.toString();
                        //title
                        String title = page.substring(page.toLowerCase().indexOf("<title>")+7,
                                page.toLowerCase().indexOf("</title>"));
                        //keyword matchers
                        Matcher keywordMatcher = keywordPatern.matcher(page);
                        Matcher titleMatcher = keywordPatern.matcher(title);


                        int titleHits = 0;
                        int keyWordHits = 0;

                        while(titleMatcher.find()){
                            titleHits++;
                        }

                        while(keywordMatcher.find()){
                            keyWordHits++;
                        }

                        //add result to priority queue
                        results.add(new SearchResult(url, keyWordHits, titleHits));
                    }

                    //update webview
                    generateSearchResults(results);

				}
			}
		});
	}

    /**
     * Updates result page and loads it
     * @param results priority queue
     */
    private void generateSearchResults(PriorityQueue<SearchResult> results){
        //html
        String head = "<HTML>\n<body>\n"+
                            "<table>\n" +
                                "<thead>\n" +
                                    "<th> <h4> Result Number </h4> </th>\n" +
                                    "<th></th>\n"+
                                    "<th></th>\n"+
                                    "<th></th>\n"+
                                    "<th></th>\n"+
                                    "<th> <h4> Link </h4> </th>\n" +
                                "</thead>\n";
        //build table dynamically
        for (int i=0; i<6; i++){
            String entryhtml = "";
            if (results.size()>0){
                SearchResult entry = results.poll();
                String fileName = entry.url.substring(entry.url.lastIndexOf("/")+1);
                entryhtml = "<tr>\n <td> <h4> "+(i+1)+"</h4> </td>\n"+
                            "<td></td>\n"+
                            "<td></td>\n"+
                            "<td></td>\n"+
                            "<td></td>\n"+
                            "<td><h4><a href=\"file://"+entry.getUrl()+"\">"+fileName+"</a></h4></td>\n</tr>";
            }
            head = head+entryhtml;
        }

        String end = "</table> </body> </html>";
        String html = head + end;

        //gets url of search page
        URL url = getClass().getResource(searchResultsURL);

        //writes it to file
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(url.toExternalForm().substring(5)+appendString));
            writer.write(html);
            writer.close();
        } catch (IOException e){
            Log.error("Failed to parse Help HTML files.: ", e);
        }




        //reloads or loads the search page
        WebEngine webEngine = webView.getEngine();
        String s = webEngine.getLocation();
        if (s.substring(s.lastIndexOf("/")+1).equals("searchResults.html"))
            webEngine.reload();
        else
            webEngine.load(url.toExternalForm()+appendString);


    }
	
	/**
	 * Selects the tree item designated by the string.
	 * 
	 * @param treeItemString The tree item string. This must 
	 * match exactly to the name of one of the tree items.
	 * If it doesn't, nothing happens.
	 */
	public void selectTreeItem(String treeItemString) {
		TreeItem<String> ti = getItemFromString(treeItemString, treeView.getRoot());
		if (ti != null) {
			msm.select(ti);
		}
		else {
            // should never happen
			System.out.println("No Tree Item by the name: " + treeItemString
                          + " in HelpController.selectTreeItem(String)");
		}
	}
	
	/**
	 * Selects the tree item designated by the string,
	 * and scrolls to the appropriate location designated
	 * by the appendString.
	 * 
	 * @param treeItemString The tree item string. This must 
	 * match exactly to the name of one of the tree items.
	 * If it doesn't, nothing happens.
	 * @param appendString The id of the location to scroll to.
	 */
	public void selectTreeItem(String treeItemString, String appendString) {
		useAppendString = true;
		this.appendString = appendString;
		TreeItem<String> ti = getItemFromString(treeItemString, treeView.getRoot());
		if (ti != null) {
			msm.select(ti);
		}
		else {
            // should never happen
            System.out.println("No Tree Item by the name: " + treeItemString
                    + " in HelpController.selectTreeItem(String,String)");
		}
	}
	
	/**
	 * Gives the stage of the help dialog.
	 * 
	 * @return the stage of the help dialog.
	 */
	public Stage getStage() {
		return ((Stage) (closeButton.getScene().getWindow()));
	}
	
	/**
	 * Recursive method to give a node of the tree from just
	 * the name of it's value.
	 * 
	 * @param s The name of the tree item desired. Name must 
	 * be valid, returns null if not valid.
	 * @param item The TreeItem of which all children should
	 * be checked to see if their name matches.
	 * @return The node of the tree, if there is one, that 
	 * contains a value which is the same as the specified string.
	 */
	private TreeItem<String> getItemFromString(String s, TreeItem<String> item) {

        if (item != null && s != null) {
			if (item.isLeaf()) {
				return item.getValue().equals(s) ? item : null;
			}
			if (item.getValue().equals(s)) {
				return item;
			}
			Iterator<TreeItem<String>> iter = item.getChildren().iterator();
			while (iter.hasNext()) {
				TreeItem<String> next = iter.next();
				TreeItem<String> candidate = getItemFromString(s,next);
				if (candidate != null) {
					return candidate;
				}
			}
		}
		return null;
	}
	
	/**
	 * Opens a new help dialog and returns its HelpController.
	 * 
	 * @param d - The desktop controller of the current application.
	 * @return - The HelpController of the new Help Dialog.
	 */
	public static HelpController openHelpDialog(DesktopController d) {
		return openHelpDialog(d, null, "");
	}
	
	/**
	 * Opens a new help dialog and returns its HelpController.
	 * 
	 * @param d - The desktop controller of the current application.
	 * @param initialWindow - The exact ID string of the window to open initially.
	 * @return - The HelpController of the new Help Dialog.
	 */
	public static HelpController openHelpDialog(DesktopController d, String initialWindow) {
		return openHelpDialog(d, initialWindow, "");
	}
	
	/**
	 * Opens a new help dialog and returns its HelpController.
	 * 
	 * @param d - The desktop controller of the current application.
	 * @param initialWindow - The exact ID string of the window to open initially.
	 * @param appendString - The id of the location to scroll to.
	 * @return - The HelpController of the new Help Dialog.
	 */
	public static HelpController openHelpDialog(DesktopController d, String initialWindow, String appendString) {
		final HelpController helpController = new HelpController(d, initialWindow, appendString);
		FXMLLoader fxmlLoader = new FXMLLoader(helpController.getClass().getResource(
				"/cpusim/gui/help/HelpFXML.fxml"));
		fxmlLoader.setController(helpController);
        final Stage dialogStage = new Stage();
        Pane dialogRoot = null;
        try {
            dialogRoot = (Pane) fxmlLoader.load();
        } catch (IOException e) {}

        Scene dialogScene = new Scene(dialogRoot);
        dialogStage.setScene(dialogScene);
        dialogStage.initModality(Modality.NONE);
        dialogStage.setTitle("Help");
        dialogStage.show();
        
        dialogStage.setOnHidden(new EventHandler<WindowEvent>() {
        	@Override
			public void handle(WindowEvent arg0) {
        		helpController.desktop.setHelpController(null);
        	}
        });
		
        dialogStage.addEventFilter(
        		KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
        			@Override
        			public void handle(KeyEvent event) {
        				if (event.getCode().equals(KeyCode.ESCAPE)) {
        					if (dialogStage.isFocused()) {
        						dialogStage.close();
        					}
        				}
        			}
        		});
        
        return helpController;
	}
}