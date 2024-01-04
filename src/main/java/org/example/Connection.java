package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;

public class Connection {
  private final Socket socket;
  private boolean mode = false;

  public Connection(Socket socket) {
    this.socket = socket;
    run();
  }

  private void run() {
    try {
      InputStream input = socket.getInputStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(input));
      String receivedMode = in.readLine();
      String receivedType = in.readLine();

      if (Objects.equals(receivedMode, "PVC")) {
        mode = true;
      }

      MyLogger.logger.log(Level.INFO, "I've received " + receivedMode);
      MyLogger.logger.log(Level.INFO, "I've received " + receivedType);

      Board board = new Board(Integer.parseInt(receivedType), mode, socket, in);
      board.clientHandler();
    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }
}
