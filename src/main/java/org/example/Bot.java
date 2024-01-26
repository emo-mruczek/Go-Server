package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Bot {

  private int boardSize;
  private int[][] board;
  private int[] prevMove = {0,0};

  public Bot(int boardSize, int[][] board) {
    this.boardSize = boardSize;
    this.board = board;
  }

  public void setBoard(int[][] board) {
    this.board = board;
  }

  public int[] makeMove() {

    // Prioritize capturing opponent stones
    int[] captureMove = prioritizeCapture();
    if (captureMove != null && !Arrays.equals(captureMove, prevMove)) {
      prevMove = captureMove;
      return captureMove;
    }

    // If no capture move, prioritize defending own stones
    int[] defendMove = prioritizeDefense();
    if (defendMove != null && !Arrays.equals(defendMove, prevMove)) {
      prevMove = defendMove;
      return defendMove;
    }

    // If no capturing or defending move, play near own stones
    int[] strategicMove = prioritizeStrategic();
    if (strategicMove != null && !Arrays.equals(strategicMove, prevMove)) {
      prevMove = strategicMove;
      return strategicMove;
    }

    // If no strategic move, play randomly
    return playRandomMove();
  }

  private int[] prioritizeCapture() {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board[i][j] == 0) {
          // Check if placing a stone captures opponent stones
          if (isCaptureMove(i, j, 2)) {
            return new int[]{i, j};
          }
        }
      }
    }
    return null;
  }

  private int[] prioritizeDefense() {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board[i][j] == 0) {
          // Check if placing a stone defends own stones from capture
          if (isCaptureMove(i, j, 1)) {
            return new int[]{i, j};
          }
        }
      }
    }
    return null;
  }

  private int[] prioritizeStrategic() {
    List<int[]> strategicMoves = new ArrayList<>();

    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board[i][j] == 0) {
          // Check if placing a stone is strategically advantageous
          if (isStrategicMove(i, j)) {
            strategicMoves.add(new int[]{i, j});
          }
        }
      }
    }

    if (!strategicMoves.isEmpty()) {
      // Choose a random move from the strategic moves
      Random random = new Random();
      return strategicMoves.get(random.nextInt(strategicMoves.size()));
    }

    return null;
  }

  private boolean isCaptureMove(int row, int col, int opponentStone) {
    // Check if placing a stone at (row, col) captures opponent stones
    int[][] tempBoard = copyBoard();
    tempBoard[row][col] = 2; // Simulate placing a stone

    // Check for captures in all four directions
    return isCaptured(tempBoard, row + 1, col, opponentStone) ||
        isCaptured(tempBoard, row - 1, col, opponentStone) ||
        isCaptured(tempBoard, row, col + 1, opponentStone) ||
        isCaptured(tempBoard, row, col - 1, opponentStone);
  }

  private boolean isCaptured(int[][] tempBoard, int row, int col, int opponentStone) {
    // Helper method to check if a group of opponent stones is captured
    if (row < 0 || row >= boardSize || col < 0 || col >= boardSize || tempBoard[row][col] != opponentStone) {
      return false; // Out of bounds or not opponent's stone
    }

    tempBoard[row][col] = 0; // Temporarily remove the stone for the simulation

    // Check if the group is captured
    boolean captured = true;
    captured &= isCaptured(tempBoard, row + 1, col, opponentStone);
    captured &= isCaptured(tempBoard, row - 1, col, opponentStone);
    captured &= isCaptured(tempBoard, row, col + 1, opponentStone);
    captured &= isCaptured(tempBoard, row, col - 1, opponentStone);

    return captured;
  }

  private boolean isStrategicMove(int row, int col) {
    // Check if placing a stone at (row, col) is strategically advantageous
    int[][] tempBoard = copyBoard();
    tempBoard[row][col] = 2; // Simulate placing a stone

    // Check if the stone is near own stones
    return isAdjacentToOwnStone(tempBoard, row + 1, col) ||
        isAdjacentToOwnStone(tempBoard, row - 1, col) ||
        isAdjacentToOwnStone(tempBoard, row, col + 1) ||
        isAdjacentToOwnStone(tempBoard, row, col - 1);
  }

  private boolean isAdjacentToOwnStone(int[][] tempBoard, int row, int col) {
    // Helper method to check if a cell is adjacent to own stone
    return row >= 0 && row < boardSize && col >= 0 && col < boardSize && tempBoard[row][col] == 2;
  }

  private int[][] copyBoard() {
    // Helper method to create a copy of the current board
    int[][] copy = new int[boardSize][];
    for (int i = 0; i < boardSize; i++) {
      copy[i] = board[i].clone();
    }
    return copy;
  }

  private int[] playRandomMove() {
    // If no strategic moves, play randomly
    int[][] emptyIntersections = getEmptyIntersections();

    if (emptyIntersections.length > 0) {
      Random random = new Random();
      int randomIndex = random.nextInt(emptyIntersections.length);
      return emptyIntersections[randomIndex];
    }

    // If no empty intersections are found, return an invalid move
    return new int[]{-1, -1};
  }

  private int[][] getEmptyIntersections() {
    int emptyCount = 0;

    // Count the number of empty intersections
    for (int[] row : board) {
      for (int intersection : row) {
        if (intersection == 0) {
          emptyCount++;
        }
      }
    }

    // Create an array to store empty intersection coordinates
    int[][] emptyIntersections = new int[emptyCount][2];
    int index = 0;

    // Populate the array with empty intersection coordinates
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board[i][j] == 0) {
          emptyIntersections[index][0] = i;
          emptyIntersections[index][1] = j;
          index++;
        }
      }
    }

    return emptyIntersections;
  }
}