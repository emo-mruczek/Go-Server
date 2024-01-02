package org.example;

import java.io.*;
import java.net.*;
import java.util.logging.Level;

public class ServerApp {
  public static void main(String[] args) {

    try (ServerSocket serverSocket = new ServerSocket(4444)) {
      MyLogger.logger.log(Level.INFO,"Server is listening on port 4444");

      Socket socket = serverSocket.accept();
      MyLogger.logger.log(Level.INFO,"New client connected");
      new Board(socket);

    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}