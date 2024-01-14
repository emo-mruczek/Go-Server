package org.example;

import java.net.Socket;

public class BoardRecap {

  public BoardRecap(Socket socket) {
    String gamesList = DatabaseConnection.retrieveGames();
    MessageController.sendMessage(gamesList, socket);

    String gameID = MessageController.receiveMessage(socket);
    String movesList = DatabaseConnection.retrieveMoves(Integer.parseInt(gameID));
    MessageController.sendMessage(movesList, socket);

  }
}
