package br.usp.icmc.OracleManager;

public class Helper {
	public static String getViewName(String controllerName){
		String ret = controllerName.replace("Controller", "View.fxml");
		return ret;
	}
}
