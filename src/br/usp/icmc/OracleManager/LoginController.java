package br.usp.icmc.OracleManager;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController extends Controller{

	public static void show(Stage stage){
		String view = "LoginView.fxml";
		String title = "Title";
		show(stage, view, title);
	}

	@FXML
	private TextField username;
	@FXML
	private PasswordField password;
	@FXML
	private Button loginButton;
	@FXML
	private Button exitButton;


	@FXML
	private void onLoginButtonPressed(Event e){

		String alertStyle = "-fx-border-color: #aa1b00";
		String user = username.getText();
		String pass = password.getText();

		if (user.equals(""))
			username.setStyle(alertStyle);
		else if (pass.equals(""))
			password.setStyle(alertStyle);
		else {
			MainController.show(stage);
		}
	}

	@FXML
	private void onExitButtonPressed(Event e){
	}
}
