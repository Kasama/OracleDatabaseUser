package br.usp.icmc.OracleManager;

import java.sql.*;

public class DatabaseModel {

	private Connection conn;
	private ResultSet rs;
	private Statement statement;
	private String username, password;
	private boolean busy = false;

	public DatabaseModel(String user, String pass){
		username = user;
		password = pass;
	}

	public boolean connect(){
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			conn = DriverManager.getConnection(
					"jdbc:oracle:thin:@grad.icmc.usp.br:15215:orcl",
					username,
					password
			);
			conn.setAutoCommit(true);
			return true;
		} catch (ClassNotFoundException e) {
			Logger.log("Failed to load Oracle Database Driver");
		} catch (SQLException e) {
			if (e.getMessage().contains("onnection")){
				Logger.log("Failed to stablish connection with the database");
			} else {
				Logger.log("Failed to login with the user/password combination");
			}
		}
		return false;
	}

	public void closeConnection() {
		try {
			if (conn != null && !conn.isClosed()){
				if (busy) closeResultSet();
				rs = null;
				statement = null;
				conn.close();
				conn = null;
			}
		} catch (SQLException e) {
			Logger.log("Failed to close connection");
		}
	}

	public boolean openResultSet(String sql){
		if (isBusy()) return false;

		try	{
			statement = conn.createStatement();
			rs = statement.executeQuery(sql);
			busy = true;
		} catch (SQLException e) {
			Logger.log("Failed to connect to the database while querying "+ sql);
		}
		return true;
	}

	public boolean openResultSetForTable(String table){
		String stm = "select * from " + table;
		return openResultSet(stm);
	}

	public boolean useResultSet(ResultSetUser<ResultSet> user){
		if (isBusy()) return false;
		if (rs != null)
			user.use(rs);
		return true;
	}

	public boolean isBusy(){
		return busy;
	}

	public boolean isNotBusy(){
		return !isBusy();
	}

	public boolean closeResultSet(){
		if (isNotBusy()) return false;
		try {
			statement.close();
			rs = null;
			busy = false;
		} catch (SQLException e) {
			Logger.log("Failed to close statement");
		}
		return true;
	}

	public void useEachRow(String table, String column, ResultSetUser<String> user){

		if (isBusy()) return;

		String sql = "select " + column + " from " + table;
		openResultSet(sql);
		try {
			while (rs.next())
				user.use(rs.getString(column.replaceAll("\\s", "").toUpperCase()));
		} catch (SQLException e) {
			Logger.log("Failed to connect to the database while querying " + table + "." + column);
		}
	}

}
