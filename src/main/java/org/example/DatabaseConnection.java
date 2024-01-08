package org.example;

import java.sql.*;
import java.sql.Connection;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseConnection {

  //change if needed
  private static final String username = "root";
  private static final String password = "qwerty";
  private static final String databaseName = "Go";
  private static final String connectionString = "jdbc:mysql://localhost/" + databaseName + "?user=" + username + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8";

  //TODO: retriving (cuz below code is just for testing purposes)
  public static void retrieve() {

    try (Connection con = DriverManager.getConnection(connectionString)) {

      con.setCatalog("Go");

      DatabaseMetaData metaData = con.getMetaData();

      ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});

      System.out.println("Tables in the database:");
      while (tables.next()) {
        String tableName = tables.getString("TABLE_NAME");
        System.out.println(tableName);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }


  //TODO: ??? if u want, player
  public static int saveNewGame() throws SQLException {
    int key = 0;
    ResultSet generatedKeys = null;
    try (Connection con = DriverManager.getConnection(connectionString)) {
      con.setCatalog("Go");

      try (PreparedStatement insertGame = con.prepareStatement("INSERT INTO Games (timestamp) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
        ZonedDateTime timestamp = ZonedDateTime.now();
        String formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"));

        insertGame.setString(1, formattedTimestamp);
        insertGame.executeUpdate();

        generatedKeys = insertGame.getGeneratedKeys();
        generatedKeys.next();
        key = generatedKeys.getInt(1);
        //key = generatedKeys.getInt(1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return key;
  }

  private static void saveToMoves(String data) {
  String[] moveData = data.split(",");
  //TODO: getting game id
 // String game_id =
    String timestamp = moveData[0];
    String player = moveData[1];
    String placement = moveData[2];

  }


  public static void prepareDatabase() {

    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost/?user=" + username + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8")) {

      try (Statement createDbStatement = con.createStatement()) {
        createDbStatement.executeUpdate("CREATE DATABASE IF NOT EXISTS `Go`");
      }

      con.setCatalog("Go");

      try (Statement createGamesTableStatement = con.createStatement()) {
        createGamesTableStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Games (id INT AUTO_INCREMENT PRIMARY KEY, timestamp TIMESTAMP, player ENUM('BLACK', 'WHITE'))");
      }

      try (Statement createMovesTableStatement = con.createStatement()) {
        createMovesTableStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Moves (id INT AUTO_INCREMENT PRIMARY KEY, game_id INT, timestamp TIMESTAMP, player ENUM('BLACK', 'WHITE'), placement CHAR(2), FOREIGN KEY (game_id) REFERENCES Games(id))");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}






