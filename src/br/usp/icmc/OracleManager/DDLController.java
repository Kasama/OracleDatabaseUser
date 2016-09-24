package br.usp.icmc.OracleManager;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class DDLController extends Controller {

	@FXML
	protected TextArea ddlText;

	private DatabaseModel db;

	protected void initialize(){
		super.initialize();
		ddlText.setText("eh");
	}

	public static void show(Stage stage, DatabaseModel db){
		String view = "DDLView.fxml";
		String title = "DDL Viewer";
		DDLController controller = (DDLController) show(stage, view, title, 600, 800);
		controller.lateInit(db);
	}

	private void lateInit(DatabaseModel db) {
		this.db = db;

	}
}
