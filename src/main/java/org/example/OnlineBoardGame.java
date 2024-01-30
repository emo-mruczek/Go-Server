package org.example;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class OnlineBoardGame extends BoardGame implements Runnable {

  private final Socket firstPlayer;
  private final Socket secondPlayer;
  private Socket currPlayer;
  private Socket currOpponent;
  int passes = 0;


  public OnlineBoardGame(Socket firstPlayer, Socket secondPlayer, int size, int gameID) {
    super(size,firstPlayer, gameID);
    this.firstPlayer = firstPlayer;
    this.secondPlayer = secondPlayer;
  }

  public void run() {
    MessageController.sendMessage("JOINED", firstPlayer);

    while (true) {
      try {
        currPlayer = firstPlayer;
        currOpponent = secondPlayer;
        String fromFirst = MessageController.receiveMessage(firstPlayer);
        if (Objects.equals(fromFirst, "BYE")) {
          currPlayer.close();
          currOpponent.close();
          System.out.println("Socket has closed!");
          return;
        } else if (Objects.equals(fromFirst, "FORFEIT")) {
          System.out.println("DUPA");
          endGame();
          return;
        }
        insertStone(fromFirst);

        currPlayer = secondPlayer;
        currOpponent = firstPlayer;
        String fromSecond = MessageController.receiveMessage(secondPlayer);
        if (Objects.equals(fromSecond, "BYE")) {
          currPlayer.close();
          currOpponent.close();
          System.out.println("Socket has closed!");
          return;
        } else if (Objects.equals(fromSecond, "FORFEIT")) {
          System.out.println("DUPA");
          endGame();
        }
        insertStone(fromSecond);
      } catch (IOException e) {
        System.out.println("Socket closed");
      }
    }
  }

  @Override
  protected void insertStone(String value) {
    if (Objects.equals(value, "PASS")) {
      passes++;
      if (passes > 1) {
        endGame();
      } else {
        MessageController.sendMessage("PASS " + "none", currOpponent);
        MessageController.sendMessage("NO", currPlayer);
      }
      return;
    }

    passes = 0;
    int row = Converter.getChar(value.charAt(0));
    int col = Converter.getChar(value.charAt(1));
    int color = getColor(value.charAt(2));

    makeMove(row, col, color);
  }

  private void endGame() {
    MessageController.sendMessage("YES", currPlayer);
    MessageController.sendMessage("PASS " + "END", currOpponent);
    MyLogger.logger.log(Level.INFO, "Gra się skończyła");
    score = GameResultCalculator.calculateGameResult(gameBoard, whiteCaptures, blackCaptures, size);
    MyLogger.logger.log(Level.INFO, "czarny punkty: " + score[0] + "\n");
    MyLogger.logger.log(Level.INFO, "białe punkty: " + score[1] + "\n");
    MyLogger.logger.log(Level.INFO, "kto wygrał: " + score[2]);
    System.out.println(Arrays.deepToString(gameBoard));
    MessageController.sendMessage(String.valueOf(score[2]), currPlayer);
    MessageController.sendMessage(String.valueOf(score[2]), currOpponent);
    String winner;
    if (score[2] == 1) {
      winner = "BLACK";
    } else {
      winner = "WHITE";
    }
    DatabaseConnection.saveWinner(winner, gameID);
  }

  @Override
  protected void makeMove(int row, int col, int color) {
    ko = 0;
    if (gameBoard[row][col] != 0) {
      MessageController.sendMessage("INSERT FALSE", currPlayer);
      MyLogger.logger.log(Level.INFO, "Field is already occupied: " + row + col);

      String anotherMove = MessageController.receiveMessage(currPlayer);
      insertStone(anotherMove);
    }

    int[][] tempBoard = copyBoard(gameBoard);

    tempBoard[row][col] = color;

    if (isCapturingMove(row, col, color, tempBoard)) {

      gameBoard[row][col] = color;
      MessageController.sendMessage("INSERT TRUE", currPlayer);
      MessageController.sendMessage(Converter.convertPosition(row) + String.valueOf(Converter.convertPosition(col)) + color, currPlayer);
      MessageController.sendMessage("INSERT TRUE", currOpponent);
      MessageController.sendMessage(Converter.convertPosition(row) + String.valueOf(Converter.convertPosition(col)) + color, currOpponent);
      MyLogger.logger.log(Level.INFO, "Stone captured opponent's stone");
      DatabaseConnection.saveMove(prepareStatement(color, row, col, "INSERTION"), gameID);
    } else {
      if (ko == 1) {
        return;
      }
      if (isSuicidalMove(row, col, color, tempBoard)) {
        MessageController.sendMessage("INSERT FALSE", currPlayer);
        String anotherMove = MessageController.receiveMessage(currPlayer);
        insertStone(anotherMove);
        MyLogger.logger.log(Level.INFO, "Suicidal move: " + row + col);
      } else {
        gameBoard[row][col] = color;
        MessageController.sendMessage("INSERT TRUE", currPlayer);
        MessageController.sendMessage(Converter.convertPosition(row) + String.valueOf(Converter.convertPosition(col)) + color, currPlayer);
        MessageController.sendMessage("INSERT TRUE", currOpponent);
        MessageController.sendMessage(Converter.convertPosition(row) + String.valueOf(Converter.convertPosition(col)) + color, currOpponent);
        MyLogger.logger.log(Level.INFO, "Insertion ok: " + row + col);
        DatabaseConnection.saveMove(prepareStatement(color, row, col, "INSERTION"), gameID);
      }
    }
    if (color == 1) {
      blackStones++;
    } else if (color == 2) {
      whiteStones++;
    }
    gameBoard3 = copyBoard(gameBoard2);
    gameBoard2 = copyBoard(gameBoard);
  }

  @Override
  public boolean isCapturingMove(int row, int col, int color, int[][] board) {
    int opponentColor = (color == 1) ? 2 : 1;
    int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    for (int[] neighbor : neighbors) {
      int newRow = row + neighbor[0];
      int newCol = col + neighbor[1];
      if (isValidPosition(newRow, newCol, board) && board[newRow][newCol] == opponentColor) {
        if (isGroupSurrounded(newRow, newCol, opponentColor, board)) {
          if (captureStonesko(newRow, newCol, opponentColor, board)) {
            captureStones(newRow, newCol, opponentColor, board);
            return true;
          } else {
            MessageController.sendMessage("INSERT FALSE", currPlayer);
            String anotherMove = MessageController.receiveMessage(currPlayer);
            insertStone(anotherMove);
            MyLogger.logger.log(Level.INFO, "Capturing move leads to previous state");
            ko = 1;
            return false;
          }
        }
      }
    }
    return false;
  }

  @Override
  protected void captureStones(int row, int col, int color, int[][] board) {
    List<String> capturedStones = new ArrayList<>();
    captureStonesDFS(row, col, color, board, new boolean[board.length][board[0].length], capturedStones);
    for (String capturedStone : capturedStones) {
      String[] position = capturedStone.split("");
      int capturedRow = Converter.getChar(position[0].charAt(0));
      int capturedCol = Converter.getChar(position[1].charAt(0));
      MessageController.sendMessage("DELETE " + capturedStone, currPlayer);
      MessageController.sendMessage("DELETE " + capturedStone, currOpponent);
      DatabaseConnection.saveMove(prepareStatement(color, capturedRow, capturedCol, "DELETION"), gameID);
      if (color == 1) {
        whiteCaptures++;
        blackStones--;
      } else {
        blackCaptures++;
        whiteStones--;
      }
      gameBoard[capturedRow][capturedCol] = 0;
    }
  }
}
