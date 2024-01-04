package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;

public class Board {
  private final int size;
  private final Socket socket;
  private final BufferedReader in;
  private PrintWriter out;
  private int gameBoard[][];
  private int blackStones;  // Liczba czarnych kamieni na planszy
  private int whiteStones;  // Liczba białych kamieni na planszy
  private int blackCaptures;  // Liczba przejętych kamieni przez czarnego gracza
  private int whiteCaptures;  // Liczba przejętych kamieni przez białego gracza

  public Board(int size, Socket socket, BufferedReader in) {
    this.size = size;
    this.socket = socket;
    this.in = in;
    this.gameBoard = new int[size][size];
    initializeBoard();
    this.blackStones = 0;
    this.whiteStones = 0;
    this.blackCaptures = 0;
    this.whiteCaptures = 0;
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
      case "INSERT" -> insertStone(value);
      case "BYE" -> socket.close();
    }
  }

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

      // Wysyłamy informację do klienta o przejętym kamieniu
      MessageController.sendMessage("DELETE " + convertPosition(row) + convertPosition(col), socket); //TODO: czy nie powinno byc tutaj rekurencji?? (zeby zbijac całą grupę)
      //TODO: moze jakas metoda do tego co by sie rekurencyjnie wywoływała
      //TODO: also to wysyla row/col kamienia postawionego, nie zbijanego (nie ustalamy w sumie w ogole tutaj/nie zwracamy zbijanego)

      // Zwiększ liczbę przejętych kamieni dla odpowiedniego koloru
      if (color == 1) {
        blackCaptures++;
        whiteStones--;  // Zmniejsz liczbę białych kamieni na planszy
      } else if (color == 2) {
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

  private boolean isValidPosition(int row, int col, int[][] board) {
    // Sprawdź, czy pozycja (row, col) mieści się w zakresie planszy
    return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
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

    int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};  //TODO: czy to są wartosci dla sasiada? czy po prostu wstawia ujemne bo w sumie nie wiem (nie rozumiem tablic xd)
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

  private boolean isCapturingMove(int row, int col, int color, int[][] board) {
    // Sprawdź, czy ruch powoduje zbicie przynajmniej jednej grupy kamieni przeciwnika

    // Sprawdź otoczenie kamienia
    int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};  //TODO: is it ok???? czy nie powinno byc cos w stylu col - 1, row - 1 itd (czyli w sumie tak jak mowilem up, nie znam sie)

    for (int[] neighbor : neighbors) {
      int newRow = row + neighbor[0];
      int newCol = col + neighbor[1];

      if (isValidPosition(newRow, newCol, board) && board[newRow][newCol] != color && isGroupSurrounded(newRow, newCol, 3 - color, board)) {
        // Znaleziono grupę przeciwnika, którą można zbić
        return true;
      }
    }

    return false;
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

  private boolean isMoveAllowed(int row, int col, int color) {  //TODO: stara metoda? czy mozna wywalic ja?
    if (gameBoard[row][col] != 0) {
      return false; // Pole jest już zajęte
    }

    // Sprawdź czy ruch nie jest samobójczy
    if (isSuicidalMove(row, col, color, gameBoard)) {
      return false; // Ruch jest samobójczy
    }

    return true; // Ruch jest dozwolony
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

  private String convertPosition(int pos) { //TODO: jakby bylo cos nie tak to mozna stawic identyczna metode z clienta, ale nie sprawdzalem czy jest git
    if (pos < 10) {
      return String.valueOf(pos);
    } else {
      return String.valueOf((char) ('A' + pos - 10));
    }
  }
}
