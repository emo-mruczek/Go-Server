package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Board {
  private final int size;
  private final Socket socket;
  private final BufferedReader in;
  private PrintWriter out;
  private int gameBoard[][];
  public Board(int size, Socket socket, BufferedReader in) {
    this.size = size;
    this.socket = socket;
    this.in = in;
    this.gameBoard = new int[size][size];
    initializeBoard();
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

  //TODO: what commands do we need?
  private void whatCommand(String command) throws IOException {
    String[] part = command.split("\\s+");
    String name = part[0];
    String value = part[1];

    System.out.println("Command: " + name);
    System.out.println("Data: " + value);

    switch (name) {
      case "INSERT" -> insertStone(value);
      //case "DRAW" -> drawClicked();
      //case "DELETE" -> deleteClicked(value);
      //case "SEARCH" -> searchClicked(value);
      case "BYE" -> socket.close();
    }
  }
  private void insertStone(String value) {
    int row = getRow(value.charAt(0));
    int col = getCol(value.charAt(1));
    int color = getColor(value.charAt(2));

    // Dodaj kamień do planszy
    gameBoard[row][col] = color;
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
}
