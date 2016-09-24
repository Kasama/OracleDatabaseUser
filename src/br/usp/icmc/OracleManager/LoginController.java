package br.usp.icmc.OracleManager;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController extends Controller{

	public static void show(Stage stage){
		String view = "LoginView.fxml";
		String title = "Title";
		show(stage, view, title, 0, 0);
	}

	@FXML
	protected TextField username;
	@FXML
	protected PasswordField password;
	@FXML
	protected TextArea messageField;

	private String alertStyle = "-fx-border-color: #aa1c1c";

	@FXML
	protected void initialize(){
		super.initialize();
		Logger.getLogger().addListener(messageField::setText);
		Logger.log("finished loading");
		// paint the border of the text fields that go empty
		username.setOnKeyTyped(e -> {
			if (username.getText().equals(""))
				username.setStyle(alertStyle);
			else
				username.setStyle("");
		});
		password.setOnKeyTyped(e -> {
			if (password.getText().equals(""))
				password.setStyle(alertStyle);
			else
				password.setStyle("");
		});
	}

	@FXML
	private void onLoginButtonPressed(Event e){
		DatabaseModel db = loginToDB();
		MainController.show(stage, db);
	}

	@FXML
	private void onDDLButtonPressed(Event e){
		DatabaseModel db = loginToDB();
		DDLController.show(stage, db);
	}

	@FXML
	private void onExitButtonPressed(Event e){
		stage.close();
	}

	// stablish connection to the database
	private DatabaseModel loginToDB(){

		String user = username.getText();
		String pass = password.getText();

		Logger.log("Trying to connect to database with user: " + user);

		DatabaseModel db = new DatabaseModel(user, pass);
		if (db.connect())
			return db;
		else return null;
	}
}
