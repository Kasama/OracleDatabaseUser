package br.usp.icmc.OracleManager;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.ArrayList;

public class DDLController extends Controller {

	@FXML
	protected TextArea ddlText;

	private DatabaseModel db;

	protected void initialize(){
		super.initialize();
	}

	@FXML
	private void copyButtonPressed(Event e){
		if (ddlText.getSelectedText().equals("")) {
			ddlText.selectAll();
			ddlText.copy();
			ddlText.deselect();
		} else ddlText.copy();
	}

	@FXML
	private void exitButtonPressed(Event e){
		db.closeConnection();
		LoginController.show(stage);
	}

	public static void show(Stage stage, DatabaseModel db){
		String view = "DDLView.fxml";
		String title = "DDL Viewer";
		DDLController controller = (DDLController) show(stage, view, title, 600, 800);
		controller.lateInit(db);
	}

	private void lateInit(DatabaseModel db) {
		this.db = db;

		ArrayList<String> tables = new ArrayList<>();
		// get all tables
		db.useEachRow("user_tables", "table_name", tables::add);

		tables.forEach(table -> ddlText.appendText(db.getDDLFor(table)));
	}
}
