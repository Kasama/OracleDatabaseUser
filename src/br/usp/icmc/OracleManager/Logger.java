package br.usp.icmc.OracleManager;

import java.util.ArrayList;

public class Logger {

	private static Logger logger = null;
	private ArrayList<String> log = new ArrayList<>();
	private ArrayList<LoggerSubscriber> listeners = new ArrayList<>();
	private String logHead = null;

	private Logger() {}

	public Logger clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public static Logger getLogger() {
		if (logger == null) logger = new Logger();
		return logger;
	}

	public static void log(String loggable){
		Logger.getLogger().logg(loggable);
	}

	public void logg(String loggable) {
		this.log.add(loggable);
		this.logHead = loggable;
		this.notifyObservers(loggable);
	}

	private void notifyObservers(String loggable) {
		for (LoggerSubscriber listener : listeners) {
			listener.useLog(loggable);
		}
	}

	public String getLogHead() {
		return logHead;
	}

	public String dumpLog() {
		StringBuilder sb = new StringBuilder();

		log.forEach(l -> {
			String s = l + "\n";
			sb.insert(0, s);
		});

		return sb.toString();
	}

	public void addListener(LoggerSubscriber listener) {
		listeners.add(listener);
	}
}
