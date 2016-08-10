package game;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

  private Connection conn;

  public Database(String aDBPath) throws ClassNotFoundException {
    Class.forName("org.sqlite.JDBC");

    try {
      String database = "jdbc:sqlite:";
      database += aDBPath;
      conn = DriverManager.getConnection(database);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    System.out.println("opened database.");
  }

  public void updateDB(String sqlStatement) {
    try {
      Statement stat = conn.createStatement();

      stat.execute(sqlStatement);

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public ResultSet askDB(String sqlStatement) {
    try {
      Statement stat = conn.createStatement();

      if (stat.execute(sqlStatement)) {
        return stat.getResultSet();
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }
}
