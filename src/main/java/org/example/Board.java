package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
<<<<<<< Updated upstream
=======
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
>>>>>>> Stashed changes

public class Board {
  private final int size;
  private final Socket socket;
  private final BufferedReader in;
  private PrintWriter out;

  public Board(int size, Socket socket, BufferedReader in) {
    this.size = size;
    this.socket = socket;
    this.in = in;
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

  //TODO: what commands do we need?
  private void whatCommand(String command) throws IOException {
    String[] part = command.split("\\s+");
    String name = part[0];
    String value = part[1];

    System.out.println("Command: " + name);
    System.out.println("Data: " + value);

    switch (name) {
      //case "INSERT" -> insertClicked(value);
      //case "DRAW" -> drawClicked();
      //case "DELETE" -> deleteClicked(value);
      //case "SEARCH" -> searchClicked(value);
      case "BYE" -> socket.close();
    }
  }
<<<<<<< Updated upstream
=======

  private void insertStone(String value) {
    int row = getRow(value.charAt(0));
    int col = getCol(value.charAt(1));
    int color = getColor(value.charAt(2));

    makeMove(row, col, color);
  }

  private void makeMove(int row, int col, int color) {
    if (gameBoard[row][col] != 0) {
      // Pole jest już zajęte
      MessageController.sendMessage("INSERT FALSE", socket);
      MyLogger.logger.log(Level.INFO, "Field is already occupied: " + row + col);
      return;
    }

    // Tworzymy kopię planszy, aby nie modyfikować oryginalnej planszy podczas sprawdzania
    int[][] tempBoard = copyBoard(gameBoard);

    // Postawienie tymczasowego kamienia na planszy
    tempBoard[row][col] = color;

    // Sprawdź, czy tymczasowy kamień powoduje zbicie grupy kamieni przeciwnika
    if (isCapturingMove(row, col, color, tempBoard)) {
      // Zbicje kamienia przeciwnika - ruch jest legalny
      gameBoard[row][col] = color;
      MessageController.sendMessage("INSERT TRUE", socket);
      MyLogger.logger.log(Level.INFO, "Stone captured opponent's stone");
      // Zwiększ liczbę przejętych kamieni dla odpowiedniego koloru
      if (color == 1) {
        blackCaptures++;
        whiteStones--;  // Zmniejsz liczbę białych kamieni na planszy
      } else {
        whiteCaptures++;
        blackStones--;  // Zmniejsz liczbę czarnych kamieni na planszy
      }
    } else {
      // Sprawdź czy ruch nie jest samobójczy
      if (isSuicidalMove(row, col, color, tempBoard)) {
        // Ruch jest samobójczy
        MessageController.sendMessage("INSERT FALSE", socket);
        MyLogger.logger.log(Level.INFO, "Suicidal move: " + row + col);
      } else {
        // Ruch jest dozwolony
        gameBoard[row][col] = color;
        MessageController.sendMessage("INSERT TRUE", socket);
        MyLogger.logger.log(Level.INFO, "Inserion ok: " + row + col);

        // Zwiększ liczbę kamieni dla odpowiedniego koloru
        if (color == 1) {
          blackStones++;
        } else if (color == 2) {
          whiteStones++;
        }
      }
    }
  }

  private int[][] copyBoard(int[][] original) {
    // Stwórz kopię planszy
    int[][] copy = new int[original.length][original[0].length];
    for (int i = 0; i < original.length; i++) {
      System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
    }
    return copy;
  }

  private boolean isGroupSurroundedDFS(int row, int col, int color, int[][] board, boolean[][] visited) {
    // Wersja DFS sprawdzająca, czy grupa kamieni o danym kolorze jest otoczona
    if (!isValidPosition(row, col, board) || visited[row][col]) {
      return true; // Jeśli pozycja nie jest ważna lub została już odwiedzona, zwróć true (oznacza otoczenie)
    }

    if (board[row][col] == 0) {
      return false; // Jeśli na danej pozycji nie ma kamienia, to grupa nie jest otoczona
    }

    if (board[row][col] != color) {
      return true; // Jeśli kamień ma inny kolor, zwróć true (oznacza otoczenie)
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
    // Sprawdź, czy grupa kamieni o danym kolorze jest otoczona
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
        // Sprawdź, czy grupa przeciwna jest otoczona
        if (isGroupSurrounded(newRow, newCol, opponentColor, board)) {
          // Jeśli tak, to zbij kamienie
          captureStones(newRow, newCol, opponentColor, board);
          return true;
        }
      }
    }

    return false;
  }

  private void captureStones(int row, int col, int color, int[][] board) {
    // Usunięcie zbijanych kamieni
    List<String> capturedStones = new ArrayList<>();
    captureStonesDFS(row, col, color, board, new boolean[board.length][board[0].length], capturedStones);

    for (String capturedStone : capturedStones) {
      String[] position = capturedStone.split("");
      int capturedRow = getRow(position[0].charAt(0));
      int capturedCol = getCol(position[1].charAt(0));

      // Usunięcie kamienia z gameBoard
      gameBoard[capturedRow][capturedCol] = 0;
    }

    // Wysłanie informacji o zbijeniu kamieni do klienta
    for (String capturedStone : capturedStones) {
      MessageController.sendMessage("DELETE " + capturedStone, socket);
    }
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


  // Metoda pomocnicza do sprawdzenia, czy pozycja mieści się w zakresie planszy
  private boolean isValidPosition(int row, int col, int[][] board) {
    return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
  }

  private boolean isSuicidalMove(int row, int col, int color, int[][] board) {
    // Sprawdź, czy ruch jest samobójczy, tj. czy postawienie kamienia spowoduje,
    // że grupa kamieni gracza zostanie otoczona

    // Tworzymy kopię planszy, aby nie modyfikować oryginalnej planszy podczas sprawdzania
    int[][] tempBoard = copyBoard(board);

    // Postawienie tymczasowego kamienia na planszy
    tempBoard[row][col] = color;

    // Jeśli grupa kamieni gracza zostanie otoczona, to ruch jest samobójczy
    return isGroupSurrounded(row, col, color, tempBoard);
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

>>>>>>> Stashed changes
}
