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
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void openResultSet(String table){

		if (busy) return;

		String stm = "select * from " + table;
		try	{
			statement = conn.createStatement();
			rs = statement.executeQuery(stm);
			busy = true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void useResultSet(ResultSetUser<ResultSet> user){
		if (rs != null)
			user.use(rs);
	}

	public boolean isBusy(){
		return busy;
	}

	public void closeResultSet(){
		if (!busy) return;
		try {
			statement.close();
			busy = false;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void useEachRow(String table, String column, ResultSetUser<String> user){

		if (busy) return;

		StringBuilder builder = new StringBuilder("select ");
		builder.append(column);
		builder.append(" from ");
		builder.append(table);

		String str = "";
		try {
			str = builder.toString();
			statement = conn.createStatement();
			rs = statement.executeQuery(str);
			while (rs.next()) {
				user.use(rs.getString(column.replaceAll("\\s", "").toUpperCase()));
			}
			statement.close();
		} catch (SQLException ex) {
			//jtAreaDeStatus.setText("Erro na consulta: \"" + str + "\"");
		}
	}

}
