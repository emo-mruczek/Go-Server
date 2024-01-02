package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;

public class Board {
  private final Socket socket;

  public Board(Socket socket) {
    this.socket = socket;
    run();
  }

  private void run() {
    try {
      InputStream input = socket.getInputStream();
      BufferedReader in = new BufferedReader(new InputStreamReader(input));
      String receivedType = in.readLine();


      MyLogger.logger.log(Level.INFO, "I've received " + receivedType);



    } catch (IOException ex) {
      System.out.println("Server exception: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

}
