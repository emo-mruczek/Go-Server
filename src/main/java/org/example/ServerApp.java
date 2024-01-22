package org.example;

import java.io.*;
import java.net.*;
import java.util.logging.Level;

public class ServerApp {
  public static void main(String[] args){
  DatabaseConnection.prepareDatabase();

    try (ServerSocket serverSocket = new ServerSocket(4444)) {
      MyLogger.logger.log(Level.INFO, "Server is listening on port 4444");
      while (true) {
        Socket firstPlayer = serverSocket.accept();
        MessageController.sendMessage("FIRST", firstPlayer);
        String size = MessageController.receiveMessage(firstPlayer);
        MyLogger.logger.log(Level.INFO, "First client connected");
        Socket secondPlayer = serverSocket.accept();
        MessageController.sendMessage("SECOND", secondPlayer);
        MessageController.sendMessage(size, secondPlayer);
        MyLogger.logger.log(Level.INFO, "Second client connected");

        Connection task1 = new Connection(firstPlayer);
        Connection task2 = new Connection(secondPlayer);
        Thread t1 = new Thread(task1);
        t1.start();
        Thread t2 = new Thread(task2);
        t2.start();
      }
    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}