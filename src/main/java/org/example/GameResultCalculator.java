package org.example;

import java.util.Arrays;

public class GameResultCalculator {

    public static int[] calculateGameResult(int[][] board, int whiteCaptures, int blackCaptures, int size) {
        int[] result = new int[3];  // Indeksy: 0 - punkty czarnych, 1 - punkty białych, 2 - kto wygrał

        int[][] visited = new int[size][size];
        for (int i = 0; i < size; i++) {
            Arrays.fill(visited[i], 0);
        }

        int blackScore = calculateGroupPoints(1, board, visited);
        int whiteScore = calculateGroupPoints(2, board, visited);

        // Dodaj punkty z przejętych kamieni
        blackScore += blackCaptures;
        whiteScore += whiteCaptures;

        // Dodaj punkty za terytoria
        int[] territoryScores = calculateTerritoryScores(board, size);
        blackScore += territoryScores[0];
        whiteScore += territoryScores[1];

        // Przypisz wyniki
        result[0] = blackScore;
        result[1] = whiteScore;

        // Określ zwycięzcę
        if (blackScore > whiteScore) {
            result[2] = 1;  // Czarny wygrał
        } else if (whiteScore > blackScore) {
            result[2] = 2;  // Biały wygrał
        } else {
            result[2] = 0;  // Remis
        }

        return result;
    }

    private static int calculateGroupPoints(int color, int[][] board, int[][] visited) {
        int groupPoints = 0;

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                if (board[i][j] == color && visited[i][j] == 0) {
                    int groupSize = countGroupSize(i, j, color, board, visited);
                    if (isGroupSurrounded(i, j, color, board)) {
                        groupPoints += groupSize;
                    }
                }
            }
        }

        return groupPoints;
    }

    private static int countGroupSize(int row, int col, int color, int[][] board, int[][] visited) {
        if (!isValidPosition(row, col, board) || visited[row][col] == 1 || board[row][col] != color) {
            return 0;
        }

        visited[row][col] = 1;

        int size = 1;
        int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] neighbor : neighbors) {
            int newRow = row + neighbor[0];
            int newCol = col + neighbor[1];

            size += countGroupSize(newRow, newCol, color, board, visited);
        }

        return size;
    }

    private static int[] calculateTerritoryScores(int[][] board, int size) {
        int[] territoryScores = new int[2];  // Indeksy: 0 - punkty terytoriów czarnych, 1 - punkty terytoriów białych

        int[][] visited = new int[size][size];
        for (int i = 0; i < size; i++) {
            Arrays.fill(visited[i], 0);
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0 && visited[i][j] == 0) {
                    int territoryPoints = calculateTerritoryPoints(i, j, board, visited);
                    if (territoryPoints > 0) {
                        if (board[i][j] == 1) {
                            territoryScores[0] += territoryPoints;
                        } else if (board[i][j] == 2) {
                            territoryScores[1] += territoryPoints;
                        }
                    }
                }
            }
        }

        return territoryScores;
    }

    private static int calculateTerritoryPoints(int row, int col, int[][] board, int[][] visited) {
        if (!isValidPosition(row, col, board) || visited[row][col] == 1 || board[row][col] != 0) {
            return 0;
        }

        visited[row][col] = 1;

        int territoryPoints = 1;
        int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

        for (int[] neighbor : neighbors) {
            int newRow = row + neighbor[0];
            int newCol = col + neighbor[1];

            territoryPoints += calculateTerritoryPoints(newRow, newCol, board, visited);
        }

        return territoryPoints;
    }

    private static boolean isGroupSurrounded(int row, int col, int color, int[][] board) {
        int[][] visited = new int[board.length][board[0].length];
        return isGroupSurroundedDFS(row, col, color, board, visited);
    }

    private static boolean isGroupSurroundedDFS(int row, int col, int color, int[][] board, int[][] visited) {
        if (!isValidPosition(row, col, board) || visited[row][col] == 1) {
            return true;
        }

        if (board[row][col] == 0) {
            return false;
        }

        if (board[row][col] != color) {
            return true;
        }

        visited[row][col] = 1;

        int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        boolean surrounded = true;

        for (int[] neighbor : neighbors) {
            int newRow = row + neighbor[0];
            int newCol = col + neighbor[1];

            surrounded = surrounded && isGroupSurroundedDFS(newRow, newCol, color, board, visited);
        }

        return surrounded;
    }

    private static boolean isValidPosition(int row, int col, int[][] board) {
        return row >= 0 && row < board.length && col >= 0 && col < board[0].length;
    }
}
