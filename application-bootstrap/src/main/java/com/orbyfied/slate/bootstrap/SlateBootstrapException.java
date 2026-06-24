package com.orbyfied.slate.bootstrap;

public class SlateBootstrapException extends RuntimeException {
  public SlateBootstrapException() {

  }

  public SlateBootstrapException(String message) {
    super(message);
  }

  public SlateBootstrapException(String message, Throwable cause) {
    super(message, cause);
  }

  public SlateBootstrapException(Throwable cause) {
    super(cause);
  }

  public SlateBootstrapException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
