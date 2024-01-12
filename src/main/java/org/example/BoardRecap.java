package org.example;

import java.net.Socket;

public class BoardRecap {

  public BoardRecap(Socket socket) {
    String message = DatabaseConnection.retrieveGames();
    MessageController.sendMessage(message, socket);
  }
}
