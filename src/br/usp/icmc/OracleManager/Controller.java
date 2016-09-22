package br.usp.icmc.OracleManager;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class Controller {

	protected Stage stage;

	public static void show(Stage stage, String view){
		show(stage, view, "", 600, 800);
	}

	public static void show(Stage stage, String view, String title){
		show(stage, view, title, 0, 0);
	}
	public static void show(Stage stage, String view, String title, double height, double width){

		FXMLLoader loader = new FXMLLoader(Controller.class.getResource(view));
		Parent root = null;
		try {
			root = loader.load();
		} catch (IOException e) {
			System.err.println("A unexpected error happened");
			e.printStackTrace();
			System.exit(1);
		}

		stage.setScene(new Scene(root));
		stage.setTitle(title);
		if (height != 0 && width != 0) {
			stage.setHeight(height);
			stage.setWidth(width);
		}
		stage.show();

		Controller controller = loader.getController();
		controller.setStage(stage);
		controller.init();

	}

	public void setStage(Stage stage){
		if (this.stage == null)
			this.stage = stage;
	}

	public abstract void init();

}
