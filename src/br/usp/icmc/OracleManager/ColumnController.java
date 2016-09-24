package br.usp.icmc.OracleManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class ColumnController {

	private static final String PARTIAL_SELECT_VIEW = "partialSelect.fxml";

	@FXML
	private Label columnName;
	@FXML
	private TextField rowContent;
	@FXML
	private HBox column;

	public static ColumnController getNewColumn(String columnName, String content, String style){
		FXMLLoader loader = new FXMLLoader(ColumnController.class.getResource(PARTIAL_SELECT_VIEW));
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		ColumnController controller = loader.getController();
		controller.columnName.setStyle(style);
		controller.columnName.setText(columnName);
		controller.rowContent.setText(content);

		return controller;
	}

	public void setRowContent(String content){
		rowContent.setText(content);
	}

	public HBox getColumn() {
		return column;
	}
}
