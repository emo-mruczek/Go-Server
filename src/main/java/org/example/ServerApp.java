package org.example;

import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.logging.Level;

public class ServerApp {
  public static void main(String[] args) {
DatabaseConnection.prepareDatabase();
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