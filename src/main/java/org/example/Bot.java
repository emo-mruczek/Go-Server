package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Bot {
  private final int boardSize;
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
    int[] captureMove = prioritizeCapture();
    if (captureMove != null && !Arrays.equals(captureMove, prevMove)) {
      prevMove = captureMove;
      return captureMove;
    }
    int[] defendMove = prioritizeDefense();
    if (defendMove != null && !Arrays.equals(defendMove, prevMove)) {
      prevMove = defendMove;
      return defendMove;
    }
    int[] strategicMove = prioritizeStrategic();
    if (strategicMove != null && !Arrays.equals(strategicMove, prevMove)) {
      prevMove = strategicMove;
      return strategicMove;
    }
    return playRandomMove();
  }

  private int[] prioritizeCapture() {
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (board[i][j] == 0) {
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
          if (isStrategicMove(i, j)) {
            strategicMoves.add(new int[]{i, j});
          }
        }
      }
    }
    if (!strategicMoves.isEmpty()) {
      Random random = new Random();
      return strategicMoves.get(random.nextInt(strategicMoves.size()));
    }
    return null;
  }

  private boolean isCaptureMove(int row, int col, int opponentStone) {
    int[][] tempBoard = copyBoard();
    tempBoard[row][col] = 2;
    return isCaptured(tempBoard, row + 1, col, opponentStone) ||
        isCaptured(tempBoard, row - 1, col, opponentStone) ||
        isCaptured(tempBoard, row, col + 1, opponentStone) ||
        isCaptured(tempBoard, row, col - 1, opponentStone);
  }

  private boolean isCaptured(int[][] tempBoard, int row, int col, int opponentStone) {
    if (row < 0 || row >= boardSize || col < 0 || col >= boardSize || tempBoard[row][col] != opponentStone) {
      return false;
    }
    tempBoard[row][col] = 0;
    boolean captured = true;
    captured &= isCaptured(tempBoard, row + 1, col, opponentStone);
    captured &= isCaptured(tempBoard, row - 1, col, opponentStone);
    captured &= isCaptured(tempBoard, row, col + 1, opponentStone);
    captured &= isCaptured(tempBoard, row, col - 1, opponentStone);
    return captured;
  }

  private boolean isStrategicMove(int row, int col) {
    int[][] tempBoard = copyBoard();
    tempBoard[row][col] = 2;
    return isAdjacentToOwnStone(tempBoard, row + 1, col) ||
        isAdjacentToOwnStone(tempBoard, row - 1, col) ||
        isAdjacentToOwnStone(tempBoard, row, col + 1) ||
        isAdjacentToOwnStone(tempBoard, row, col - 1);
  }

  private boolean isAdjacentToOwnStone(int[][] tempBoard, int row, int col) {
    return row >= 0 && row < boardSize && col >= 0 && col < boardSize && tempBoard[row][col] == 2;
  }

  private int[][] copyBoard() {
    int[][] copy = new int[boardSize][];
    for (int i = 0; i < boardSize; i++) {
      copy[i] = board[i].clone();
    }
    return copy;
  }

  private int[] playRandomMove() {
    int[][] emptyIntersections = getEmptyIntersections();
    if (emptyIntersections.length > 0) {
      Random random = new Random();
      int randomIndex = random.nextInt(emptyIntersections.length);
      return emptyIntersections[randomIndex];
    }
    return new int[]{-1, -1};
  }

  private int[][] getEmptyIntersections() {
    int emptyCount = 0;
    for (int[] row : board) {
      for (int intersection : row) {
        if (intersection == 0) {
          emptyCount++;
        }
      }
    }
    int[][] emptyIntersections = new int[emptyCount][2];
    int index = 0;
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