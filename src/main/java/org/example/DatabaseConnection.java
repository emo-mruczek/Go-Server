package org.example;

import java.sql.*;
import java.sql.Connection;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

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
  public static int saveNewGame(int size) throws SQLException {
    int key = 0;
    ResultSet generatedKeys = null;

    String timestamp = getTimestamp();

    try (Connection con = DriverManager.getConnection(connectionString)) {
      con.setCatalog("Go");

      try (PreparedStatement insertGame = con.prepareStatement("INSERT INTO games (timestamp, size) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {

        insertGame.setString(1, timestamp);
        insertGame.setInt(2, size);
        insertGame.executeUpdate();

        generatedKeys = insertGame.getGeneratedKeys();
        generatedKeys.next();
        key = generatedKeys.getInt(1);

        MyLogger.logger.log(Level.INFO, "Saved new game with id: " + key);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return key;
  }

  public static void saveWinner(String player, int gameID) {

    try (Connection con = DriverManager.getConnection(connectionString)) {
      con.setCatalog("Go");

      try (PreparedStatement insertPlayer = con.prepareStatement("UPDATE games SET player = ? WHERE id = ?")) {
        insertPlayer.setString(1, player);
        insertPlayer.setInt(2, gameID);
        insertPlayer.executeUpdate();

        MyLogger.logger.log(Level.INFO, "Saved winner: " + player + " of a game: " + gameID);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void saveMove(String data, int gameID) {
  String[] moveData = data.split(",");

    String player = moveData[0];
    String row = moveData[1];
    String col = moveData[2];
    String type = moveData[3];

    String timestamp = getTimestamp();

    try (Connection con = DriverManager.getConnection(connectionString)) {
      con.setCatalog("Go");

      try (PreparedStatement insertGame = con.prepareStatement("INSERT INTO moves (game_id, timestamp, player, placementRow, placementCol, type) VALUES (?, ?, ?, ?, ?, ?)")) {

        insertGame.setInt(1, gameID);
        insertGame.setString(2, timestamp);
        insertGame.setString(3, player);
        insertGame.setString(4, row);
        insertGame.setString(5, col);
        insertGame.setString(6, type);
        insertGame.executeUpdate();

        MyLogger.logger.log(Level.INFO, "Saved move: " + gameID + " " + timestamp + " " + player + " " + row + col);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }


  public static void prepareDatabase() {

    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost/?user=" + username + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8")) {

      try (Statement createDbStatement = con.createStatement()) {
        createDbStatement.executeUpdate("CREATE DATABASE IF NOT EXISTS `Go`");
      }

      con.setCatalog("Go");

      try (Statement createGamesTableStatement = con.createStatement()) {
        createGamesTableStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Games (id INT AUTO_INCREMENT PRIMARY KEY, timestamp TIMESTAMP, size INT, player ENUM('BLACK', 'WHITE'))");
      }

      try (Statement createMovesTableStatement = con.createStatement()) {
        createMovesTableStatement.executeUpdate("CREATE TABLE IF NOT EXISTS Moves (id INT AUTO_INCREMENT PRIMARY KEY, game_id INT, timestamp TIMESTAMP, player ENUM('BLACK', 'WHITE'), placementRow CHAR(1), placementCol CHAR(1), type ENUM('INSERTION', 'DELETION'), FOREIGN KEY (game_id) REFERENCES Games(id))");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private static String getTimestamp() {
    ZonedDateTime timestamp = ZonedDateTime.now();

    return timestamp.format(DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss"));
  }
}






