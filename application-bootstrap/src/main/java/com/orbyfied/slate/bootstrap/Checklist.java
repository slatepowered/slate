package com.orbyfied.slate.bootstrap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Simple utility which may serve as a checklist for completed actions.
 */
public class Checklist {
  public static final Object COMPLETED = new Object();

  final Map<String, Object> completed = new LinkedHashMap<>();

  public void completed(String name, Object value) {
    completed.put(name, value);
  }

  public void completed(String name) {
    completed.put(name, COMPLETED);
  }

  public boolean wasCompleted(String name) {
    return completed.containsKey(name);
  }

  @SuppressWarnings("unchecked")
  public <T> T completionOrNull(String name) {
    return (T) completed.get(name);
  }

  @SuppressWarnings("unchecked")
  public <T, E extends Exception> T completionOrThrow(String name, Supplier<E> supplier) throws E {
    if (!wasCompleted(name)) {
      throw supplier.get();
    }

    return (T) completed.get(name);
  }
}
