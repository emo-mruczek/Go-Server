package org.example;

public class Converter {

  public static char convertPosition(int position) {
    return (position < 10) ? (char) ('0' + position) : (char) ('A' + position - 10);
  }

  public static int getChar(char rowChar) {
    if (rowChar >= '0' && rowChar <= '9') {
      return rowChar - '0';
    } else {
      return rowChar - 'A' + 10;
    }
  }
  public static int reconvertPosition(char character) {
    if (character >= '0' && character <= '9') {
      return character - '0';
    } else if (character >= 'A' && character <= 'Z') {
      return character - 'A' + 10;
    } else {
      throw new IllegalArgumentException("Invalid character: " + character);
    }
  }
}
