package org.example;

import javax.net.ssl.X509TrustManager;
import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class ServerApp {
  public static void main(String[] args) throws SQLException {
  DatabaseConnection.prepareDatabase();

  int gameID = DatabaseConnection.saveNewGame();

  String move = "WHITE,7H";
  DatabaseConnection.saveMove(move, gameID);

  DatabaseConnection.saveWinner("BLACK", gameID);

    try (ServerSocket serverSocket = new ServerSocket(4444)) {
      MyLogger.logger.log(Level.INFO, "Server is listening on port 4444");
      while (true) {
        Socket socket = serverSocket.accept();
        MyLogger.logger.log(Level.INFO, "New client connected");
        new Connection(socket);
      }
    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }


}