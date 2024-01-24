package org.example;

import java.net.Socket;

public class OnlineGameBoard implements Runnable {

  private Socket firstPlayer;
  private Socket secondPlayer;

  private int gameBoard[][];
  private int gameID;
  private final int size;


  public OnlineGameBoard(Socket firstPlayer, Socket secondPlayer, int size, int gameID) {
    this.firstPlayer = firstPlayer;
    this.secondPlayer = secondPlayer;
    this.gameBoard = new int[size][size];
    this.gameID = gameID;
    this.size = size;

    initializeBoard();

  }

  public void run() {
    MessageController.sendMessage("JOINED", firstPlayer);

    while (true) {
      String fromFirst = MessageController.receiveMessage(firstPlayer);
      MessageController.sendMessage(fromFirst, secondPlayer);

      String fromSecond = MessageController.receiveMessage(secondPlayer);
      MessageController.sendMessage(fromSecond, firstPlayer);
    }

  }


  private void initializeBoard() {
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        gameBoard[i][j] = 0; // Wypełnienie tablicy zerami
      }
    }
  }

}
