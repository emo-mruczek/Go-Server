package org.example;

import java.util.logging.*;

public class MyLogger {

  private MyLogger() {
    throw new InstantiationError("MyLogger is a full static class!");
  }

  public static final Logger logger = Logger.getGlobal();

  public static void loggerConfig() {
    ConsoleHandler ch = new ConsoleHandler();
    ch.setLevel(Level.INFO);
    logger.addHandler(ch);

    logger.setLevel(Level.OFF);
  }
}