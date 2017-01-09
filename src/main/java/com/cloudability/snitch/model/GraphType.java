package com.cloudability.snitch.model;

public enum GraphType {
  line,
  column;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
