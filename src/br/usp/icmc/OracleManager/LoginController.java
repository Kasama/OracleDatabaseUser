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

	@FXML
	protected void initialize(){
		super.initialize();
		Logger.getLogger().addListener(messageField::setText);
		Logger.log("finished loading");
	}

	@FXML
	private void onLoginButtonPressed(Event e){

		String alertStyle = "-fx-border-color: #aa1c1c";
		String user = username.getText();
		String pass = password.getText();

		Logger.log("Trying to connect to database with user: " + user);

		if (user.equals(""))
			username.setStyle(alertStyle);
		else if (pass.equals(""))
			password.setStyle(alertStyle);
		else {
//			DatabaseModel db = new DatabaseModel(user, pass);
			MainController.show(stage, username.getText(), password.getText());
		}
	}

	@FXML
	private void onDDLButtonPressed(Event e){
		DDLController.show(stage);
	}

	@FXML
	private void onExitButtonPressed(Event e){
		stage.close();
	}
}
