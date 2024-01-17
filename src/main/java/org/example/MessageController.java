package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class MessageController {

    public static void sendMessage(String message, Socket socket) {
        try {
            TimeUnit.MILLISECONDS.sleep(10);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            MyLogger.logger.log(Level.INFO, message);
        } catch (UnknownHostException e) {
            System.out.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
    }

    public static String receiveMessage(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            return in.readLine();
        } catch (IOException e) {
            System.out.println("Błąd podczas odbierania wiadomości: " + e.getMessage());
            return null;
        }
    }

}