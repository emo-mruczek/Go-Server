package org.example;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.*;
import java.sql.Connection;

public class DatabaseConnection {

  //change if needed
  static String username = "root";
  static String password = "qwerty";
  static String databaseName = "Go";

  public static void retrieve()  {

    try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost/" + databaseName + "?user=" + username + "&password=" + password + "&useUnicode=true&characterEncoding=UTF-8")) {

      // Use the 'Go Database'
      con.setCatalog("Go");

      DatabaseMetaData metaData = con.getMetaData();

      // Get table information
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


    public static void save(String type, String data) {

    System.out.println(type + ": " + data);
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






