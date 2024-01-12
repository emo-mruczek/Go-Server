package org.example;

public class BoardRecap {

  public BoardRecap() {
    System.out.println(DatabaseConnection.retrieveGames());
  }
}
