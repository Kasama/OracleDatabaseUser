package br.usp.icmc.OracleManager;

import javafx.scene.control.TextArea;

public class LoggerListener implements LoggerSubscriber{

	TextArea txt;

	public LoggerListener(TextArea txt){
		this.txt = txt;
	}

	@Override
	public void useLog(String loggable) {
		txt.setText(loggable);
	}
}
