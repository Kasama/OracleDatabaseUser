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
	protected void initialize() {
		super.initialize();
		Logger.getLogger().addListener(messageField::setText);
	}

	protected void lateInit(DatabaseModel db) {
		this.db = db;
		if(db.isConnected())
			setupChoiceBox();
	}

	@FXML
	private void returnToLoginScreen(){
		db.closeConnection();
		LoginController.show(stage);
	}

	private void setupChoiceBox() {
		ObservableList<String> items = tables.getItems();
		db.useEachRow("user_tables", "table_name", items::add);
		db.useEachRow("user_views", "VIEW_NAME || ' (view)'", items::add);
		db.useEachRow("user_snapshots", "table_name", elem -> {
			items.remove(elem);
			items.removeIf(e -> e.equals(elem));
			items.add(elem + " (view)");
		});
		tables.getSelectionModel().selectedIndexProperty().addListener(
				(observable, oldValue, newValue) -> {
					String selectedTable = tables.getItems().get(newValue.intValue());
					onChoiceBoxChange(selectedTable);
				}
		);
		Logger.log("Successfully loaded database tables");
	}

	@FXML
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

	private void onChoiceBoxChange(String tableName){
		tableTab.getChildren().clear();
		currentColumns.clear();
		nextButton.setDisable(false);
		db.closeResultSet();
//		ArrayList<String> PKs = db.getConstraints(tableName, 'P');
//		ArrayList<String> Uniques = db.getConstraints(tableName, 'U');

		db.openResultSetForTable(tableName);
		db.useResultSet(rs -> {
			try {
				if(rs.next()) {
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						String colName = rs.getMetaData().getColumnName(i);
						String colData = rs.getString(i);
						String style = "";
//						if ( PKs.stream()
//								.filter(pk -> pk.compareToIgnoreCase(colName) == 0)
//								.count() != 0
//						) style = "-fx-text-fill: yellow";
//						if ( Uniques.stream()
//									 .filter(u -> u.compareToIgnoreCase(colName) == 0)
//									 .count() != 0
//								) style = "-fx-text-fill: blue";

						ColumnController column =
								ColumnController.getNewColumn(colName, colData, style);
						currentColumns.add(column);
					}
				}
			} catch (SQLException e) {
				Logger.log("Database communication failed");
			}
		});
		List<HBox> colsList = currentColumns.stream()
				.map(ColumnController::getColumn)
				.collect(Collectors.toList());

		tableTab.getChildren().addAll(colsList);
	}

	public static void show(Stage stage, DatabaseModel db){
		String view = "MainView.fxml";
		String title = "Title";
		MainController controller = (MainController) show(stage, view, title, 600, 800);
		controller.lateInit(db);
	}
}
