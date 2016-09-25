package br.usp.icmc.OracleManager;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainController extends Controller {

	@FXML
	protected ChoiceBox<String> tables;
	@FXML
	protected TextArea messageField;
	@FXML
	protected VBox tableTab;
	@FXML
	protected Button nextButton;

	private DatabaseModel db;
	private ArrayList<ColumnController> currentColumns = new ArrayList<>();

	@FXML
	// initialize the scene
	protected void initialize() {
		super.initialize();
		Logger.getLogger().addListener(messageField::setText);
	}

	// initialize the database
	protected void lateInit(DatabaseModel db) {
		this.db = db;
		if(db.isConnected())
			setupChoiceBox();
	}

	@FXML
	// go back to the previous scene
	private void returnToLoginScreen(){
		db.closeConnection();
		LoginController.show(stage);
	}

	// populate choice box with the name of all tables in the database
	private void setupChoiceBox() {
		ObservableList<String> items = tables.getItems();
		// get all tables
		db.useEachRow("user_tables", "table_name", items::add);
		// get all views
		db.useEachRow("user_views", "VIEW_NAME || ' (view)'", items::add);
		// get all materialized views
		db.useEachRow("user_snapshots", "table_name", elem -> {
			items.remove(elem);
			items.removeIf(e -> e.equals(elem));
			items.add(elem + " (view)");
		});
		// set a listener on the choice box to populate the main area with
		// contents from the table
		tables.getSelectionModel().selectedIndexProperty().addListener(
				(observable, oldValue, newValue) -> {
					String selectedTable = tables.getItems().get(newValue.intValue());
					onChoiceBoxChange(selectedTable); // update main area
				}
		);
		Logger.log("Successfully loaded database tables");
	}

	@FXML
	// Show next record on current table
	private void onNextButtonPress(Event e){

		boolean ret = db.useResultSet(rs -> {
			try {
				if (rs.next()){
					for (int i = 1; i <= currentColumns.size(); i++){
						ColumnController row = currentColumns.get(i-1);
						row.setRowContent(rs.getString(i));
					}
				}else{
					nextButton.setDisable(true);
					db.closeResultSet();
				}
			} catch (SQLException e1) {
				Logger.log("Could not fetch ResultSet");
			}
		});

		if (!ret) Logger.log("Problem while trying to advance to next");
	}

	// populate main area with information about the current selected table
	private void onChoiceBoxChange(String tableName){
		// strip out the (view) identifier in tableName
		tableName = tableName.replaceAll("\\s*\\(\\s*view\\s*\\)\\s*", "");
		// clean whatever was already there
		tableTab.getChildren().clear();
		currentColumns.clear();
		nextButton.setDisable(false);
		db.closeResultSet();
		ArrayList<String> PKs = db.getConstraints(tableName, 'P');
		PKs.forEach(System.out::println);
		ArrayList<String> Uniques = db.getConstraints(tableName, 'U');

		// get a map containing all possibilities for each checked column
		// i.e. CHECK COLUMN IN (1,2,3) would return the map {"COLUMN" => ["1","2","3"]}
		Map<String, String[]> checks = db.getCheckInConstraint(tableName);
//		checks.forEach((col, c) -> {
//			System.out.println("---");
//			System.out.println("col: '" + col + "'");
//			for (String s : c) {
//				System.out.println("- '" + s + "'");
//			}
//		});

		db.openResultSetForTable(tableName);
		db.useResultSet(rs -> {
			try {
				if(rs.next()) {
					// for each column, create a new ColumnController with its info
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						String colName = rs.getMetaData().getColumnName(i);
						String colData = rs.getString(i);
						String style = "";
						if (PKs.contains(colName))
							style = "-fx-text-fill: #939300";
						if (Uniques.contains(colName))
							style = "-fx-text-fill: #000083";
//						this.nextButton.setStyle(style);
						ColumnController column;
						String[] poss;
						// checks if there is a 'CHECK' constraint for this column
						if ((poss = checks.get(colName)) != null){
							column = ColumnController.getNewColumn(
									colName, poss, colData, style
							);
						}else {
							column = ColumnController.getNewColumn(
									colName, colData, style
							);
						}
						// and add it to the list of columns
						currentColumns.add(column);
					}
				}
			} catch (SQLException e) {
				Logger.log("Database communication failed");
			}
		});
		// collect HBoxes from list of columns to visually add
		List<HBox> colsList = currentColumns.stream()
				.map(ColumnController::getColumn)
				.collect(Collectors.toList());

		tableTab.getChildren().addAll(colsList);
	}

	public static void show(Stage stage, DatabaseModel db){
		String view = "MainView.fxml"; // name of the view fxml
		//TODO change title
		String title = "Title"; // screen title
		// initialize scene
		MainController controller = (MainController) show(stage, view, title, 600, 800);
		// setup late stuff
		controller.lateInit(db);
	}
}
