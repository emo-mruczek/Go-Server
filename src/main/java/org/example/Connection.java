package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;

public class Connection {
  private final Socket socket;
  private final ServerSocket serverSocket;

  public Connection(Socket socket, ServerSocket serverSocket) {
    this.socket = socket;
    this.serverSocket = serverSocket;
    run();
  }

  private void run() {
    try {
      InputStream input = socket.getInputStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(input));
      String receivedType = in.readLine();

      MyLogger.logger.log(Level.INFO, "I've received " + receivedType);

      if (Objects.equals(receivedType, "RECAP")) {
        BoardRecap board = new BoardRecap(socket);
      } else if (Objects.equals(receivedType, "ONLINE")) {
        MessageController.sendMessage("FIRST", socket);
        String size = MessageController.receiveMessage(socket);

        Socket secondSocket = serverSocket.accept();
        System.out.println(MessageController.receiveMessage(secondSocket));
        MessageController.sendMessage("SECOND", secondSocket);
        MessageController.sendMessage(size, secondSocket);

        int gameID = DatabaseConnection.saveNewGame(Integer.parseInt(size));

        System.out.println("OK!");
        OnlineBoardGame task = new OnlineBoardGame(socket, secondSocket, Integer.parseInt(size), gameID);
        Thread t1 = new Thread(task);
        t1.start();
      } else if (Objects.equals(receivedType, "BOT")) {
        String size = MessageController.receiveMessage(socket);
        int gameID = DatabaseConnection.saveNewGame(Integer.parseInt(size));

        BotBoardGame board = new BotBoardGame(Integer.parseInt(size), socket,in, gameID);
        board.clientHandler();
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
