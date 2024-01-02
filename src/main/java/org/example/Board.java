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
}
