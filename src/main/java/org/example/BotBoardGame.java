package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class BotBoardGame {

  private final int size;
  private final Socket socket;
  private final BufferedReader in;
  private PrintWriter out;
  private int gameBoard[][];
  private int blackStones;  // Liczba czarnych kamieni na planszy
  private int whiteStones;  // Liczba białych kamieni na planszy
  private int blackCaptures;  // Liczba przejętych kamieni przez czarnego gracza
  private int whiteCaptures;  // Liczba przejętych kamieni przez białego gracza
  private int gameID;
  Bot bot;

  public BotBoardGame(int size, Socket socket, BufferedReader in, int gameID) {
    this.size = size;
    this.socket = socket;
    this.in = in;
    this.gameBoard = new int[size][size];
    this.gameID = gameID;
    initializeBoard();
    this.blackStones = 0;
    this.whiteStones = 0;
    this.blackCaptures = 0;
    this.whiteCaptures = 0;
    this.bot = new Bot(size, gameBoard);
  }

  private void initializeBoard() {
    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        gameBoard[i][j] = 0; // Wypełnienie tablicy zerami
      }
    }
  }

  public void clientHandler() {
    try {
      OutputStream output = socket.getOutputStream();
      out = new PrintWriter(output, true);

      String command;
      do {
        command = in.readLine();
        whatCommand(command);
      } while (!command.equals("BYE"));
    } catch (IOException e) {
      System.out.println("Socket closed");
    }
  }

  private void whatCommand(String command) throws IOException {
    String[] part = command.split("\\s+");
    String name = part[0];
    String value = part[1];

    System.out.println("Command: " + name);
    System.out.println("Data: " + value);

    switch (name) {
      case "INSERT" -> {
        insertStone(value);
        botMove();
      }
      case "BYE" -> socket.close();
    }
  }

  private void botMove() {
    bot.setBoard(gameBoard);
    int[] botMove = bot.makeMove();

    makeMove(botMove[0], botMove[1], 2);

    String botMoveStatus = MessageController.receiveMessage(socket);
    switch (botMoveStatus) {
      case "INSERT TRUE" -> sendBotMove(botMove);
      case "INSERT FALSE" -> botMove();
    }

  }

  private void sendBotMove(int[] botMove) {
    String rowChar = convertPosition(botMove[0]);
    String colChar = convertPosition(botMove[1]);

    String stringBotMove = (rowChar + colChar);

    gameBoard[botMove[0]][botMove[1]] = 2;

    MessageController.sendMessage(stringBotMove, socket);
  }


  private void insertStone(String value) {
    int row = getRow(value.charAt(0));
    int col = getCol(value.charAt(1));
    int color = getColor(value.charAt(2));

    makeMove(row, col, color);
  }

  private void makeMove(int row, int col, int color) {
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

      if (color == 1) {
        blackCaptures++;
        whiteStones--;
      } else {
        whiteCaptures++;
        blackStones--;
      }
    } else {
      if (isSuicidalMove(row, col, color, tempBoard)) {
        MessageController.sendMessage("INSERT FALSE", socket);
        MyLogger.logger.log(Level.INFO, "Suicidal move: " + row + col);
      } else {
        gameBoard[row][col] = color;
        MessageController.sendMessage("INSERT TRUE", socket);
        MyLogger.logger.log(Level.INFO, "Inserion ok: " + row + col);
         DatabaseConnection.saveMove(prepareStatement(color, row, col, "INSERTION"), gameID);

        if (color == 1) {
          blackStones++;
        } else if (color == 2) {
          whiteStones++;
        }
      }
    }
  }

  private boolean isGroupSurroundedDFS(int row, int col, int color, int[][] board, boolean[][] visited) {
    // Wersja DFS sprawdzająca, czy grupa kamieni o danym kolorze jest otoczona
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

  private boolean isGroupSurrounded(int row, int col, int color, int[][] board) {
    boolean[][] visited = new boolean[board.length][board[0].length];
    return isGroupSurroundedDFS(row, col, color, board, visited);
  }

  public boolean isCapturingMove(int row, int col, int color, int[][] board) {
    int opponentColor = (color == 1) ? 2 : 1;

    int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    for (int[] neighbor : neighbors) {
      int newRow = row + neighbor[0];
      int newCol = col + neighbor[1];

      if (isValidPosition(newRow, newCol, board) && board[newRow][newCol] == opponentColor) {
        if (isGroupSurrounded(newRow, newCol, opponentColor, board)) {
          captureStones(newRow, newCol, opponentColor, board);
          return true;
        }
      }
    }

    return false;
  }

  private void captureStones(int row, int col, int color, int[][] board) {
    List<String> capturedStones = new ArrayList<>();
    captureStonesDFS(row, col, color, board, new boolean[board.length][board[0].length], capturedStones);

    for (String capturedStone : capturedStones) {
      String[] position = capturedStone.split("");
      int capturedRow = getRow(position[0].charAt(0));
      int capturedCol = getCol(position[1].charAt(0));
      MessageController.sendMessage("DELETE " + capturedStone, socket);
       DatabaseConnection.saveMove(prepareStatement(color, capturedRow, capturedCol, "DELETION"), gameID);

      gameBoard[capturedRow][capturedCol] = 0;
    }

    //for (String capturedStone : capturedStones) {
    //
    //}
  }

  private void captureStonesDFS(int row, int col, int color, int[][] board, boolean[][] visited, List<String> capturedStones) {
    if (!isValidPosition(row, col, board) || visited[row][col]) {
      return;
    }

    if (board[row][col] == color) {
      visited[row][col] = true;
      capturedStones.add(convertPosition(row) + convertPosition(col));

      int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

      for (int[] neighbor : neighbors) {
        int newRow = row + neighbor[0];
        int newCol = col + neighbor[1];

        captureStonesDFS(newRow, newCol, color, board, visited, capturedStones);
      }
    }
  }

  private boolean isValidPosition(int row, int col, int[][] board) {
    return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
  }

  private boolean isSuicidalMove(int row, int col, int color, int[][] board) {
    int[][] tempBoard = copyBoard(board);

    tempBoard[row][col] = color;

    return isGroupSurrounded(row, col, color, tempBoard);
  }


  private int[][] copyBoard(int[][] original) {
    // Stwórz kopię planszy
    int[][] copy = new int[original.length][original[0].length];
    for (int i = 0; i < original.length; i++) {
      System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
    }
    return copy;
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

  private String prepareStatement(int color, int row, int col, String type) {

    String player;
    if (color == 1)
      player = "BLACK";
    else
      player = "WHITE";

    String rowChar = convertPosition(row);
    String colChar = convertPosition(col);

    String statement = player + "," + rowChar + "," + colChar + "," + type;
    MyLogger.logger.log(Level.INFO, "Prepared statement: " + statement);

    return statement;
  }

}

