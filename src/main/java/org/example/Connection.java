package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public class Connection {
  private final Socket socket;

  public Connection(Socket socket) {
    this.socket = socket;
    run();
  }

  private void run() {
    try {
      InputStream input = socket.getInputStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(input));
      String receivedType = in.readLine();

      MyLogger.logger.log(Level.INFO, "I've received " + receivedType);

      if (Objects.equals(receivedType, "RECAP")) {
        BoardRecap board = new BoardRecap();
      }
      else {

        int gameID = DatabaseConnection.saveNewGame(Integer.parseInt(receivedType));

        BoardGame board = new BoardGame(Integer.parseInt(receivedType), socket, in, gameID);
        board.clientHandler();
      }
    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
