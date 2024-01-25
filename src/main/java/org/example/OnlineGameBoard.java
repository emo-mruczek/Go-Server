package org.example;

import java.net.Socket;
import java.util.logging.Level;

public class OnlineGameBoard implements Runnable {

  private Socket firstPlayer;
  private Socket secondPlayer;

  private Socket currPlayer;
  private Socket currOpponent;

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
      currPlayer = firstPlayer;
      currOpponent = secondPlayer;
      String fromFirst = MessageController.receiveMessage(firstPlayer);
      insertStone(fromFirst);

      currPlayer = secondPlayer;
      currOpponent = firstPlayer;
      String fromSecond = MessageController.receiveMessage(secondPlayer);
      insertStone(fromSecond);
    }

  }

  private void insertStone(String value) {
    int row = getRow(value.charAt(0));
    int col = getCol(value.charAt(1));
    int color = getColor(value.charAt(2));

    makeMove(row, col, color);
  }

  private void makeMove(int row, int col, int color) {
    //  if (gameBoard[row][col] != 0) {
    //   MessageController.sendMessage("INSERT FALSE", socket);
    //    MyLogger.logger.log(Level.INFO, "Field is already occupied: " + row + col);
    //   return;
    //  }

    //  int[][] tempBoard = copyBoard(gameBoard);

    //  tempBoard[row][col] = color;

    //   if (isCapturingMove(row, col, color, tempBoard)) {

    //    gameBoard[row][col] = color;
    //   MessageController.sendMessage("INSERT TRUE", socket);
    //   MyLogger.logger.log(Level.INFO, "Stone captured opponent's stone");
    //   DatabaseConnection.saveMove(prepareStatement(color, row, col, "INSERTION"), gameID);

    //   if (color == 1) {
    //     blackCaptures++;
    //    whiteStones--;
    //   } else {
    //     whiteCaptures++;
    //     blackStones--;
    //  }
    // } else {
    //    if (isSuicidalMove(row, col, color, tempBoard)) {
    //      MessageController.sendMessage("INSERT FALSE", socket);
    //     MyLogger.logger.log(Level.INFO, "Suicidal move: " + row + col);
    //   } else {
    gameBoard[row][col] = color;
    MessageController.sendMessage("INSERT TRUE", currPlayer);
    MessageController.sendMessage(row + String.valueOf(col) + color, currPlayer);
    MessageController.sendMessage("INSERT TRUE", currOpponent);
    MessageController.sendMessage(row + String.valueOf(col) + color, currOpponent);
    MyLogger.logger.log(Level.INFO, "Insertion ok: " + row + col);
    //    DatabaseConnection.saveMove(prepareStatement(color, row, col, "INSERTION"), gameID);

    //     if (color == 1) {
    //      blackStones++;
    //    } else if (color == 2) {
    //      whiteStones++;
    //    }
    //  }
    //  }
  }


  private void initializeBoard() {
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        gameBoard[i][j] = 0; // Wypełnienie tablicy zerami
      }
    }
  }

  private int getRow(char rowChar) {
    if (rowChar >= '0' && rowChar <= '9') {
      return rowChar - '0';
    } else {
      return rowChar - 'A' + 10;
    }
  }

  private int getCol(char colChar) {
    if (colChar >= '0' && colChar <= '9') {
      return colChar - '0';
    } else {
      return colChar - 'A' + 10;
    }
  }

  private int getColor(char colorChar) {
    return Character.getNumericValue(colorChar);
  }

  private String convertPosition(int pos) {
    if (pos < 10) {
      return String.valueOf(pos);
    } else {
      return String.valueOf((char) ('A' + pos - 10));
    }
  }


}
