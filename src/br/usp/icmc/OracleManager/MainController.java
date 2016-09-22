package br.usp.icmc.OracleManager;

import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainController extends Controller {

	@FXML
	private ChoiceBox<String> tables;
	@FXML
	private TextField messageField;
	@FXML
	private VBox tableTab;
	@FXML
	private Button nextButton;

	private DatabaseModel db;
	private ArrayList<ColumnController> currentColumns = new ArrayList<>();

	@Override
	public void init() {
		db = new DatabaseModel("a8936756", "a8936756");
		db.connect();
		setupChoiceBox();
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
	}

	@FXML
	private void onNextButtonPress(Event e){
		if (!db.isBusy()) return;

		db.useResultSet(rs -> {
			try {
				if (rs.next()){
					for (int i = 1; i <= currentColumns.size(); i++){
						ColumnController row = currentColumns.get(i-1);
						row.setRowContent(rs.getString(i));
					}
				}else{
					nextButton.setDisable(true);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		});
	}

	private void onChoiceBoxChange(String newValue){
		tableTab.getChildren().clear();
		currentColumns.clear();
		nextButton.setDisable(false);
		db.closeResultSet();
		db.openResultSet(newValue);
		db.useResultSet(rs -> {
			try {
				if(rs.next()) {
					for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
						String colName = rs.getMetaData().getColumnName(i);
						String colData = rs.getString(i);
						ColumnController column =
								ColumnController.getNewColumn(colName, colData);
						currentColumns.add(column);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		List<HBox> colsList = currentColumns.stream()
				.map(ColumnController::getColumn)
				.collect(Collectors.toList());

		tableTab.getChildren().addAll(colsList);
	}

	public static void show(Stage stage){
		String view = "MainView.fxml";
		String title = "Title";
		show(stage, view, title, 600, 800);
	}
}
