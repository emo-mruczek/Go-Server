package org.example;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.logging.Level;

public class ServerApp {
  public static void main(String[] args) throws SQLException, ClassNotFoundException {

   DatabaseConnection.retrieve();

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