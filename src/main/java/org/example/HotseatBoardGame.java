package org.example;

import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;

public class HotseatBoardGame extends BoardGame {

  public HotseatBoardGame(int size, Socket socket, int gameID) {
    super(size, socket, gameID);
  }

  @Override
  protected void whatCommand(String command) {
    String[] part = command.split("\\s+");
    String name = part[0];
    String value = part[1];
    System.out.println("Command: " + name);
    System.out.println("Data: " + value);
    switch (name) {
      case "INSERT" -> insertStone(value);
      case "END" -> {
        MyLogger.logger.log(Level.INFO, "Gra się skończyła");
        score = GameResultCalculator.calculateGameResult(gameBoard, whiteCaptures, blackCaptures, size);
          MyLogger.logger.log(Level.INFO, "czarny punkty: " + score[0] + "\n");
          MyLogger.logger.log(Level.INFO, "białe punkty: " + score[1] + "\n");
          MyLogger.logger.log(Level.INFO, "kto wygrał: " + score[2]);
        System.out.println(Arrays.deepToString(gameBoard));
        MessageController.sendMessage(String.valueOf(score[2]), socket);
        String winner;
        if (score[2] == 1) {
          winner = "BLACK";
        } else {
          winner = "WHITE";
        }
        DatabaseConnection.saveWinner(winner, gameID);
      }
      case "FORFEIT" -> {
        MyLogger.logger.log(Level.INFO, "Gracz przyznał się do przegranej!");
        String winner;
        if (Objects.equals(value, "2")) {
          winner = "BLACK";
        } else {
          winner = "WHITE";
        }
        DatabaseConnection.saveWinner(winner, gameID);
      }
    }
  }
}
