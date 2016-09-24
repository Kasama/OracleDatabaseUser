package br.usp.icmc.OracleManager;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class DDLController extends Controller {

	@FXML
	protected TextArea ddlText;

	protected void initialize(){
		super.initialize();
		ddlText.setText("eh");
	}

	public static void show(Stage stage){
		String view = "DDLView.fxml";
		String title = "DDL Viewer";
		show(stage, view, title, 600, 800);
	}
}
