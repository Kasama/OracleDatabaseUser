package br.usp.icmc.OracleManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

	public boolean isConnected(){
		try {
			return !conn.isClosed();
		} catch (SQLException e) {
			Logger.log("Failed to get connection status");
			return false;
		}
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
			Logger.log("Failed to connect to the database while querying "+ sql + "\n err: " + e.getMessage());
		}
		return true;
	}

	public boolean doTransaction(String sql, LambdaUser<ResultSet> user){
		boolean ret;
		ret = openResultSet(sql);
		if (ret) ret = useResultSet(user);
		if (ret) ret = closeResultSet();
		return ret;
	}

	public boolean openResultSetForTable(String table){
		String stm = "select * from " + table;
		return openResultSet(stm);
	}

	public boolean useResultSet(LambdaUser<ResultSet> user){
		if (isNotBusy()) return false;
		if (rs != null)
			user.use(rs);
		return true;
	}

	public Map<String, String[]> getCheckInConstraint(String tableName){
		Map<String, String[]> ret = new HashMap<>();
		String sql =
				"SELECT SEARCH_CONDITION FROM user_constraints" +
						" WHERE UPPER(table_name) = UPPER('LE09cargo') AND" +
						" CONSTRAINT_TYPE = 'C'";
		doTransaction(sql, rs -> {
			try {
				while(rs.next()){
					String condition = rs.getString("SEARCH_CONDITION");
					if (!condition.toUpperCase().contains("IN")) continue;
					String[] s = condition.split(" IN ");
					String colName = s[0].toUpperCase();
					String possibilities = s[1];
					possibilities = possibilities.replaceAll("(\\(|\\)|')", "");
					String[] a = possibilities.split(",");
					ret.put(colName, a);
				}
			} catch (SQLException e) {
				Logger.log("Database communication failed");
			}
		});
		return ret;
	}

	public ArrayList<String> getConstraints(String tableName, char constraintType){
		String sql =
				"SELECT column_name FROM all_cons_columns WHERE constraint_name = (" +
					" SELECT constraint_name FROM user_constraints" +
						" WHERE UPPER(table_name) = UPPER('" + tableName + "')" +
						"AND CONSTRAINT_TYPE = '" + constraintType + "'" +
				" )";
		ArrayList<String> ret = new ArrayList<>();
		doTransaction(sql, rs -> {
			try {
				while(rs.next()){
					System.out.println("figured that '" + rs.getString(1) + "' is " + constraintType);
					ret.add(rs.getString(1));
				}
			} catch (SQLException e) {
				Logger.log("Database communication failed");
			}
		});
		return ret;
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

	public boolean useEachRow(String table, String column, LambdaUser<String> user){

		if (isBusy()) return false;

		String sql = "select " + column + " from " + table;
		doTransaction(sql, rs -> {
			try {
				while (rs.next())
					user.use(rs.getString(column.replaceAll("\\s", "").toUpperCase()));
			} catch (SQLException e) {
				Logger.log("Failed to connect to the database while querying " + table + "." + column);
			}
		});
		return true;
	}

}
