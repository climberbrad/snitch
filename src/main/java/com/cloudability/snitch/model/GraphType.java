package com.cloudability.snitch.model;

public enum GraphType {
  line,
  column,
  area;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
