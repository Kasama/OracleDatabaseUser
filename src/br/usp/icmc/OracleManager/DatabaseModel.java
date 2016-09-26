package br.usp.icmc.OracleManager;

import com.sun.deploy.util.StringUtils;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

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

    // try to connect to the database with provided user and password
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

    // close existing connection
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

    // execute given sql, leaving the statement open for further usage (db becomes busy)
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

    // passes the currently open ResultSet to user
    public boolean useResultSet(LambdaUser<ResultSet> user){
        if (isNotBusy()) return false;
        if (rs != null)
            user.use(rs);
        return true;
    }

    // closes the currently open ResultSet (db becomes not busy)
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

    // execute given sql, passing the ResultSet to `user`
    public boolean doTransaction(String sql, LambdaUser<ResultSet> user){
        boolean ret;
        ret = openResultSet(sql);
        if (ret) ret = useResultSet(user);
        if (ret) ret = closeResultSet();
        return ret;
    }

    // execute selec for given table, leaving the statement open for further usage (db becomes busy)
    public boolean openResultSetForTable(String table){
        String stm = "select * from " + table;
        return openResultSet(stm);
    }

    // generates a map of columns and their possible values
    // for every column that has a "CHECK ... IN ..." constraint there will be
    // one entry in the map, with every possible value
    // i.e. A column with CHECK COLUMN_NAME IN ('a', 'b', 'c') will produce a map
    // that contains {"COLUMN_NAME" => ['a', 'b', 'c']}
    public Map<String, String[]> getCheckInConstraint(String tableName){
        Map<String, String[]> ret = new HashMap<>();
        String sql =
                "SELECT SEARCH_CONDITION FROM user_constraints" +
                        " WHERE UPPER(table_name) = UPPER('"+tableName+"') AND" +
                        " CONSTRAINT_TYPE = 'C'";
        doTransaction(sql, rs -> {
            try {
                while(rs.next()){
                    String condition = rs.getString("SEARCH_CONDITION");
                    // ignore checks without IN
                    if (!condition.toUpperCase().contains(" IN ")) continue;
                    // split on IN, leaving the column name on s[0] and
                    // the list of possible values on s[1]
                    String[] s = condition.split(" IN ");
                    String colName = s[0].toUpperCase();
                    String possibilities = s[1];
                    // remove parens
                    possibilities = possibilities.replaceAll("(\\(|\\)|')", "");
                    String[] a = possibilities.split(",");
                    // add check to the map
                    ret.put(colName, a);
                }
            } catch (SQLException e) {
                Logger.log("Database communication failed");
            }
        });
        return ret;
    }

    public Map<String, String[]> getForeignKeyConstraints(String tableName){
        String sql =
                "SELECT" +
                        " H_ALL_COLS.COLUMN_NAME AS HOST_COL," +
                        " F_ALL_COLS.COLUMN_NAME AS FOREIGN_COL," +
                        " F_ALL_COLS.TABLE_NAME  AS FOREIGN_TABLE" +
                        " FROM ALL_CONS_COLUMNS H_ALL_COLS" +
                        " JOIN USER_CONSTRAINTS U" +
                        " ON H_ALL_COLS.CONSTRAINT_NAME = U.CONSTRAINT_NAME" +
                        " JOIN ALL_CONS_COLUMNS F_ALL_COLS" +
                        " ON F_ALL_COLS.CONSTRAINT_NAME = R_CONSTRAINT_NAME" +
                        " WHERE CONSTRAINT_TYPE = 'R' AND" +
                        " H_ALL_COLS.TABLE_NAME = UPPER('" + tableName + "')";
        // FIXME find a way to handle double foreign keys (LE09CARGO)
        Map<String, String[]> FKs = new HashMap<>();
        Map<String, String[]> ret = new HashMap<>();

        doTransaction(sql, rs -> {
            try {
                while(rs.next()) {
                    String[] foreign =
                            new String[] { rs.getString(3), rs.getString(2) };
                    FKs.put(rs.getString(1), foreign);
                }
            } catch (SQLException e) {
                Logger.log("Failed to fetch Foreign Keys");
            }
        });

//		System.out.println("FKS -----");
//		FKs.forEach((col, c) -> {
//			System.out.println("---");
//			System.out.println("col: '" + col + "'");
//			for (String s : c) {
//				System.out.println("- '" + s + "'");
//			}
//		});

        FKs.forEach((local, foreign) -> {
            String eachSQL =
                    "SELECT UNIQUE " +
                            foreign[1] + " FROM " + foreign[0] +
                            " ORDER BY " + foreign[1];
            System.out.println("running SQL: " + eachSQL);
            ArrayList<String> possible = new ArrayList<>();
            doTransaction(eachSQL, rs -> {
                try {
                    while(rs.next())
                        possible.add(rs.getString(1));
                } catch (SQLException e) {
                    Logger.log("Failed to fetch FK values " + e.getMessage());
                }
            });
            String[] values = new String[possible.size()];
            values = possible.toArray(values);
            ret.put(local, values);
        });

        return ret;
    }

    // FIXME this should get the name of the columns that have a FK or UNIQUE
    // constraint, but it breaks when ran via code
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

    // selects a given column from given table and passes each row value to user
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

    public String getDDLFor(String table) {
        String sql =
                "SELECT dbms_metadata.get_ddl('TABLE','" + table + "')" +
                        " FROM DUAL";

        final String[] ret = new String[1];
        doTransaction(sql, rs -> {
            try {
                if (rs.next())
                    ret[0] = rs.getString(1);
            } catch (SQLException e) {
                Logger.log("Failed to get DDL for table " + table);
            }
        });
        return ret[0];
    }

    private static <KEY, VALUE> void put(Map<KEY, List<VALUE>> map, KEY key, VALUE value) {
        map.compute(key, (s, strings) -> strings == null ? new ArrayList<>() : strings).add((VALUE) (value + ", "));
    }
    public Map<String, String> getPrivilegesFor(String table) {
        String sql = "SELECT grantee, privilege " +
                "FROM user_tab_privs " +
                "WHERE table_name = '" + table + "'";

        HashMap ret = new HashMap<>();
        doTransaction(sql, rs -> {
            try {
                while(rs.next()) {
                    put(ret, rs.getString("grantee"), rs.getString("privilege"));
                }
            }catch (SQLException e) {
                Logger.log("Failed to get privileges for table " + table);
            }
        });
        return ret;
    }

    public boolean insertIntoTable(String tableName, HashMap data) {
        String tableRows = StringUtils.join(data.keySet(), ", ");
        String values = StringUtils.join(data.values(), ", ");

        String sql = "INSERT INTO '" + tableName +"' " + "(" + tableRows + ")" + " VALUES " + "(" + values + ")";

        return doTransaction(sql, rs -> {});
    }
}
