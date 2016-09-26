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
	private ComboBox<String> possibleValues;
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
		return getNewColumn(columnName, content, selected, style, false);
	}

	public static ColumnController getNewColumn(String columnName, String[] content, String selected, String style, boolean editable){

		ColumnController controller = loadController();
		controller.columnName.setStyle(style);
		controller.columnName.setText(columnName);
		controller.rowContent.setVisible(false);
		controller.possibleValues.setVisible(true);
		controller.possibleValues.getItems().addAll(content);
		controller.possibleValues.getSelectionModel().select(selected);
		controller.possibleValues.setEditable(editable);
		controller.type = Type.COMBO;

		return controller;

	}

	public static ColumnController getNewColumn(String columnName, String content, String style){
		return getNewColumn(columnName, content, style, false);
	}

	public static ColumnController getNewColumn(String columnName, String content, String style, boolean editable){

		ColumnController controller = loadController();
		controller.columnName.setStyle(style);
		controller.columnName.setText(columnName);
		controller.rowContent.setText(content);
		controller.rowContent.setEditable(editable);
		controller.type = Type.TEXT;

		return controller;
	}

	public String getColumnName() {
		return columnName.getText();
	}

	public String getColumnContent() {
		if (type == Type.TEXT)
			return rowContent.getText();
		else
			return possibleValues.getSelectionModel().getSelectedItem();
	}

	public void setRowContent(String content){
		if (type == Type.TEXT)
			rowContent.setText(content);
		else {
			possibleValues.getSelectionModel().select(content);
		}
	}

	public HBox getColumn() {
		return column;
	}
}
