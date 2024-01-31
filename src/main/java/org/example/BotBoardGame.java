package org.example;

import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.logging.Level;

public class BotBoardGame extends BoardGame{
  Bot bot; //na imie mu Andrzej

  public BotBoardGame(int size, Socket socket, int gameID) {
    super(size, socket, gameID);
    this.bot = new Bot(size, gameBoard);
  }

  @Override
  protected void whatCommand(String command) throws IOException {
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
      case "PASS" -> shouldGameFinished();
      case "FORFEIT" -> DatabaseConnection.saveWinner("WHITE", gameID);
    }
  }

  private void shouldGameFinished() {
    if(getRandomBoolean()) {
      endGame();
    } else {
      MessageController.sendMessage("NO", socket);
      botMove();
    }
  }

  private void endGame() {
    MyLogger.logger.log(Level.INFO, "Gra się skończyła");
    score = GameResultCalculator.calculateGameResult(gameBoard, whiteCaptures, blackCaptures, size);
    MyLogger.logger.log(Level.INFO, "czarny punkty: " + score[0] + "\n");
    MyLogger.logger.log(Level.INFO, "białe punkty: " + score[1] + "\n");
    MyLogger.logger.log(Level.INFO, "kto wygrał: " + score[2]);
    MessageController.sendMessage("YES", socket);
    MessageController.sendMessage(String.valueOf(score[2]), socket);
    String winner;
    if (score[2] == 1) {
      winner = "BLACK";
    } else {
      winner = "WHITE";
    }
    DatabaseConnection.saveWinner(winner, gameID);
  }

  public static boolean getRandomBoolean() {
    return Math.random() < 0.5;
  }

  private void botMove() {
    bot.setBoard(gameBoard);
    int[] botMove = bot.makeMove();
    makeMove(botMove[0], botMove[1], 2);
    String botMoveStatus = MessageController.receiveMessage(socket);
    switch (Objects.requireNonNull(botMoveStatus)) {
      case "INSERT TRUE" -> sendBotMove(botMove);
      case "INSERT FALSE" -> botMove();
    }
  }

  private void sendBotMove(int[] botMove) {
    char rowChar = Converter.convertPosition(botMove[0]);
    char colChar = Converter.convertPosition(botMove[1]);
    String stringBotMove = (rowChar + String.valueOf(colChar));
    gameBoard[botMove[0]][botMove[1]] = 2;
    MessageController.sendMessage(stringBotMove, socket);
  }
}

