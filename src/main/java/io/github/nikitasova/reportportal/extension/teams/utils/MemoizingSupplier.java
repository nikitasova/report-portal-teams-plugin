package io.github.nikitasova.reportportal.extension.teams.utils;

import java.util.function.Supplier;

public class MemoizingSupplier<T> implements Supplier<T> {

  private final Supplier<T> delegate;
  private volatile boolean initialized;
  private T value;

  public MemoizingSupplier(Supplier<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public T get() {
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          value = delegate.get();
          initialized = true;
        }
      }
    }
    return value;
  }
}
