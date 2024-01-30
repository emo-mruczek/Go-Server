package org.example;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

class BoardGameTest {

  @Test
  void testInitializeBoard() {
    BoardGame boardGame = new ConcreteBoardGame(9, null, 1);
    boardGame.initializeBoard();
    int[][] gameBoard = boardGame.gameBoard;

    for (int i = 0; i < 9; i++) {
      for (int j = 0; j < 9; j++) {
        assertEquals(0, gameBoard[i][j]);
      }
    }
  }

  @Test
  void testMakeMove() {
    Socket socket =new Socket();
    BoardGame boardGame = new ConcreteBoardGame(9, socket, 1);
    boardGame.makeMove(3, 3, 1); // Assuming it's a valid move
    assertEquals(1, boardGame.gameBoard[3][3]);
  }

}

class ConcreteBoardGame extends BoardGame {
  public ConcreteBoardGame(int size, Socket socket, int gameID) {
    super(size, socket, gameID);
  }

  @Override
  protected void whatCommand(String command) throws IOException {
  }
}
