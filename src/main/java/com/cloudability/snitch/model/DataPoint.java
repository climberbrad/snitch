package com.cloudability.snitch.model;

public class DataPoint {
  public final String name;
  public final double y;

  public DataPoint(String name, double y) {
    this.name = name;
    this.y = y;
  }
}
