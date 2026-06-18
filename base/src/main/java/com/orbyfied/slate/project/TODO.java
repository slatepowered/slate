package com.orbyfied.slate.project;

public final class TODO {

  public static void todoErrorHandling(Throwable thr) {
    thr.printStackTrace();
  }

  public static void todoErrorHandling(String msg, Throwable thr) {
    System.err.println(msg);
    thr.printStackTrace();
  }

  public static void todoEventLogging(String source, String msg) {
    System.err.println("[slate::" + source + "] " + msg);
  }

}
