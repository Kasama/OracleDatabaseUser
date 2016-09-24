package br.usp.icmc.OracleManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
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
	private ComboBox<String> checkPossibilities;
	@FXML
	private HBox column;

	private enum Type {TEXT, COMBO}

	private Type type = Type.TEXT;

	public static ColumnController loadController(){
		FXMLLoader loader = new FXMLLoader(ColumnController.class.getResource(PARTIAL_SELECT_VIEW));
		try {
			loader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return loader.getController();
	}

	public static ColumnController getNewColumn(String columnName, String[] content, String selected, String style){

		ColumnController controller = loadController();
		controller.columnName.setStyle(style);
		controller.columnName.setText(columnName);
		controller.rowContent.setVisible(false);
		controller.checkPossibilities.setVisible(true);
		controller.checkPossibilities.getItems().addAll(content);
		controller.checkPossibilities.getSelectionModel().select(selected);
		controller.type = Type.COMBO;

		return controller;

	}

	public static ColumnController getNewColumn(String columnName, String content, String style){

		ColumnController controller = loadController();
		controller.columnName.setStyle(style);
		controller.columnName.setText(columnName);
		controller.rowContent.setText(content);
		controller.type = Type.TEXT;

		return controller;
	}

	public void setRowContent(String content){
		if (type == Type.TEXT)
			rowContent.setText(content);
		else {
			checkPossibilities.getSelectionModel().select(content);
		}
	}

	public HBox getColumn() {
		return column;
	}
}
