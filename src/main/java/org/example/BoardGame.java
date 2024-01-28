package org.example;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public abstract class BoardGame {
  protected int size;
  protected Socket socket;
  protected int[][] gameBoard;
  protected int[][] gameBoard2;
  protected int[][] gameBoard3;
  protected int ko = 0;
  protected int blackStones;  // Liczba czarnych kamieni na planszy
  protected int whiteStones;  // Liczba białych kamieni na planszy
  protected int whiteCaptures;  // Liczba przejętych kamieni przez czarnego gracza
  protected int blackCaptures;  // Liczba przejętych kamieni przez białego gracza
  protected int gameID;
  protected int[] score;

  public BoardGame(int size, Socket socket, int gameID) {
    this.size = size;
    this.socket = socket;
    this.gameBoard = new int[size][size];
    this.gameBoard2 = new int[size][size];
    this.gameBoard3 = new int[size][size];
    this.gameID = gameID;
   // initializeBoard();
    this.blackStones = 0;
    this.whiteStones = 0;
    this.blackCaptures = 0;
    this.whiteCaptures = 0;
    initializeBoard();
  }

  protected void initializeBoard() {
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        gameBoard[i][j] = 0;
      }
    }
  }

  public void clientHandler() {
    try {
      String command;
      do {
        command = MessageController.receiveMessage(socket);
        if (Objects.equals(command, "BYE")) {
          socket.close();
          System.out.println("Socket has closed!");
          return;
        }
        whatCommand(command);
      } while (true);
    } catch (IOException e) {
      System.out.println("Socket closed");
    }
  }

  protected void whatCommand(String command) throws IOException {
  }

  protected void insertStone(String value) {
    int row = Converter.getChar(value.charAt(0));
    int col = Converter.getChar(value.charAt(1));
    int color = getColor(value.charAt(2));
    makeMove(row, col, color);
  }

  protected void makeMove(int row, int col, int color) {
    ko = 0;
    if (gameBoard[row][col] != 0) {
      MessageController.sendMessage("INSERT FALSE", socket);
      MyLogger.logger.log(Level.INFO, "Field is already occupied: " + row + col);
      return;
    }
    int[][] tempBoard = copyBoard(gameBoard);
    tempBoard[row][col] = color;
    if (isCapturingMove(row, col, color, tempBoard)) {
      gameBoard[row][col] = color;
      MessageController.sendMessage("INSERT TRUE", socket);
      MyLogger.logger.log(Level.INFO, "Stone captured opponent's stone");
      DatabaseConnection.saveMove(prepareStatement(color, row, col, "INSERTION"), gameID);
    } else {
      if (ko == 1) {
        return;
      }
      if (isSuicidalMove(row, col, color, tempBoard)) {
        MessageController.sendMessage("INSERT FALSE", socket);
        MyLogger.logger.log(Level.INFO, "Suicidal move: " + row + col);
      } else {
        gameBoard[row][col] = color;
        MessageController.sendMessage("INSERT TRUE", socket);
        MyLogger.logger.log(Level.INFO, "Inserion ok: " + row + col);
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

  protected String prepareStatement(int color, int row, int col, String type) {
    String player;
    if (color == 1)
      player = "BLACK";
    else
      player = "WHITE";
    char rowChar = Converter.convertPosition(row);
    char colChar = Converter.convertPosition(col);
    String statement = player + "," + rowChar + "," + colChar + "," + type;
    MyLogger.logger.log(Level.INFO, "Prepared statement: " + statement);
    return statement;
  }

  protected int[][] copyBoard(int[][] original) {
    // Stwórz kopię planszy
    int[][] copy = new int[original.length][original[0].length];
    for (int i = 0; i < original.length; i++) {
      System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
    }
    return copy;
  }

  private boolean isGroupSurroundedDFS(int row, int col, int color, int[][] board, boolean[][] visited) {
    if (!isValidPosition(row, col, board) || visited[row][col]) {
      return true;
    }
    if (board[row][col] == 0) {
      return false;
    }
    if (board[row][col] != color) {
      return true;
    }
    visited[row][col] = true;
    int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    boolean surrounded = true;
    for (int[] neighbor : neighbors) {
      int newRow = row + neighbor[0];
      int newCol = col + neighbor[1];
      surrounded = surrounded && isGroupSurroundedDFS(newRow, newCol, color, board, visited);
    }
    return surrounded;
  }

  protected boolean isGroupSurrounded(int row, int col, int color, int[][] board) {
    boolean[][] visited = new boolean[board.length][board[0].length];
    return isGroupSurroundedDFS(row, col, color, board, visited);
  }


  protected boolean isCapturingMove(int row, int col, int color, int[][] board) {
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
            MessageController.sendMessage("INSERT FALSE", socket);
            MyLogger.logger.log(Level.INFO, "Capturing move leads to previous state");
            ko = 1;
            return false;
          }
        }
      }
    }
    return false;
  }

  protected void captureStones(int row, int col, int color, int[][] board) {
    List<String> capturedStones = new ArrayList<>();
    captureStonesDFS(row, col, color, board, new boolean[board.length][board[0].length], capturedStones);
    for (String capturedStone : capturedStones) {
      String[] position = capturedStone.split("");
      int capturedRow = Converter.getChar(position[0].charAt(0));
      int capturedCol = Converter.getChar(position[1].charAt(0));
      MessageController.sendMessage("DELETE " + capturedStone, socket);
      if (color == 1) {
        whiteCaptures++;
        blackStones--;
      } else {
        blackCaptures++;
        whiteStones--;
      }
      DatabaseConnection.saveMove(prepareStatement(color, capturedRow, capturedCol, "DELETION"), gameID);
      gameBoard[capturedRow][capturedCol] = 0;
    }
  }

  protected boolean captureStonesko(int row, int col, int color, int[][] board) {
    List<String> capturedStones = new ArrayList<>();
    int[][] tempBoardko = copyBoard(board);
    captureStonesDFS(row, col, color, board, new boolean[board.length][board[0].length], capturedStones);
    for (String capturedStone : capturedStones) {
      String[] position = capturedStone.split("");
      int capturedRow = Converter.getChar(position[0].charAt(0));
      int capturedCol = Converter.getChar(position[1].charAt(0));
      tempBoardko[capturedRow][capturedCol] = 0;
    }
    return !Arrays.deepEquals(tempBoardko, gameBoard2) && !Arrays.deepEquals(tempBoardko, gameBoard3);
  }

  protected void captureStonesDFS(int row, int col, int color, int[][] board, boolean[][] visited, List<String> capturedStones) {
    if (!isValidPosition(row, col, board) || visited[row][col]) {
      return;
    }

    if (board[row][col] == color) {
      visited[row][col] = true;
      capturedStones.add(Converter.convertPosition(row) + String.valueOf(Converter.convertPosition(col)));
      int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
      for (int[] neighbor : neighbors) {
        int newRow = row + neighbor[0];
        int newCol = col + neighbor[1];
        captureStonesDFS(newRow, newCol, color, board, visited, capturedStones);
      }
    }
  }

  protected boolean isValidPosition(int row, int col, int[][] board) {
    return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
  }

  protected boolean isSuicidalMove(int row, int col, int color, int[][] board) {
    int[][] tempBoard = copyBoard(board);
    tempBoard[row][col] = color;
    return isGroupSurrounded(row, col, color, tempBoard);
  }
  protected int getColor(char colorChar) {
    return Character.getNumericValue(colorChar);
  }
}
